import { useState, type FormEvent } from 'react'
import { DEVICE_TYPES, type DeviceType, type SendDevice } from '../types/gps'

interface DeviceFormProps {
  onSubmit: (payload: SendDevice) => Promise<void>
  busy: boolean
}

export function DeviceForm({ onSubmit, busy }: DeviceFormProps) {
  const [name, setName] = useState('')
  const [externalId, setExternalId] = useState('')
  const [deviceType, setDeviceType] = useState<DeviceType>('PHONE')

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    await onSubmit({ name, externalId, deviceType })
    setName('')
    setExternalId('')
    setDeviceType('PHONE')
  }

  return (
    <form className="stack-form" onSubmit={handleSubmit}>
      <label>
        Nazwa
        <input
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
          placeholder="Flota-01"
        />
      </label>
      <label>
        External ID
        <input
          value={externalId}
          onChange={(e) => setExternalId(e.target.value)}
          required
          placeholder="car-01"
          className="mono"
        />
      </label>
      <label>
        Typ
        <select
          value={deviceType}
          onChange={(e) => setDeviceType(e.target.value as DeviceType)}
        >
          {DEVICE_TYPES.map((type) => (
            <option key={type} value={type}>
              {type}
            </option>
          ))}
        </select>
      </label>
      <button type="submit" disabled={busy}>
        {busy ? 'Wysyłanie…' : 'Dodaj urządzenie'}
      </button>
    </form>
  )
}
