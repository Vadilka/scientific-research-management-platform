export type UserRole = 'ADMIN' | 'EDITOR' | 'REVIEWER' | 'AUTHOR'

export type CurrentUser = {
  id: number
  fullName: string
  email: string
  roleName: UserRole
  enabled: boolean
}

export type UserSummary = {
  id: number
  fullName: string
  email: string
  roleName: UserRole
}

export type ManagedUser = {
  id: number
  fullName: string
  email: string
  roleName: UserRole
  enabled: boolean
}

export type RegistrationPayload = {
  fullName: string
  email: string
  password: string
}

export type NotificationItem = {
  id: number
  notificationType: string
  title: string
  message: string
  submissionId: number | null
  submissionTitle: string | null
  read: boolean
  readAt: string | null
  createdAt: string
}

export type NotificationSummary = {
  unreadCount: number
}

export type Category = {
  id: number
  code: string
  name: string
  description: string
}

export type SubmissionSummary = {
  id: number
  title: string
  status: string
  categoryName: string
  submittedByEmail: string
  submittedAt: string | null
  createdAt: string
  updatedAt: string
}

export type SubmissionAuthor = {
  id?: number
  fullName: string
  email: string
  affiliation: string
  authorOrder: number
  correspondingAuthor: boolean
}

export type SubmissionDetail = {
  id: number
  title: string
  abstractText: string
  keywords: string[]
  correspondingAuthorEmail: string
  status: string
  category: Category
  submittedByEmail: string
  submittedAt: string | null
  createdAt: string
  updatedAt: string
  authors: SubmissionAuthor[]
  files: SubmissionFileItem[]
}

export type CreateSubmissionPayload = {
  title: string
  abstractText: string
  keywords: string[]
  correspondingAuthorEmail: string
  categoryId: number
  authors: SubmissionAuthor[]
  submitNow: boolean
}

export type UpdateSubmissionPayload = CreateSubmissionPayload

export type SubmissionSearchParams = {
  status?: string
  categoryId?: number
  query?: string
}

export type SubmissionFileType = 'PDF' | 'DOCX' | 'LATEX_SOURCE'

export type SubmissionFileItem = {
  id: number
  originalFileName: string
  mediaType: string
  fileType: SubmissionFileType
  fileSize: number
  createdAt: string
}

export type ReviewAssignment = {
  id: number
  submissionId: number
  submissionTitle: string
  submissionStatus: string
  categoryName: string
  reviewerEmail: string
  status: string
  assignedAt: string
  dueDate: string | null
}

export type AssignReviewerPayload = {
  submissionId: number
  reviewerEmail: string
  dueDate?: string | null
}

export type ReviewRecommendation = 'ACCEPT' | 'ACCEPT_WITH_CHANGES' | 'REJECT'

export type ReviewItem = {
  id: number
  assignmentId: number
  submissionId: number
  submissionTitle: string
  reviewerEmail: string
  score: number
  recommendation: ReviewRecommendation
  summaryComment: string
  detailedComment: string
  submittedAt: string
}

export type CreateReviewPayload = {
  assignmentId: number
  score: number
  recommendation: ReviewRecommendation
  summaryComment: string
  detailedComment: string
}

export type EditorialDecisionType = 'ACCEPT' | 'REJECT' | 'PUBLISH'

export type EditorialDecision = {
  id: number
  submissionId: number
  submissionTitle: string
  submissionStatus: string
  editorEmail: string
  decisionType: EditorialDecisionType
  decisionNote: string
  decidedAt: string
}

export type CreateEditorialDecisionPayload = {
  submissionId: number
  decisionType: EditorialDecisionType
  decisionNote: string
}

export type ReportSummary = {
  totalSubmissions: number
  draftSubmissions: number
  submittedSubmissions: number
  inReviewSubmissions: number
  reviewCompletedSubmissions: number
  acceptedSubmissions: number
  rejectedSubmissions: number
  publishedSubmissions: number
  totalReviewAssignments: number
  submittedReviewAssignments: number
  totalFiles: number
}
