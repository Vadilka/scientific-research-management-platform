import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { fetchManagedUsers, updateUserRole } from '../api/client'
import type { ManagedUser, UserRole } from '../api/types'
import { useAuth } from '../auth/useAuth'
import { LoginRequired } from '../components/LoginRequired'
import { RoleGate } from '../components/RoleGate'

const ROLE_OPTIONS: UserRole[] = ['AUTHOR', 'REVIEWER', 'EDITOR', 'ADMIN']

export function AdminPage() {
  const { t } = useTranslation()
  const { credentials, isAuthenticated } = useAuth()
  const [users, setUsers] = useState<ManagedUser[]>([])
  const [pendingRoles, setPendingRoles] = useState<Record<number, UserRole>>({})
  const [error, setError] = useState<string | null>(null)
  const [message, setMessage] = useState<string | null>(null)

  const loadUsers = useCallback(async () => {
    if (!credentials) {
      return
    }

    setError(null)
    try {
      const data = await fetchManagedUsers(credentials)
      setUsers(data)
    } catch {
      setError(t('adminLoadError'))
    }
  }, [credentials, t])

  useEffect(() => {
    void loadUsers()
  }, [loadUsers])

  const onSaveRole = async (userId: number) => {
    if (!credentials) {
      return
    }

    const user = users.find((item) => item.id === userId)
    const nextRole = pendingRoles[userId] ?? user?.roleName
    if (!user || !nextRole || nextRole === user.roleName) {
      return
    }

    setError(null)
    setMessage(null)
    try {
      await updateUserRole(credentials, userId, nextRole)
      setMessage(t('roleUpdateSuccess'))
      await loadUsers()
    } catch {
      setError(t('roleUpdateError'))
    }
  }

  if (!isAuthenticated) {
    return <LoginRequired />
  }

  return (
    <RoleGate roles={['ADMIN']}>
      <section className="page-stack">
        <section className="wide-card">
          <p className="eyebrow">Administration</p>
          <h2>{t('adminTitle')}</h2>
          <p>{t('adminIntro')}</p>
          {message ? <p className="success-text">{message}</p> : null}
          {error ? <p className="error-text">{error}</p> : null}
        </section>

        <section className="wide-card">
          <div className="section-row">
            <h3>{t('userManagementTitle')}</h3>
            <span>{users.length}</span>
          </div>
          <p>{t('userManagementIntro')}</p>
          <div className="stack-sm">
            {users.map((user) => {
              const selectedRole = pendingRoles[user.id] ?? user.roleName

              return (
                <article key={user.id} className="user-management-card">
                  <div>
                    <strong>{user.fullName}</strong>
                    <p>{user.email}</p>
                    <span className="role-chip">{user.roleName}</span>
                    <p className="muted-text">
                      {user.enabled ? t('userStatusEnabled') : t('userStatusDisabled')}
                    </p>
                  </div>

                  <div className="user-management-actions">
                    <label className="field">
                      <span>{t('roleLabel')}</span>
                      <select
                        value={selectedRole}
                        onChange={(event) =>
                          setPendingRoles((current) => ({
                            ...current,
                            [user.id]: event.target.value as UserRole,
                          }))
                        }
                      >
                        {ROLE_OPTIONS.map((role) => (
                          <option key={role} value={role}>
                            {role}
                          </option>
                        ))}
                      </select>
                    </label>

                    <button
                      type="button"
                      onClick={() => void onSaveRole(user.id)}
                      disabled={selectedRole === user.roleName}
                    >
                      {t('saveRoleButton')}
                    </button>
                  </div>
                </article>
              )
            })}
          </div>
        </section>
      </section>
    </RoleGate>
  )
}
