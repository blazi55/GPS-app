import { useCallback, useEffect, useMemo, useState } from 'react'
import { api } from './api/client'
import { DeviceForm } from './components/DeviceForm'
import { DeviceList } from './components/DeviceList'
import { MapView } from './components/MapView'
import { SimulateLocationForm } from './components/SimulateLocationForm'
import { TrackInfo } from './components/TrackInfo'
import './lib/leafletSetup'
import type {
  Device,
  LocationPoint,
  SendDevice,
  TrackSummary,
} from './types/gps'

const POLL_MS = 5000

export default function App() {
  const [devices, setDevices] = useState<Device[]>([])
  const [latest, setLatest] = useState<LocationPoint[]>([])
  const [selectedExternalId, setSelectedExternalId] = useState<string | null>(
    null,
  )
  const [track, setTrack] = useState<TrackSummary | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busyDevice, setBusyDevice] = useState(false)
  const [busyLocation, setBusyLocation] = useState(false)
  const [busyTrack, setBusyTrack] = useState(false)

  const refresh = useCallback(async () => {
    try {
      const [deviceList, latestList] = await Promise.all([
        api.getDevices(),
        api.getLatestAll(),
      ])
      setDevices(deviceList)
      setLatest(latestList)
      setError(null)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Błąd połączenia z API')
    }
  }, [])

  useEffect(() => {
    void refresh()
    const id = window.setInterval(() => void refresh(), POLL_MS)
    return () => window.clearInterval(id)
  }, [refresh])

  const latestByDevice = useMemo(() => {
    const map: Record<string, string> = {}
    for (const point of latest) {
      map[point.deviceExternalId] = point.timestamp
    }
    return map
  }, [latest])

  async function handleAddDevice(payload: SendDevice) {
    setBusyDevice(true)
    try {
      await api.sendDevice(payload)
      // RabbitMQ is async — short delay then refresh
      await new Promise((r) => setTimeout(r, 800))
      await refresh()
      setSelectedExternalId(payload.externalId)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Nie udało się dodać urządzenia')
    } finally {
      setBusyDevice(false)
    }
  }

  async function handleSendLocation(payload: LocationPoint) {
    setBusyLocation(true)
    try {
      await api.sendLocation(payload)
      await new Promise((r) => setTimeout(r, 800))
      await refresh()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Nie udało się wysłać lokalizacji')
    } finally {
      setBusyLocation(false)
    }
  }

  async function handleLoadTrack() {
    if (!selectedExternalId) return
    setBusyTrack(true)
    try {
      const summary = await api.getTrack(selectedExternalId)
      setTrack(summary)
      setError(null)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Nie udało się pobrać trasy')
    } finally {
      setBusyTrack(false)
    }
  }

  function handleSelect(externalId: string) {
    setSelectedExternalId(externalId)
    setTrack(null)
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <header className="brand">
          <p className="brand__mark">GPS Track</p>
          <p className="brand__tag">Monitoring floty w czasie rzeczywistym</p>
        </header>

        {error && <div className="banner error">{error}</div>}

        <section className="panel">
          <h2>Urządzenia</h2>
          <DeviceList
            devices={devices}
            selectedExternalId={selectedExternalId}
            latestByDevice={latestByDevice}
            onSelect={handleSelect}
          />
        </section>

        <section className="panel">
          <h2>Trasa</h2>
          <TrackInfo
            track={track}
            loading={busyTrack}
            hasSelection={Boolean(selectedExternalId)}
            onLoadTrack={handleLoadTrack}
            onClearTrack={() => setTrack(null)}
          />
        </section>

        <section className="panel">
          <h2>Nowe urządzenie</h2>
          <DeviceForm onSubmit={handleAddDevice} busy={busyDevice} />
        </section>

        <section className="panel">
          <h2>Symulacja GPS</h2>
          <SimulateLocationForm
            externalId={selectedExternalId}
            onSubmit={handleSendLocation}
            busy={busyLocation}
          />
        </section>
      </aside>

      <main className="map-stage">
        <MapView
          devices={devices}
          latest={latest}
          trackPoints={track?.points ?? []}
          selectedExternalId={selectedExternalId}
        />
        <div className="map-hud">
          <span className="mono">
            {latest.length} aktywnych · odświeżanie co {POLL_MS / 1000}s
          </span>
        </div>
      </main>
    </div>
  )
}
