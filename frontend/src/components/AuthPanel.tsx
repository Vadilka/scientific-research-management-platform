import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../auth/useAuth'

type LoginFormValues = {
  email: string
  password: string
}

type RegistrationFormValues = {
  fullName: string
  email: string
  password: string
}

export function AuthPanel() {
  const { t } = useTranslation()
  const { currentUser, isAuthenticated, isLoading, login, logout, loginError, registrationError, register } = useAuth()
  const [mode, setMode] = useState<'login' | 'register'>('login')
  const loginForm = useForm<LoginFormValues>()
  const registrationForm = useForm<RegistrationFormValues>()

  const onLoginSubmit = loginForm.handleSubmit(async (values) => {
    try {
      await login(values)
    } catch {
      // Error text is exposed by the auth context.
    }
  })

  const onRegisterSubmit = registrationForm.handleSubmit(async (values) => {
    try {
      await register(values)
    } catch {
      // Error text is exposed by the auth context.
    }
  })

  if (isAuthenticated && currentUser) {
    return (
      <section className="auth-card">
        <p className="eyebrow">{t('authSignedIn')}</p>
        <strong>{currentUser.fullName}</strong>
        <span>{currentUser.email}</span>
        <span className="role-chip">{currentUser.roleName}</span>
        <p className="muted-text">{t('authRoleManagedHint')}</p>
        <button type="button" onClick={logout}>
          {t('logout')}
        </button>
      </section>
    )
  }

  return (
    <section className="auth-card">
      <p className="eyebrow">{t('authTitle')}</p>

      <div className="segmented-control">
        <button
          type="button"
          className={mode === 'login' ? 'active' : 'secondary-button'}
          onClick={() => setMode('login')}
        >
          {t('login')}
        </button>
        <button
          type="button"
          className={mode === 'register' ? 'active' : 'secondary-button'}
          onClick={() => setMode('register')}
        >
          {t('register')}
        </button>
      </div>

      {mode === 'login' ? (
        <form className="stack-sm" onSubmit={onLoginSubmit}>
          <label className="field">
            <span>{t('emailLabel')}</span>
            <input type="email" {...loginForm.register('email', { required: true })} />
          </label>

          <label className="field">
            <span>{t('passwordLabel')}</span>
            <input type="password" {...loginForm.register('password', { required: true })} />
          </label>

          <button type="submit" disabled={isLoading}>
            {isLoading ? t('loading') : t('login')}
          </button>

          {loginError ? <p className="error-text">{loginError}</p> : null}
        </form>
      ) : (
        <form className="stack-sm" onSubmit={onRegisterSubmit}>
          <label className="field">
            <span>{t('fullNameLabel')}</span>
            <input {...registrationForm.register('fullName', { required: true })} />
          </label>

          <label className="field">
            <span>{t('emailLabel')}</span>
            <input type="email" {...registrationForm.register('email', { required: true })} />
          </label>

          <label className="field">
            <span>{t('passwordLabel')}</span>
            <input type="password" {...registrationForm.register('password', { required: true, minLength: 8 })} />
          </label>

          <button type="submit" disabled={isLoading}>
            {isLoading ? t('loading') : t('register')}
          </button>

          {registrationError ? <p className="error-text">{registrationError}</p> : null}
          <p className="muted-text">{t('registrationHint')}</p>
        </form>
      )}
    </section>
  )
}
