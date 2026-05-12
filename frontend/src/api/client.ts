import axios from 'axios'
import type {
  AssignReviewerPayload,
  Category,
  CreateEditorialDecisionPayload,
  CreateReviewPayload,
  CreateSubmissionPayload,
  CurrentUser,
  EditorialDecision,
  ManagedUser,
  NotificationItem,
  NotificationSummary,
  RegistrationPayload,
  ReportSummary,
  ReviewAssignment,
  ReviewItem,
  SubmissionDetail,
  SubmissionFileItem,
  SubmissionFileType,
  SubmissionSearchParams,
  SubmissionSummary,
  UpdateSubmissionPayload,
  UserRole,
  UserSummary,
} from './types'

export type Credentials = {
  token: string
}

export type LoginCredentials = {
  email: string
  password: string
}

export type AuthResponse = {
  token: string
  user: CurrentUser
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

function createAuthorizationHeader(credentials: Credentials) {
  return `Bearer ${credentials.token}`
}

function createApiClient(credentials: Credentials) {
  return axios.create({
    baseURL: API_BASE_URL,
    headers: {
      Authorization: createAuthorizationHeader(credentials),
    },
  })
}

export async function fetchCurrentUser(credentials: Credentials) {
  const client = createApiClient(credentials)
  const response = await client.get<CurrentUser>('/api/auth/me')
  return response.data
}

export async function loginUser(payload: LoginCredentials) {
  const response = await axios.post<AuthResponse>(`${API_BASE_URL}/api/auth/login`, payload)
  return response.data
}

export async function registerUser(payload: RegistrationPayload) {
  const response = await axios.post<AuthResponse>(`${API_BASE_URL}/api/auth/register`, payload)
  return response.data
}

export async function fetchUsersByRole(credentials: Credentials, role: UserRole) {
  const client = createApiClient(credentials)
  const response = await client.get<UserSummary[]>('/api/users', {
    params: { role },
  })
  return response.data
}

export async function fetchManagedUsers(credentials: Credentials) {
  const client = createApiClient(credentials)
  const response = await client.get<ManagedUser[]>('/api/users/management')
  return response.data
}

export async function updateUserRole(credentials: Credentials, userId: number, roleName: UserRole) {
  const client = createApiClient(credentials)
  const response = await client.patch<ManagedUser>(`/api/users/${userId}/role`, { roleName })
  return response.data
}

export async function fetchNotifications(credentials: Credentials) {
  const client = createApiClient(credentials)
  const response = await client.get<NotificationItem[]>('/api/notifications')
  return response.data
}

export async function fetchNotificationSummary(credentials: Credentials) {
  const client = createApiClient(credentials)
  const response = await client.get<NotificationSummary>('/api/notifications/summary')
  return response.data
}

export async function markNotificationAsRead(credentials: Credentials, notificationId: number) {
  const client = createApiClient(credentials)
  const response = await client.patch<NotificationItem>(`/api/notifications/${notificationId}/read`)
  return response.data
}

export async function markAllNotificationsAsRead(credentials: Credentials) {
  const client = createApiClient(credentials)
  const response = await client.patch<NotificationSummary>('/api/notifications/read-all')
  return response.data
}

export async function fetchCategories(credentials: Credentials) {
  const client = createApiClient(credentials)
  const response = await client.get<Category[]>('/api/categories')
  return response.data
}

export async function fetchSubmissions(credentials: Credentials, params?: SubmissionSearchParams) {
  const client = createApiClient(credentials)
  const response = await client.get<SubmissionSummary[]>('/api/submissions', {
    params,
  })
  return response.data
}

export async function fetchSubmissionDetails(credentials: Credentials, submissionId: number) {
  const client = createApiClient(credentials)
  const response = await client.get<SubmissionDetail>(`/api/submissions/${submissionId}`)
  return response.data
}

export async function createSubmission(credentials: Credentials, payload: CreateSubmissionPayload) {
  const client = createApiClient(credentials)
  const response = await client.post<SubmissionDetail>('/api/submissions', payload)
  return response.data
}

export async function updateSubmission(
  credentials: Credentials,
  submissionId: number,
  payload: UpdateSubmissionPayload,
) {
  const client = createApiClient(credentials)
  const response = await client.put<SubmissionDetail>(`/api/submissions/${submissionId}`, payload)
  return response.data
}

export async function fetchSubmissionFiles(credentials: Credentials, submissionId: number) {
  const client = createApiClient(credentials)
  const response = await client.get<SubmissionFileItem[]>(`/api/submissions/${submissionId}/files`)
  return response.data
}

export async function uploadSubmissionFile(
  credentials: Credentials,
  submissionId: number,
  fileType: SubmissionFileType,
  file: File,
) {
  const client = createApiClient(credentials)
  const formData = new FormData()
  formData.append('fileType', fileType)
  formData.append('file', file)

  const response = await client.post<SubmissionFileItem>(`/api/submissions/${submissionId}/files`, formData)
  return response.data
}

export async function downloadSubmissionFile(credentials: Credentials, submissionId: number, fileId: number) {
  const client = createApiClient(credentials)
  const response = await client.get<Blob>(`/api/submissions/${submissionId}/files/${fileId}`, {
    responseType: 'blob',
  })
  return response.data
}

export async function fetchReviewAssignments(credentials: Credentials) {
  const client = createApiClient(credentials)
  const response = await client.get<ReviewAssignment[]>('/api/review-assignments')
  return response.data
}

export async function assignReviewer(credentials: Credentials, payload: AssignReviewerPayload) {
  const client = createApiClient(credentials)
  const response = await client.post<ReviewAssignment>('/api/review-assignments', payload)
  return response.data
}

export async function fetchReviewsBySubmission(credentials: Credentials, submissionId: number) {
  const client = createApiClient(credentials)
  const response = await client.get<ReviewItem[]>(`/api/reviews/submission/${submissionId}`)
  return response.data
}

export async function submitReview(credentials: Credentials, payload: CreateReviewPayload) {
  const client = createApiClient(credentials)
  const response = await client.post<ReviewItem>('/api/reviews', payload)
  return response.data
}

export async function fetchEditorialDecisionsBySubmission(credentials: Credentials, submissionId: number) {
  const client = createApiClient(credentials)
  const response = await client.get<EditorialDecision[]>(`/api/editorial-decisions/submission/${submissionId}`)
  return response.data
}

export async function createEditorialDecision(credentials: Credentials, payload: CreateEditorialDecisionPayload) {
  const client = createApiClient(credentials)
  const response = await client.post<EditorialDecision>('/api/editorial-decisions', payload)
  return response.data
}

export async function fetchReportSummary(credentials: Credentials) {
  const client = createApiClient(credentials)
  const response = await client.get<ReportSummary>('/api/reports/summary')
  return response.data
}

export async function downloadReportCsv(credentials: Credentials) {
  const client = createApiClient(credentials)
  const response = await client.get<Blob>('/api/reports/submissions/export/csv', {
    responseType: 'blob',
  })
  return response.data
}

export async function downloadReportPdf(credentials: Credentials) {
  const client = createApiClient(credentials)
  const response = await client.get<Blob>('/api/reports/submissions/export/pdf', {
    responseType: 'blob',
  })
  return response.data
}
