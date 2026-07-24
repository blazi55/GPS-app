import type { Dictionary } from './types'

export const pl: Dictionary = {
  brand: {
    name: 'GPS Track',
    tagline: 'Monitoring floty w czasie rzeczywistym',
  },
  sections: {
    devices: 'Urządzenia',
    track: 'Trasa',
    newDevice: 'Nowe urządzenie',
    simulateGps: 'Symulacja GPS',
  },
  devices: {
    empty: 'Brak urządzeń — dodaj pierwsze poniżej.',
  },
  track: {
    show: 'Pokaż trasę',
    loading: 'Ładowanie…',
    clear: 'Wyczyść',
    points: 'Punkty',
    distance: 'Dystans',
  },
  deviceForm: {
    name: 'Nazwa',
    externalId: 'External ID',
    type: 'Typ',
    namePlaceholder: 'Flota-01',
    externalIdPlaceholder: 'car-01',
    submit: 'Dodaj urządzenie',
    submitting: 'Wysyłanie…',
  },
  simulate: {
    selected: 'Symulacja pozycji dla {id}',
    pickDevice: 'Wybierz urządzenie, żeby wysłać punkt GPS',
    latitude: 'Szerokość',
    longitude: 'Długość',
    submit: 'Wyślij lokalizację',
    submitting: 'Wysyłanie…',
  },
  hud: {
    activeRefresh: '{count} aktywnych · odświeżanie co {seconds}s',
  },
  errors: {
    connection: 'Błąd połączenia z API',
    addDevice: 'Nie udało się dodać urządzenia',
    sendLocation: 'Nie udało się wysłać lokalizacji',
    loadTrack: 'Nie udało się pobrać trasy',
  },
  lang: {
    pl: 'PL',
    en: 'EN',
    switchLabel: 'Język',
  },
}
