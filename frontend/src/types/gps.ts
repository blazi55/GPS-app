export type DeviceType = 'PHONE' | 'TABLET' | 'CAR' | 'DRONE' | 'WATCH'

export interface Device {
  id: number
  name: string
  deviceType: DeviceType
  externalId: string
}

export interface SendDevice {
  name: string
  externalId: string
  deviceType: DeviceType
}

export interface LocationPoint {
  deviceExternalId: string
  latitude: number
  longitude: number
  timestamp: string
}

export interface TrackSummary {
  deviceExternalId: string
  pointCount: number
  totalDistanceMeters: number
  from: string | null
  to: string | null
  points: LocationPoint[]
}

export interface ApiError {
  timestamp: string
  status: number
  message: string
}

export const DEVICE_TYPES: DeviceType[] = [
  'PHONE',
  'TABLET',
  'CAR',
  'DRONE',
  'WATCH',
]
