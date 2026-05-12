import { useTranslation } from 'react-i18next'

export function LanguageSwitcher() {
  const { i18n, t } = useTranslation()

  const languages = [
    { code: 'pl', label: 'PL' },
    { code: 'en', label: 'EN' },
  ]

  return (
    <div className="language-switcher" aria-label={t('language')}>
      {languages.map((language) => (
        <button
          key={language.code}
          type="button"
          className={i18n.resolvedLanguage === language.code ? 'active' : ''}
          onClick={() => void i18n.changeLanguage(language.code)}
        >
          {language.label}
        </button>
      ))}
    </div>
  )
}
