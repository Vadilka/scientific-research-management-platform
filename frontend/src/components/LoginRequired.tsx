import { useTranslation } from 'react-i18next'

export function LoginRequired() {
  const { t } = useTranslation()

  return (
    <section className="wide-card">
      <p className="eyebrow">Authentication</p>
      <h2>{t('loginRequiredTitle')}</h2>
      <p>{t('loginRequiredText')}</p>
    </section>
  )
}
