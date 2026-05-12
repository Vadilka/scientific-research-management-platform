import { useEffect, useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'
import { useTranslation } from 'react-i18next'
import {
  createEditorialDecision,
  fetchEditorialDecisionsBySubmission,
  fetchSubmissions,
} from '../api/client'
import type {
  CreateEditorialDecisionPayload,
  EditorialDecision,
  EditorialDecisionType,
  SubmissionSummary,
} from '../api/types'
import { useAuth } from '../auth/useAuth'
import { LoginRequired } from '../components/LoginRequired'
import { RoleGate } from '../components/RoleGate'

type EditorialDecisionFormValues = {
  submissionId: string
  decisionType: EditorialDecisionType
  decisionNote: string
}

export function PublishedPage() {
  const { t } = useTranslation()
  const { credentials, currentUser, isAuthenticated } = useAuth()
  const [publishedItems, setPublishedItems] = useState<SubmissionSummary[]>([])
  const [decisions, setDecisions] = useState<EditorialDecision[]>([])
  const [selectedSubmissionId, setSelectedSubmissionId] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [message, setMessage] = useState<string | null>(null)

  const decisionForm = useForm<EditorialDecisionFormValues>({
    defaultValues: {
      submissionId: '',
      decisionType: 'ACCEPT',
      decisionNote: '',
    },
  })

  const manageableSubmissions = useMemo(
    () =>
      publishedItems.filter((item) =>
        ['REVIEW_COMPLETED', 'ACCEPTED', 'REJECTED', 'PUBLISHED'].includes(item.status),
      ),
    [publishedItems],
  )

  const isEditorOrAdmin = currentUser ? ['EDITOR', 'ADMIN'].includes(currentUser.roleName) : false

  useEffect(() => {
    if (!credentials) {
      return
    }

    setError(null)
    void fetchSubmissions(credentials)
      .then((items) => {
        setPublishedItems(
          items.filter((item) => ['PUBLISHED', 'ACCEPTED', 'REVIEW_COMPLETED', 'REJECTED'].includes(item.status)),
        )
      })
      .catch(() => {
        setError(t('publishedLoadError'))
      })
  }, [credentials, t])

  const loadDecisions = async (submissionId: number) => {
    if (!credentials) {
      return
    }

    setSelectedSubmissionId(submissionId)
    setError(null)
    try {
      const decisionItems = await fetchEditorialDecisionsBySubmission(credentials, submissionId)
      setDecisions(decisionItems)
    } catch {
      setError(t('decisionLoadError'))
    }
  }

  const onSubmitDecision = decisionForm.handleSubmit(async (values) => {
    if (!credentials) {
      return
    }

    setError(null)
    setMessage(null)
    try {
      const payload: CreateEditorialDecisionPayload = {
        submissionId: Number(values.submissionId),
        decisionType: values.decisionType,
        decisionNote: values.decisionNote,
      }
      const createdDecision = await createEditorialDecision(credentials, payload)
      setMessage(t('editorialDecisionSuccess'))
      decisionForm.reset({
        submissionId: '',
        decisionType: 'ACCEPT',
        decisionNote: '',
      })

      const items = await fetchSubmissions(credentials)
      setPublishedItems(
        items.filter((item) => ['PUBLISHED', 'ACCEPTED', 'REVIEW_COMPLETED', 'REJECTED'].includes(item.status)),
      )
      await loadDecisions(createdDecision.submissionId)
    } catch {
      setError(t('editorialDecisionError'))
    }
  })

  if (!isAuthenticated) {
    return <LoginRequired />
  }

  return (
    <div className="page-stack">
      <section className="wide-card">
        <p className="eyebrow">Publication Queue</p>
        <h2>{t('publishedTitle')}</h2>
        <p>{t('publishedIntro')}</p>
        {message ? <p className="success-text">{message}</p> : null}
        {error ? <p className="error-text">{error}</p> : null}
      </section>

      {isEditorOrAdmin ? (
        <RoleGate roles={['EDITOR', 'ADMIN']}>
          <section className="wide-card">
            <h3>{t('editorialDecisionTitle')}</h3>
            <p>{t('editorialDecisionIntro')}</p>
            <form className="stack-md" onSubmit={onSubmitDecision}>
              <div className="field-grid">
                <label className="field">
                  <span>{t('submissionLabel')}</span>
                  <select {...decisionForm.register('submissionId', { required: true })}>
                    <option value="">{t('selectSubmission')}</option>
                    {manageableSubmissions.map((submission) => (
                      <option key={submission.id} value={submission.id}>
                        {submission.title} ({submission.status})
                      </option>
                    ))}
                  </select>
                </label>

                <label className="field">
                  <span>{t('decisionTypeLabel')}</span>
                  <select {...decisionForm.register('decisionType', { required: true })}>
                    <option value="ACCEPT">ACCEPT</option>
                    <option value="REJECT">REJECT</option>
                    <option value="PUBLISH">PUBLISH</option>
                  </select>
                </label>
              </div>

              <label className="field">
                <span>{t('decisionNoteLabel')}</span>
                <textarea rows={5} {...decisionForm.register('decisionNote', { required: true })} />
              </label>

              <button type="submit">{t('saveDecisionButton')}</button>
            </form>
          </section>
        </RoleGate>
      ) : null}

      <section className="wide-card">
        <div className="section-row">
          <h3>{t('publicationQueueTitle')}</h3>
          <span>{publishedItems.length}</span>
        </div>
        <div className="stack-sm">
          {publishedItems.length === 0 ? <p>{t('publishedEmpty')}</p> : null}
          {publishedItems.map((item) => (
            <article key={item.id} className="list-card">
              <div>
                <strong>{item.title}</strong>
                <p>
                  {item.status} · {item.categoryName}
                </p>
              </div>
              <button type="button" className="secondary-button" onClick={() => void loadDecisions(item.id)}>
                {t('loadDecisionsButton')}
              </button>
            </article>
          ))}
        </div>
      </section>

      <section className="wide-card accent">
        <div className="section-row">
          <h3>{t('decisionHistoryTitle')}</h3>
          <span>{selectedSubmissionId ? `#${selectedSubmissionId}` : '-'}</span>
        </div>
        {decisions.length === 0 ? <p>{t('decisionHistoryEmpty')}</p> : null}
        <div className="stack-sm">
          {decisions.map((decision) => (
            <article key={decision.id} className="review-card">
              <strong>
                {decision.decisionType} · {decision.editorEmail}
              </strong>
              <p>{decision.decisionNote}</p>
              <p className="muted">
                {decision.submissionStatus} · {new Date(decision.decidedAt).toLocaleString()}
              </p>
            </article>
          ))}
        </div>
      </section>
    </div>
  )
}
