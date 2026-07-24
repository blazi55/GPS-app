import { useEffect } from 'react'
import {
  MapContainer,
  Marker,
  Polyline,
  Popup,
  TileLayer,
  useMap,
} from 'react-leaflet'
import type { Device, LocationPoint } from '../types/gps'
import { deviceIcon } from '../lib/leafletSetup'

interface MapViewProps {
  devices: Device[]
  latest: LocationPoint[]
  trackPoints: LocationPoint[]
  selectedExternalId: string | null
}

function FitBounds({
  latest,
  trackPoints,
  selectedExternalId,
}: {
  latest: LocationPoint[]
  trackPoints: LocationPoint[]
  selectedExternalId: string | null
}) {
  const map = useMap()

  useEffect(() => {
    const points =
      trackPoints.length > 0
        ? trackPoints
        : selectedExternalId
          ? latest.filter((p) => p.deviceExternalId === selectedExternalId)
          : latest

    if (points.length === 0) {
      map.setView([52.2297, 21.0122], 6)
      return
    }

    if (points.length === 1) {
      map.setView([points[0].latitude, points[0].longitude], 13)
      return
    }

    const bounds = points.map(
      (p) => [p.latitude, p.longitude] as [number, number],
    )
    map.fitBounds(bounds, { padding: [48, 48] })
  }, [latest, trackPoints, selectedExternalId, map])

  return null
}

export function MapView({
  devices,
  latest,
  trackPoints,
  selectedExternalId,
}: MapViewProps) {
  const deviceName = (externalId: string) =>
    devices.find((d) => d.externalId === externalId)?.name ?? externalId

  const trackLatLngs = trackPoints.map(
    (p) => [p.latitude, p.longitude] as [number, number],
  )

  return (
    <MapContainer
      center={[52.2297, 21.0122]}
      zoom={6}
      className="map-canvas"
      zoomControl={false}
    >
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OSM</a>'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      <FitBounds
        latest={latest}
        trackPoints={trackPoints}
        selectedExternalId={selectedExternalId}
      />

      {latest.map((point) => (
        <Marker
          key={point.deviceExternalId}
          position={[point.latitude, point.longitude]}
          icon={deviceIcon(point.deviceExternalId === selectedExternalId)}
        >
          <Popup>
            <strong>{deviceName(point.deviceExternalId)}</strong>
            <br />
            <span className="mono">
              {point.latitude.toFixed(5)}, {point.longitude.toFixed(5)}
            </span>
            <br />
            {new Date(point.timestamp).toLocaleString()}
          </Popup>
        </Marker>
      ))}

      {trackLatLngs.length > 1 && (
        <Polyline
          positions={trackLatLngs}
          pathOptions={{ color: '#0f766e', weight: 4, opacity: 0.85 }}
        />
      )}
    </MapContainer>
  )
}
