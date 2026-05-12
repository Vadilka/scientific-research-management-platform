import { useTranslation } from 'react-i18next'

type PlaceholderPageProps = {
  title: string
  description: string
}

export function PlaceholderPage({ title, description }: PlaceholderPageProps) {
  const { t } = useTranslation()

  return (
    <section className="wide-card">
      <p className="eyebrow">Module Preview</p>
      <h2>{title}</h2>
      <p>{description}</p>
      <p className="muted">{t('pagePlaceholder')}</p>
    </section>
  )
}
