import { useState, type FormEvent } from 'react'
import { useI18n } from '../i18n/I18nProvider'
import { DEVICE_TYPES, type DeviceType, type SendDevice } from '../types/gps'

interface DeviceFormProps {
  onSubmit: (payload: SendDevice) => Promise<void>
  busy: boolean
}

export function DeviceForm({ onSubmit, busy }: DeviceFormProps) {
  const { t } = useI18n()
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
        {t.deviceForm.name}
        <input
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
          placeholder={t.deviceForm.namePlaceholder}
        />
      </label>
      <label>
        {t.deviceForm.externalId}
        <input
          value={externalId}
          onChange={(e) => setExternalId(e.target.value)}
          required
          placeholder={t.deviceForm.externalIdPlaceholder}
          className="mono"
        />
      </label>
      <label>
        {t.deviceForm.type}
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
        {busy ? t.deviceForm.submitting : t.deviceForm.submit}
      </button>
    </form>
  )
}
