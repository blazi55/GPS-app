import { useState, type FormEvent } from 'react'
import { useI18n } from '../i18n/I18nProvider'
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
  const { t, format } = useI18n()
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
          ? format(t.simulate.selected, { id: externalId })
          : t.simulate.pickDevice}
      </p>
      <label>
        {t.simulate.latitude}
        <input
          className="mono"
          value={latitude}
          onChange={(e) => setLatitude(e.target.value)}
          required
          disabled={!externalId}
        />
      </label>
      <label>
        {t.simulate.longitude}
        <input
          className="mono"
          value={longitude}
          onChange={(e) => setLongitude(e.target.value)}
          required
          disabled={!externalId}
        />
      </label>
      <button type="submit" disabled={!externalId || busy}>
        {busy ? t.simulate.submitting : t.simulate.submit}
      </button>
    </form>
  )
}
