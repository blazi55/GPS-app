import type { Device } from '../types/gps'

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
  if (devices.length === 0) {
    return <p className="muted">Brak urządzeń — dodaj pierwsze poniżej.</p>
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
                  {new Date(latestByDevice[device.externalId]).toLocaleString()}
                </span>
              )}
            </button>
          </li>
        )
      })}
    </ul>
  )
}
