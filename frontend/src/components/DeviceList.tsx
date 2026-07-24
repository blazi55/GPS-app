import type { Device } from '../types/gps'
import { useI18n } from '../i18n/I18nProvider'

interface DeviceListProps {
  devices: Device[]
  selectedExternalId: string | null
  latestByDevice: Record<string, string>
  onSelect: (externalId: string) => void
}

export function DeviceList({
  devices,
  selectedExternalId,
  latestByDevice,
  onSelect,
}: DeviceListProps) {
  const { t, locale } = useI18n()

  if (devices.length === 0) {
    return <p className="muted">{t.devices.empty}</p>
  }

  return (
    <ul className="device-list">
      {devices.map((device) => {
        const selected = device.externalId === selectedExternalId
        return (
          <li key={device.id}>
            <button
              type="button"
              className={`device-item ${selected ? 'selected' : ''}`}
              onClick={() => onSelect(device.externalId)}
            >
              <span className="device-item__name">{device.name}</span>
              <span className="device-item__meta">
                <span className="badge">{device.deviceType}</span>
                <span className="mono">{device.externalId}</span>
              </span>
              {latestByDevice[device.externalId] && (
                <span className="device-item__time muted">
                  {new Date(latestByDevice[device.externalId]).toLocaleString(
                    locale,
                  )}
                </span>
              )}
            </button>
          </li>
        )
      })}
    </ul>
  )
}
