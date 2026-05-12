import { useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { downloadReportCsv, downloadReportPdf, fetchReportSummary } from '../api/client'
import type { ReportSummary } from '../api/types'
import { useAuth } from '../auth/useAuth'
import { LoginRequired } from '../components/LoginRequired'

function triggerBlobDownload(blob: Blob, fileName: string) {
  const objectUrl = window.URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = objectUrl
  anchor.download = fileName
  document.body.appendChild(anchor)
  anchor.click()
  anchor.remove()
  window.URL.revokeObjectURL(objectUrl)
}

export function ReportsPage() {
  const { t } = useTranslation()
  const { credentials, isAuthenticated } = useAuth()
  const [summary, setSummary] = useState<ReportSummary | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!credentials) {
      return
    }

    setError(null)
    void fetchReportSummary(credentials)
      .then((reportSummary) => {
        setSummary(reportSummary)
      })
      .catch(() => {
        setError(t('reportsLoadError'))
      })
  }, [credentials, t])

  const reportCards = useMemo(() => {
    if (!summary) {
      return []
    }

    return [
      { label: t('metricSubmissions'), value: summary.totalSubmissions },
      { label: t('metricDrafts'), value: summary.draftSubmissions },
      { label: t('metricSubmitted'), value: summary.submittedSubmissions },
      { label: t('metricInReview'), value: summary.inReviewSubmissions },
      { label: t('metricReviewCompleted'), value: summary.reviewCompletedSubmissions },
      { label: t('metricAccepted'), value: summary.acceptedSubmissions },
      { label: t('metricRejected'), value: summary.rejectedSubmissions },
      { label: t('metricPublished'), value: summary.publishedSubmissions },
      { label: t('metricAssignments'), value: summary.totalReviewAssignments },
      { label: t('metricSubmittedReviews'), value: summary.submittedReviewAssignments },
      { label: t('metricFiles'), value: summary.totalFiles },
    ]
  }, [summary, t])

  const onDownloadCsv = async () => {
    if (!credentials) {
      return
    }

    setError(null)
    try {
      const blob = await downloadReportCsv(credentials)
      triggerBlobDownload(blob, 'submissions-report.csv')
    } catch {
      setError(t('reportExportError'))
    }
  }

  const onDownloadPdf = async () => {
    if (!credentials) {
      return
    }

    setError(null)
    try {
      const blob = await downloadReportPdf(credentials)
      triggerBlobDownload(blob, 'submissions-report.pdf')
    } catch {
      setError(t('reportExportError'))
    }
  }

  if (!isAuthenticated) {
    return <LoginRequired />
  }

  return (
    <section className="page-stack">
      <section className="wide-card">
        <p className="eyebrow">Operational Summary</p>
        <h2>{t('reportsTitle')}</h2>
        <p>{t('reportsIntro')}</p>
        {error ? <p className="error-text">{error}</p> : null}
      </section>

      <section className="wide-card">
        <div className="section-row">
          <h3>{t('reportExportsTitle')}</h3>
          <span>{summary ? t('reportsReady') : t('loading')}</span>
        </div>
        <p>{t('reportExportsIntro')}</p>
        <div className="actions-row">
          <button type="button" onClick={() => void onDownloadCsv()}>
            {t('downloadCsvButton')}
          </button>
          <button type="button" className="secondary-button" onClick={() => void onDownloadPdf()}>
            {t('downloadPdfButton')}
          </button>
        </div>
      </section>

      <section className="stats-grid">
        {reportCards.map((card) => (
          <article key={card.label} className="card">
            <h3>{card.value}</h3>
            <p>{card.label}</p>
          </article>
        ))}
      </section>
    </section>
  )
}
