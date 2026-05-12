import { useEffect, useMemo, useState } from 'react'
import { useForm } from 'react-hook-form'
import { useTranslation } from 'react-i18next'
import {
  assignReviewer,
  fetchReviewAssignments,
  fetchReviewsBySubmission,
  fetchSubmissions,
  fetchUsersByRole,
  submitReview,
} from '../api/client'
import type {
  AssignReviewerPayload,
  CreateReviewPayload,
  ReviewAssignment,
  ReviewItem,
  SubmissionSummary,
  UserSummary,
} from '../api/types'
import { useAuth } from '../auth/useAuth'
import { LoginRequired } from '../components/LoginRequired'
import { RoleGate } from '../components/RoleGate'

type AssignmentFormValues = {
  submissionId: string
  reviewerEmail: string
  dueDate: string
}

type ReviewFormValues = {
  assignmentId: string
  score: number
  recommendation: 'ACCEPT' | 'ACCEPT_WITH_CHANGES' | 'REJECT'
  summaryComment: string
  detailedComment: string
}

export function ReviewsPage() {
  const { t } = useTranslation()
  const { credentials, currentUser, isAuthenticated } = useAuth()
  const [assignments, setAssignments] = useState<ReviewAssignment[]>([])
  const [submissions, setSubmissions] = useState<SubmissionSummary[]>([])
  const [reviewers, setReviewers] = useState<UserSummary[]>([])
  const [reviews, setReviews] = useState<ReviewItem[]>([])
  const [selectedSubmissionId, setSelectedSubmissionId] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [message, setMessage] = useState<string | null>(null)

  const assignmentForm = useForm<AssignmentFormValues>()
  const reviewForm = useForm<ReviewFormValues>({
    defaultValues: {
      score: 8,
      recommendation: 'ACCEPT_WITH_CHANGES',
    },
  })

  const canAssign = currentUser ? ['EDITOR', 'ADMIN'].includes(currentUser.roleName) : false

  const loadData = useMemo(
    () => async () => {
      if (!credentials) {
        return
      }

      setError(null)
      try {
        const assignmentData = await fetchReviewAssignments(credentials)
        setAssignments(assignmentData)

        if (canAssign) {
          const [submissionData, reviewerData] = await Promise.all([
            fetchSubmissions(credentials),
            fetchUsersByRole(credentials, 'REVIEWER'),
          ])
          setSubmissions(submissionData)
          setReviewers(reviewerData)
        }
      } catch {
        setError(t('dataLoadError'))
      }
    },
    [credentials, canAssign, t],
  )

  useEffect(() => {
    void loadData()
  }, [loadData])

  const loadReviews = async (submissionId: number) => {
    if (!credentials) {
      return
    }

    setSelectedSubmissionId(submissionId)
    try {
      const reviewData = await fetchReviewsBySubmission(credentials, submissionId)
      setReviews(reviewData)
    } catch {
      setError(t('reviewLoadError'))
    }
  }

  const onAssignReviewer = assignmentForm.handleSubmit(async (values) => {
    if (!credentials) {
      return
    }

    setError(null)
    setMessage(null)
    try {
      const payload: AssignReviewerPayload = {
        submissionId: Number(values.submissionId),
        reviewerEmail: values.reviewerEmail,
        dueDate: values.dueDate ? new Date(values.dueDate).toISOString() : null,
      }
      await assignReviewer(credentials, payload)
      setMessage(t('assignmentCreateSuccess'))
      assignmentForm.reset()
      await loadData()
    } catch {
      setError(t('assignmentCreateError'))
    }
  })

  const onSubmitReview = reviewForm.handleSubmit(async (values) => {
    if (!credentials) {
      return
    }

    setError(null)
    setMessage(null)
    try {
      const payload: CreateReviewPayload = {
        assignmentId: Number(values.assignmentId),
        score: Number(values.score),
        recommendation: values.recommendation,
        summaryComment: values.summaryComment,
        detailedComment: values.detailedComment,
      }
      const createdReview = await submitReview(credentials, payload)
      setMessage(t('reviewSubmitSuccess'))
      reviewForm.reset()
      await loadData()
      await loadReviews(createdReview.submissionId)
    } catch {
      setError(t('reviewSubmitError'))
    }
  })

  if (!isAuthenticated) {
    return <LoginRequired />
  }

  return (
    <div className="page-stack">
      <section className="wide-card">
        <p className="eyebrow">Review Workflow</p>
        <h2>{t('reviewsTitle')}</h2>
        <p>{t('reviewsIntro')}</p>
        {message ? <p className="success-text">{message}</p> : null}
        {error ? <p className="error-text">{error}</p> : null}
      </section>

      <RoleGate roles={['EDITOR', 'ADMIN']}>
        <section className="wide-card">
          <h3>{t('assignReviewerTitle')}</h3>
          <form className="stack-md" onSubmit={onAssignReviewer}>
            <div className="field-grid">
              <label className="field">
                <span>{t('submissionLabel')}</span>
                <select {...assignmentForm.register('submissionId', { required: true })}>
                  <option value="">{t('selectSubmission')}</option>
                  {submissions.map((submission) => (
                    <option key={submission.id} value={submission.id}>
                      {submission.title} ({submission.status})
                    </option>
                  ))}
                </select>
              </label>

              <label className="field">
                <span>{t('reviewerLabel')}</span>
                <select {...assignmentForm.register('reviewerEmail', { required: true })}>
                  <option value="">{t('selectReviewer')}</option>
                  {reviewers.map((reviewer) => (
                    <option key={reviewer.id} value={reviewer.email}>
                      {reviewer.fullName} ({reviewer.email})
                    </option>
                  ))}
                </select>
              </label>
            </div>

            <label className="field">
              <span>{t('dueDateLabel')}</span>
              <input type="datetime-local" {...assignmentForm.register('dueDate')} />
            </label>

            <button type="submit">{t('assignReviewerButton')}</button>
          </form>
        </section>
      </RoleGate>

      <RoleGate roles={['REVIEWER', 'ADMIN']}>
        <section className="wide-card">
          <h3>{t('submitReviewTitle')}</h3>
          <form className="stack-md" onSubmit={onSubmitReview}>
            <label className="field">
              <span>{t('assignmentLabel')}</span>
              <select {...reviewForm.register('assignmentId', { required: true })}>
                <option value="">{t('selectAssignment')}</option>
                {assignments.map((assignment) => (
                  <option key={assignment.id} value={assignment.id}>
                    #{assignment.id} · {assignment.submissionTitle} · {assignment.status}
                  </option>
                ))}
              </select>
            </label>

            <div className="field-grid">
              <label className="field">
                <span>{t('scoreLabel')}</span>
                <input type="number" min={1} max={10} {...reviewForm.register('score', { required: true })} />
              </label>

              <label className="field">
                <span>{t('recommendationLabel')}</span>
                <select {...reviewForm.register('recommendation', { required: true })}>
                  <option value="ACCEPT">ACCEPT</option>
                  <option value="ACCEPT_WITH_CHANGES">ACCEPT_WITH_CHANGES</option>
                  <option value="REJECT">REJECT</option>
                </select>
              </label>
            </div>

            <label className="field">
              <span>{t('summaryCommentLabel')}</span>
              <input {...reviewForm.register('summaryComment', { required: true })} />
            </label>

            <label className="field">
              <span>{t('detailedCommentLabel')}</span>
              <textarea rows={5} {...reviewForm.register('detailedComment', { required: true })} />
            </label>

            <button type="submit">{t('submitReviewButton')}</button>
          </form>
        </section>
      </RoleGate>

      <section className="wide-card">
        <div className="section-row">
          <h3>{t('assignmentListTitle')}</h3>
          <span>{assignments.length}</span>
        </div>
        <div className="stack-sm">
          {assignments.map((assignment) => (
            <article key={assignment.id} className="list-card">
              <div>
                <strong>{assignment.submissionTitle}</strong>
                <p>
                  {assignment.reviewerEmail} · {assignment.status} · {assignment.submissionStatus}
                </p>
              </div>
              <button
                type="button"
                className="secondary-button"
                onClick={() => void loadReviews(assignment.submissionId)}
              >
                {t('loadReviewsButton')}
              </button>
            </article>
          ))}
        </div>
      </section>

      <section className="wide-card accent">
        <div className="section-row">
          <h3>{t('reviewListTitle')}</h3>
          <span>{selectedSubmissionId ? `#${selectedSubmissionId}` : '-'}</span>
        </div>
        {reviews.length === 0 ? <p>{t('reviewListEmpty')}</p> : null}
        <div className="stack-sm">
          {reviews.map((review) => (
            <article key={review.id} className="review-card">
              <strong>
                {review.reviewerEmail} · {review.recommendation} · {review.score}/10
              </strong>
              <p>{review.summaryComment}</p>
              <p className="muted">{review.detailedComment}</p>
            </article>
          ))}
        </div>
      </section>
    </div>
  )
}
