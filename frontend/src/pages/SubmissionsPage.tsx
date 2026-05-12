import { useEffect, useMemo, useState } from 'react'
import { useFieldArray, useForm } from 'react-hook-form'
import { useTranslation } from 'react-i18next'
import {
  createSubmission,
  downloadSubmissionFile,
  fetchCategories,
  fetchSubmissionDetails,
  fetchSubmissions,
  updateSubmission,
  uploadSubmissionFile,
} from '../api/client'
import type {
  Category,
  CreateSubmissionPayload,
  SubmissionDetail,
  SubmissionFileType,
  SubmissionSearchParams,
  SubmissionSummary,
} from '../api/types'
import { useAuth } from '../auth/useAuth'
import { LoginRequired } from '../components/LoginRequired'
import { RoleGate } from '../components/RoleGate'

type SubmissionFormValues = {
  title: string
  abstractText: string
  keywordsText: string
  correspondingAuthorEmail: string
  categoryId: string
  submitNow: boolean
  authors: {
    fullName: string
    email: string
    affiliation: string
    authorOrder: number
    correspondingAuthor: boolean
  }[]
}

type FilterFormValues = {
  status: string
  categoryId: string
  query: string
}

type UploadFormValues = {
  submissionId: string
  fileType: SubmissionFileType
  file: FileList
}

const EMPTY_AUTHOR = {
  fullName: '',
  email: '',
  affiliation: 'Społeczna Akademia Nauk',
  authorOrder: 0,
  correspondingAuthor: true,
}

const STATUS_OPTIONS = ['DRAFT', 'SUBMITTED', 'IN_REVIEW', 'REVIEW_COMPLETED', 'ACCEPTED', 'REJECTED', 'PUBLISHED']

function buildPayload(values: SubmissionFormValues): CreateSubmissionPayload {
  return {
    title: values.title,
    abstractText: values.abstractText,
    keywords: values.keywordsText
      .split(',')
      .map((keyword) => keyword.trim())
      .filter(Boolean),
    correspondingAuthorEmail: values.correspondingAuthorEmail,
    categoryId: Number(values.categoryId),
    submitNow: values.submitNow,
    authors: values.authors.map((author, index) => ({
      ...author,
      authorOrder: index,
    })),
  }
}

