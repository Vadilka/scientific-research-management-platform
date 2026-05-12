import { useTranslation } from 'react-i18next'
import { useAuth } from '../auth/useAuth'

const roles = [
  { titleKey: 'roleAuthor', textKey: 'roleAuthorText' },
  { titleKey: 'roleReviewer', textKey: 'roleReviewerText' },
  { titleKey: 'roleEditor', textKey: 'roleEditorText' },
  { titleKey: 'roleAdmin', textKey: 'roleAdminText' },
]

const features = [
  { titleKey: 'featureSubmissionTitle', textKey: 'featureSubmissionText' },
  { titleKey: 'featureReviewTitle', textKey: 'featureReviewText' },
  { titleKey: 'featureOpsTitle', textKey: 'featureOpsText' },
]

export function HomePage() {
  const { t } = useTranslation()
  const { currentUser, isAuthenticated } = useAuth()

  return (
    <div className="page-stack">
      <section className="hero-card">
        <div className="hero-copy">
          <p className="eyebrow">MVP Blueprint</p>
          <h2>{t('heroTitle')}</h2>
          <p>{t('heroText')}</p>
          <div className="hero-actions">
            <a href="#mvp">{t('primaryAction')}</a>
            <a href="#milestone" className="secondary">
              {t('secondaryAction')}
            </a>
          </div>
        </div>
        <div className="hero-panel">
          <div className="status-pill">
            {isAuthenticated && currentUser
              ? `${currentUser.roleName} · ${currentUser.email}`
              : 'Backend + Frontend + Docs Ready'}
          </div>
          <div className="status-grid">
            <article>
              <span>PL/EN UI</span>
              <strong>Enabled</strong>
            </article>
            <article>
              <span>Spring Boot</span>
              <strong>API Ready</strong>
            </article>
            <article>
              <span>React + Vite</span>
              <strong>Production Build</strong>
            </article>
            <article>
              <span>Domain Model</span>
              <strong>Implemented</strong>
            </article>
          </div>
        </div>
      </section>

      <section className="feature-grid">
        {features.map((feature) => (
          <article key={feature.titleKey} className="card">
            <h3>{t(feature.titleKey)}</h3>
            <p>{t(feature.textKey)}</p>
          </article>
        ))}
      </section>

      <section className="role-grid">
        {roles.map((role) => (
          <article key={role.titleKey} className="card">
            <h3>{t(role.titleKey)}</h3>
            <p>{t(role.textKey)}</p>
          </article>
        ))}
      </section>

      <section id="milestone" className="wide-card">
        <h3>{t('milestoneTitle')}</h3>
        <p>{t('milestoneText')}</p>
      </section>

      <section id="mvp" className="wide-card accent">
        <h3>{t('mvpTitle')}</h3>
        <p>{t('mvpItems')}</p>
      </section>
    </div>
  )
}
