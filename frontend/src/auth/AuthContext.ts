import { createContext } from 'react'
import type { Credentials, LoginCredentials } from '../api/client'
import type { CurrentUser, RegistrationPayload } from '../api/types'

export type AuthContextValue = {
  credentials: Credentials | null
  currentUser: CurrentUser | null
  isAuthenticated: boolean
  isLoading: boolean
  loginError: string | null
  registrationError: string | null
  login: (credentials: LoginCredentials) => Promise<void>
  register: (payload: RegistrationPayload) => Promise<void>
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined)
