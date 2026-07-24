import type { Dictionary } from './types'

export const en: Dictionary = {
  brand: {
    name: 'GPS Track',
    tagline: 'Real-time fleet monitoring',
  },
  sections: {
    devices: 'Devices',
    track: 'Track',
    newDevice: 'New device',
    simulateGps: 'GPS simulation',
  },
  devices: {
    empty: 'No devices yet — add the first one below.',
  },
  track: {
    show: 'Show track',
    loading: 'Loading…',
    clear: 'Clear',
    points: 'Points',
    distance: 'Distance',
  },
  deviceForm: {
    name: 'Name',
    externalId: 'External ID',
    type: 'Type',
    namePlaceholder: 'Fleet-01',
    externalIdPlaceholder: 'car-01',
    submit: 'Add device',
    submitting: 'Sending…',
  },
  simulate: {
    selected: 'Simulating position for {id}',
    pickDevice: 'Select a device to send a GPS point',
    latitude: 'Latitude',
    longitude: 'Longitude',
    submit: 'Send location',
    submitting: 'Sending…',
  },
  hud: {
    activeRefresh: '{count} active · refreshing every {seconds}s',
  },
  errors: {
    connection: 'Failed to connect to the API',
    addDevice: 'Failed to add device',
    sendLocation: 'Failed to send location',
    loadTrack: 'Failed to load track',
  },
  lang: {
    pl: 'PL',
    en: 'EN',
    switchLabel: 'Language',
  },
}
