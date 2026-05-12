import axios from 'axios'
import { useCallback, useEffect, useMemo, useState, type PropsWithChildren } from 'react'
import { fetchCurrentUser, loginUser, registerUser, type Credentials, type LoginCredentials } from '../api/client'
import type { CurrentUser, RegistrationPayload } from '../api/types'
import { AuthContext, type AuthContextValue } from './AuthContext'
const STORAGE_KEY = 'article-submission-auth'

function readStoredCredentials(): Credentials | null {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) {
    return null
  }

  try {
    const parsed = JSON.parse(raw) as Partial<Credentials>
    if (!parsed.token) {
      localStorage.removeItem(STORAGE_KEY)
      return null
    }
    return { token: parsed.token }
  } catch {
    localStorage.removeItem(STORAGE_KEY)
    return null
  }
}

function extractErrorMessage(error: unknown, fallback: string) {
  if (axios.isAxiosError(error)) {
    return (error.response?.data as { message?: string } | undefined)?.message ?? fallback
  }

  return fallback
}

export function AuthProvider({ children }: PropsWithChildren) {
  const [credentials, setCredentials] = useState<Credentials | null>(null)
  const [currentUser, setCurrentUser] = useState<CurrentUser | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [loginError, setLoginError] = useState<string | null>(null)
  const [registrationError, setRegistrationError] = useState<string | null>(null)

  const loadUser = useCallback(async (nextCredentials: Credentials) => {
    const user = await fetchCurrentUser(nextCredentials)
    setCurrentUser(user)
  }, [])

  useEffect(() => {
    const storedCredentials = readStoredCredentials()

    if (!storedCredentials) {
      setIsLoading(false)
      return
    }

    setCredentials(storedCredentials)
    void loadUser(storedCredentials)
      .catch(() => {
        localStorage.removeItem(STORAGE_KEY)
        setCredentials(null)
        setCurrentUser(null)
      })
      .finally(() => setIsLoading(false))
  }, [loadUser])

  const login = useCallback(
    async (nextCredentials: LoginCredentials) => {
      setIsLoading(true)
      setLoginError(null)
      setRegistrationError(null)
      try {
        const auth = await loginUser(nextCredentials)
        const session = { token: auth.token }
        localStorage.setItem(STORAGE_KEY, JSON.stringify(session))
        setCredentials(session)
        setCurrentUser(auth.user)
      } catch (error) {
        setLoginError(extractErrorMessage(error, 'Invalid credentials'))
        throw error
      } finally {
        setIsLoading(false)
      }
    },
    [],
  )

  const register = useCallback(async (payload: RegistrationPayload) => {
    setIsLoading(true)
    setLoginError(null)
    setRegistrationError(null)
    try {
      const auth = await registerUser(payload)
      const session = { token: auth.token }
      localStorage.setItem(STORAGE_KEY, JSON.stringify(session))
      setCredentials(session)
      setCurrentUser(auth.user)
    } catch (error) {
      setRegistrationError(extractErrorMessage(error, 'Registration failed'))
      throw error
    } finally {
      setIsLoading(false)
    }
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem(STORAGE_KEY)
    setCredentials(null)
    setCurrentUser(null)
    setLoginError(null)
    setRegistrationError(null)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      credentials,
      currentUser,
      isAuthenticated: !!credentials && !!currentUser,
      isLoading,
      loginError,
      registrationError,
      login,
      register,
      logout,
    }),
    [credentials, currentUser, isLoading, loginError, registrationError, login, register, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