export function SubmissionsPage() {
  const { t } = useTranslation()
  const { credentials, isAuthenticated } = useAuth()
  const [categories, setCategories] = useState<Category[]>([])
  const [submissions, setSubmissions] = useState<SubmissionSummary[]>([])
  const [selectedSubmission, setSelectedSubmission] = useState<SubmissionDetail | null>(null)
  const [editingSubmissionId, setEditingSubmissionId] = useState<number | null>(null)
  const [activeFilters, setActiveFilters] = useState<SubmissionSearchParams>({})
  const [loading, setLoading] = useState(true)
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  const { register, control, handleSubmit, reset } = useForm<SubmissionFormValues>({
    defaultValues: {
      title: '',
      abstractText: '',
      keywordsText: '',
      correspondingAuthorEmail: 'author@san.local',
      categoryId: '',
      submitNow: true,
      authors: [EMPTY_AUTHOR],
    },
  })

  const filterForm = useForm<FilterFormValues>({
    defaultValues: {
      status: '',
      categoryId: '',
      query: '',
    },
  })

  const { fields, append, remove, replace } = useFieldArray({
    control,
    name: 'authors',
  })

  const uploadForm = useForm<UploadFormValues>({
    defaultValues: {
      submissionId: '',
      fileType: 'PDF',
    },
  })

  const loadData = useMemo(
    () => async () => {
      if (!credentials) {
        return
      }

      setLoading(true)
      setError(null)
      try {
        const [categoriesResponse, submissionsResponse] = await Promise.all([
          fetchCategories(credentials),
          fetchSubmissions(credentials, activeFilters),
        ])
        setCategories(categoriesResponse)
        setSubmissions(submissionsResponse)
      } catch {
        setError(t('dataLoadError'))
      } finally {
        setLoading(false)
      }
    },
    [credentials, activeFilters, t],
  )

  useEffect(() => {
    void loadData()
  }, [loadData])

  const onSubmit = handleSubmit(async (values) => {
    if (!credentials) {
      return
    }

    setError(null)
    setMessage(null)
    try {
      const payload = buildPayload(values)
      const savedSubmission = editingSubmissionId
        ? await updateSubmission(credentials, editingSubmissionId, payload)
        : await createSubmission(credentials, payload)

      setSelectedSubmission(savedSubmission)
      setMessage(editingSubmissionId ? t('submissionUpdatedSuccess') : t('submissionCreatedSuccess'))
      setEditingSubmissionId(null)
      reset()
      await loadData()
    } catch {
      setError(editingSubmissionId ? t('submissionUpdateError') : t('submissionCreateError'))
    }
  })

  const onApplyFilters = filterForm.handleSubmit(async (values) => {
    setActiveFilters({
      status: values.status || undefined,
      categoryId: values.categoryId ? Number(values.categoryId) : undefined,
      query: values.query.trim() || undefined,
    })
  })

  const clearFilters = () => {
    filterForm.reset({
      status: '',
      categoryId: '',
      query: '',
    })
    setActiveFilters({})
  }

  const showDetails = async (submissionId: number) => {
    if (!credentials) {
      return
    }

    try {
      const submission = await fetchSubmissionDetails(credentials, submissionId)
      setSelectedSubmission(submission)
    } catch {
      setError(t('submissionDetailsError'))
    }
  }

  const startEditing = async (submissionId: number) => {
    if (!credentials) {
      return
    }

    setError(null)
    try {
      const submission = await fetchSubmissionDetails(credentials, submissionId)
      setSelectedSubmission(submission)
      setEditingSubmissionId(submission.id)
      reset({
        title: submission.title,
        abstractText: submission.abstractText,
        keywordsText: submission.keywords.join(', '),
        correspondingAuthorEmail: submission.correspondingAuthorEmail,
        categoryId: String(submission.category.id),
        submitNow: false,
        authors: submission.authors.map((author) => ({
          fullName: author.fullName,
          email: author.email,
          affiliation: author.affiliation,
          authorOrder: author.authorOrder,
          correspondingAuthor: author.correspondingAuthor,
        })),
      })
      replace(
        submission.authors.map((author) => ({
          fullName: author.fullName,
          email: author.email,
          affiliation: author.affiliation,
          authorOrder: author.authorOrder,
          correspondingAuthor: author.correspondingAuthor,
        })),
      )
    } catch {
      setError(t('submissionDetailsError'))
    }
  }

  const cancelEditing = () => {
    setEditingSubmissionId(null)
    reset({
      title: '',
      abstractText: '',
      keywordsText: '',
      correspondingAuthorEmail: 'author@san.local',
      categoryId: '',
      submitNow: true,
      authors: [EMPTY_AUTHOR],
    })
    replace([EMPTY_AUTHOR])
  }

  const onUploadFile = uploadForm.handleSubmit(async (values) => {
    if (!credentials) {
      return
    }

    const selectedFile = values.file?.[0]
    if (!selectedFile) {
      setError(t('fileUploadMissing'))
      return
    }

    setError(null)
    setMessage(null)
    try {
      await uploadSubmissionFile(credentials, Number(values.submissionId), values.fileType, selectedFile)
      setMessage(t('fileUploadSuccess'))
      uploadForm.reset({
        submissionId: '',
        fileType: 'PDF',
      })

      const submissionId = Number(values.submissionId)
      const updatedDetails = await fetchSubmissionDetails(credentials, submissionId)
      setSelectedSubmission(updatedDetails)
      await loadData()
    } catch {
      setError(t('fileUploadError'))
    }
  })

  const onDownloadFile = async (submissionId: number, fileId: number, fileName: string) => {
    if (!credentials) {
      return
    }

    setError(null)
    try {
      const blob = await downloadSubmissionFile(credentials, submissionId, fileId)
      const objectUrl = window.URL.createObjectURL(blob)
      const anchor = document.createElement('a')
      anchor.href = objectUrl
      anchor.download = fileName
      document.body.appendChild(anchor)
      anchor.click()
      anchor.remove()
      window.URL.revokeObjectURL(objectUrl)
    } catch {
      setError(t('fileDownloadError'))
    }
  }

  if (!isAuthenticated) {
    return <LoginRequired />
  }

  return (
    <div className="page-stack">
      <section className="wide-card">
        <p className="eyebrow">Submissions API</p>
        <h2>{t('submissionsTitle')}</h2>
        <p>{t('submissionsIntro')}</p>
        {message ? <p className="success-text">{message}</p> : null}
        {error ? <p className="error-text">{error}</p> : null}
      </section>

      <RoleGate roles={['AUTHOR', 'EDITOR', 'ADMIN']}>
        <section className="wide-card">
          <div className="section-row">
            <h3>{editingSubmissionId ? t('editSubmissionTitle') : t('newSubmissionTitle')}</h3>
            {editingSubmissionId ? (
              <button type="button" className="secondary-button" onClick={cancelEditing}>
                {t('cancelEditingButton')}
              </button>
            ) : null}
          </div>
          <form className="stack-md" onSubmit={onSubmit}>
            <label className="field">
              <span>{t('titleLabel')}</span>
              <input {...register('title', { required: true })} />
            </label>

            <label className="field">
              <span>{t('abstractLabel')}</span>
              <textarea rows={6} {...register('abstractText', { required: true })} />
            </label>

            <label className="field">
              <span>{t('keywordsLabel')}</span>
              <input {...register('keywordsText', { required: true })} />
            </label>

            <div className="field-grid">
              <label className="field">
                <span>{t('correspondingEmailLabel')}</span>
                <input type="email" {...register('correspondingAuthorEmail', { required: true })} />
              </label>

              <label className="field">
                <span>{t('categoryLabel')}</span>
                <select {...register('categoryId', { required: true })}>
                  <option value="">{t('selectCategory')}</option>
                  {categories.map((category) => (
                    <option key={category.id} value={category.id}>
                      {category.name}
                    </option>
                  ))}
                </select>
              </label>
            </div>

            <div className="authors-block">
              <div className="section-row">
                <h4>{t('authorsLabel')}</h4>
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() =>
                    append({
                      fullName: '',
                      email: '',
                      affiliation: '',
                      authorOrder: fields.length,
                      correspondingAuthor: false,
                    })
                  }
                >
                  {t('addAuthor')}
                </button>
              </div>

              {fields.map((field, index) => (
                <div key={field.id} className="author-card">
                  <div className="field-grid">
                    <label className="field">
                      <span>{t('fullNameLabel')}</span>
                      <input {...register(`authors.${index}.fullName`, { required: true })} />
                    </label>
                    <label className="field">
                      <span>{t('emailLabel')}</span>
                      <input type="email" {...register(`authors.${index}.email`, { required: true })} />
                    </label>
                  </div>

                  <label className="field">
                    <span>{t('affiliationLabel')}</span>
                    <input {...register(`authors.${index}.affiliation`, { required: true })} />
                  </label>

                  <label className="checkbox-row">
                    <input type="checkbox" {...register(`authors.${index}.correspondingAuthor`)} />
                    <span>{t('correspondingAuthorLabel')}</span>
                  </label>

                  {fields.length > 1 ? (
                    <button type="button" className="danger-button" onClick={() => remove(index)}>
                      {t('removeAuthor')}
                    </button>
                  ) : null}
                </div>
              ))}
            </div>

            <label className="checkbox-row">
              <input type="checkbox" {...register('submitNow')} />
              <span>{t('submitNowLabel')}</span>
            </label>

            <button type="submit">{editingSubmissionId ? t('saveSubmissionButton') : t('createSubmissionButton')}</button>
          </form>
        </section>
      </RoleGate>

      <RoleGate roles={['AUTHOR', 'EDITOR', 'ADMIN']}>
        <section className="wide-card">
          <h3>{t('submissionFilesTitle')}</h3>
          <p>{t('submissionFilesIntro')}</p>
          <form className="stack-md" onSubmit={onUploadFile}>
            <div className="field-grid">
              <label className="field">
                <span>{t('submissionLabel')}</span>
                <select {...uploadForm.register('submissionId', { required: true })}>
                  <option value="">{t('selectSubmission')}</option>
                  {submissions.map((submission) => (
                    <option key={submission.id} value={submission.id}>
                      {submission.title} ({submission.status})
                    </option>
                  ))}
                </select>
              </label>

              <label className="field">
                <span>{t('fileTypeLabel')}</span>
                <select {...uploadForm.register('fileType', { required: true })}>
                  <option value="PDF">PDF</option>
                  <option value="DOCX">DOCX</option>
                  <option value="LATEX_SOURCE">LATEX_SOURCE</option>
                </select>
              </label>
            </div>

            <label className="field">
              <span>{t('selectFileLabel')}</span>
              <input
                type="file"
                accept=".pdf,.docx,.tex,.zip"
                {...uploadForm.register('file', { required: true })}
              />
            </label>

            <button type="submit">{t('uploadFileButton')}</button>
          </form>
        </section>
      </RoleGate>

      <section className="wide-card">
        <div className="section-row">
          <h3>{t('submissionFiltersTitle')}</h3>
          {loading ? <span>{t('loading')}</span> : null}
        </div>
        <form className="stack-md" onSubmit={onApplyFilters}>
          <div className="field-grid">
            <label className="field">
              <span>{t('statusLabel')}</span>
              <select {...filterForm.register('status')}>
                <option value="">{t('allStatuses')}</option>
                {STATUS_OPTIONS.map((status) => (
                  <option key={status} value={status}>
                    {status}
                  </option>
                ))}
              </select>
            </label>

            <label className="field">
              <span>{t('categoryLabel')}</span>
              <select {...filterForm.register('categoryId')}>
                <option value="">{t('allCategories')}</option>
                {categories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.name}
                  </option>
                ))}
              </select>
            </label>
          </div>

          <label className="field">
            <span>{t('searchQueryLabel')}</span>
            <input {...filterForm.register('query')} />
          </label>

          <div className="actions-row">
            <button type="submit">{t('applyFiltersButton')}</button>
            <button type="button" className="secondary-button" onClick={clearFilters}>
              {t('clearFiltersButton')}
            </button>
          </div>
        </form>
      </section>

      <section className="wide-card">
        <div className="section-row">
          <h3>{t('submissionListTitle')}</h3>
          <span>{submissions.length}</span>
        </div>

        <div className="stack-sm">
          {submissions.map((submission) => (
            <article key={submission.id} className="list-card">
              <div>
                <strong>{submission.title}</strong>
                <p>
                  {submission.status} · {submission.categoryName}
                </p>
              </div>
              <div className="actions-row">
                {submission.status === 'DRAFT' ? (
                  <button type="button" className="secondary-button" onClick={() => void startEditing(submission.id)}>
                    {t('editSubmissionButton')}
                  </button>
                ) : null}
                <button type="button" className="secondary-button" onClick={() => void showDetails(submission.id)}>
                  {t('viewDetails')}
                </button>
              </div>
            </article>
          ))}
        </div>
      </section>

      {selectedSubmission ? (
        <section className="wide-card accent">
          <h3>{selectedSubmission.title}</h3>
          <p>{selectedSubmission.abstractText}</p>
          <p>
            <strong>{t('statusLabel')}:</strong> {selectedSubmission.status}
          </p>
          <p>
            <strong>{t('keywordsLabel')}:</strong> {selectedSubmission.keywords.join(', ')}
          </p>
          <p>
            <strong>{t('authorsLabel')}:</strong>{' '}
            {selectedSubmission.authors.map((author) => author.fullName).join(', ')}
          </p>
          <div className="stack-sm">
            <strong>{t('filesLabel')}:</strong>
            {selectedSubmission.files.length === 0 ? <p>{t('filesEmpty')}</p> : null}
            {selectedSubmission.files.map((file) => (
              <article key={file.id} className="list-card">
                <div>
                  <strong>{file.originalFileName}</strong>
                  <p>
                    {file.fileType} · {Math.max(1, Math.round(file.fileSize / 1024))} KB
                  </p>
                </div>
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => void onDownloadFile(selectedSubmission.id, file.id, file.originalFileName)}
                >
                  {t('downloadFileButton')}
                </button>
              </article>
            ))}
          </div>
        </section>
      ) : null}
    </div>
  )
}
