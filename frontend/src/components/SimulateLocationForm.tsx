import { useState, type FormEvent } from 'react'
import type { LocationPoint } from '../types/gps'

interface SimulateLocationFormProps {
  externalId: string | null
  onSubmit: (payload: LocationPoint) => Promise<void>
  busy: boolean
}

export function SimulateLocationForm({
  externalId,
  onSubmit,
  busy,
}: SimulateLocationFormProps) {
  const [latitude, setLatitude] = useState('52.2297')
  const [longitude, setLongitude] = useState('21.0122')

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    if (!externalId) return

    await onSubmit({
      deviceExternalId: externalId,
      latitude: Number(latitude),
      longitude: Number(longitude),
      timestamp: new Date().toISOString(),
    })
  }

  return (
    <form className="stack-form" onSubmit={handleSubmit}>
      <p className="muted small">
        {externalId
          ? `Symulacja pozycji dla ${externalId}`
          : 'Wybierz urządzenie, żeby wysłać punkt GPS'}
      </p>
      <label>
        Szerokość
        <input
          className="mono"
          value={latitude}
          onChange={(e) => setLatitude(e.target.value)}
          required
          disabled={!externalId}
        />
      </label>
      <label>
        Długość
        <input
          className="mono"
          value={longitude}
          onChange={(e) => setLongitude(e.target.value)}
          required
          disabled={!externalId}
        />
      </label>
      <button type="submit" disabled={!externalId || busy}>
        {busy ? 'Wysyłanie…' : 'Wyślij lokalizację'}
      </button>
    </form>
  )
}
