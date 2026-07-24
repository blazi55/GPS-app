import { en } from './en'
import { pl } from './pl'
import type { Dictionary, Locale } from './types'

export const dictionaries: Record<Locale, Dictionary> = {
  pl,
  en,
}

export const LOCALE_STORAGE_KEY = 'gps-locale'

export function resolveLocale(value: string | null): Locale {
  return value === 'en' ? 'en' : 'pl'
}

export function formatMessage(
  template: string,
  params: Record<string, string | number>,
): string {
  return Object.entries(params).reduce(
    (result, [key, val]) => result.replaceAll(`{${key}}`, String(val)),
    template,
  )
}

export type { Dictionary, Locale }
