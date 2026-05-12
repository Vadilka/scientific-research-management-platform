import type { PropsWithChildren } from 'react'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../auth/useAuth'
import type { UserRole } from '../api/types'

type RoleGateProps = PropsWithChildren<{
  roles: UserRole[]
}>

export function RoleGate({ roles, children }: RoleGateProps) {
  const { t } = useTranslation()
  const { currentUser } = useAuth()

  if (!currentUser || !roles.includes(currentUser.roleName)) {
    return (
      <section className="wide-card">
        <p className="eyebrow">Access</p>
        <h3>{t('accessRestrictedTitle')}</h3>
        <p>{t('accessRestrictedText')}</p>
      </section>
    )
  }

  return <>{children}</>
}
