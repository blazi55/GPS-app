import type {
  Device,
  LocationPoint,
  SendDevice,
  TrackSummary,
} from '../types/gps'

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    headers: {
      'Content-Type': 'application/json',
      ...(init?.headers ?? {}),
    },
    ...init,
  })

  if (!response.ok) {
    let message = `HTTP ${response.status}`
    try {
      const body = (await response.json()) as { message?: string }
      if (body.message) message = body.message
    } catch {
      /* ignore */
    }
    throw new Error(message)
  }

  if (response.status === 204 || response.status === 202) {
    return undefined as T
  }

  const text = await response.text()
  if (!text) return undefined as T
  return JSON.parse(text) as T
}

export const api = {
  getDevices: () => request<Device[]>('/device/get/all'),

  sendDevice: (payload: SendDevice) =>
    request<void>('/device/send', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),

  getLatestAll: () => request<LocationPoint[]>('/location/latest/all'),

  getLatest: (externalId: string) =>
    request<LocationPoint>(`/location/${encodeURIComponent(externalId)}/latest`),

  getHistory: (externalId: string) =>
    request<LocationPoint[]>(
      `/location/${encodeURIComponent(externalId)}/history`,
    ),

  getTrack: (externalId: string) =>
    request<TrackSummary>(
      `/location/${encodeURIComponent(externalId)}/track`,
    ),

  sendLocation: (payload: LocationPoint) =>
    request<void>('/location/send', {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
}
