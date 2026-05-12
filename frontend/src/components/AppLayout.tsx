import { NavLink, Outlet } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../auth/useAuth'
import type { UserRole } from '../api/types'
import { AuthPanel } from './AuthPanel'
import { LanguageSwitcher } from './LanguageSwitcher'

function canAccess(roleName: UserRole | undefined, allowedRoles: UserRole[]) {
  if (!roleName) {
    return false
  }

  return allowedRoles.includes(roleName)
}

export function AppLayout() {
  const { t } = useTranslation()
  const { isAuthenticated, currentUser } = useAuth()

  const roleName = currentUser?.roleName
  const navItems = [
    { to: '/', label: t('home'), end: true },
    ...(isAuthenticated ? [{ to: '/submissions', label: t('submissions') }] : []),
    ...(isAuthenticated ? [{ to: '/notifications', label: t('notifications') }] : []),
    ...(canAccess(roleName, ['REVIEWER', 'EDITOR', 'ADMIN']) ? [{ to: '/reviews', label: t('reviews') }] : []),
    ...(canAccess(roleName, ['EDITOR', 'ADMIN']) ? [{ to: '/published', label: t('published') }] : []),
    ...(isAuthenticated ? [{ to: '/reports', label: t('reports') }] : []),
    ...(canAccess(roleName, ['ADMIN']) ? [{ to: '/admin', label: t('admin') }] : []),
  ]

  return (
    <div className="app-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">SAN / JEE Project</p>
          <h1>{t('brand')}</h1>
          <p className="subtitle">{t('subtitle')}</p>
          {isAuthenticated && currentUser ? (
            <p className="subtitle">
              {t('signedInAs')}: <strong>{currentUser.roleName}</strong>
            </p>
          ) : null}
        </div>
        <div className="header-side">
          <LanguageSwitcher />
          <AuthPanel />
        </div>
      </header>

      <nav className="nav">
        {navItems.map((item) => (
          <NavLink key={item.to} to={item.to} end={item.end}>
            {item.label}
          </NavLink>
        ))}
      </nav>

      <main className="content">
        <Outlet />
      </main>

      <footer className="footer">{t('footer')}</footer>
    </div>
  )
}
