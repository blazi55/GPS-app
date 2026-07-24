import { useI18n } from '../i18n/I18nProvider'
import type { Locale } from '../i18n'

export function LanguageSwitch() {
  const { locale, setLocale, t } = useI18n()

  return (
    <div className="lang-switch" role="group" aria-label={t.lang.switchLabel}>
      {(['pl', 'en'] as Locale[]).map((code) => (
        <button
          key={code}
          type="button"
          className={`lang-switch__btn ${locale === code ? 'active' : ''}`}
          onClick={() => setLocale(code)}
          aria-pressed={locale === code}
        >
          {t.lang[code]}
        </button>
      ))}
    </div>
  )
}
