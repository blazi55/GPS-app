export type Locale = 'pl' | 'en'

export interface Dictionary {
  brand: {
    name: string
    tagline: string
  }
  sections: {
    devices: string
    track: string
    newDevice: string
    simulateGps: string
  }
  devices: {
    empty: string
  }
  track: {
    show: string
    loading: string
    clear: string
    points: string
    distance: string
  }
  deviceForm: {
    name: string
    externalId: string
    type: string
    namePlaceholder: string
    externalIdPlaceholder: string
    submit: string
    submitting: string
  }
  simulate: {
    selected: string
    pickDevice: string
    latitude: string
    longitude: string
    submit: string
    submitting: string
  }
  hud: {
    activeRefresh: string
  }
  errors: {
    connection: string
    addDevice: string
    sendLocation: string
    loadTrack: string
  }
  lang: {
    pl: string
    en: string
    switchLabel: string
  }
}

export type TranslationKey = Dictionary
