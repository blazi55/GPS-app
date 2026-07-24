import type { TrackSummary } from '../types/gps'

interface TrackInfoProps {
  track: TrackSummary | null
  loading: boolean
  onLoadTrack: () => void
  onClearTrack: () => void
  hasSelection: boolean
}

function formatDistance(meters: number): string {
  if (meters >= 1000) return `${(meters / 1000).toFixed(2)} km`
  return `${meters.toFixed(0)} m`
}

export function TrackInfo({
  track,
  loading,
  onLoadTrack,
  onClearTrack,
  hasSelection,
}: TrackInfoProps) {
  return (
    <div className="track-info">
      <div className="track-info__actions">
        <button
          type="button"
          className="secondary"
          disabled={!hasSelection || loading}
          onClick={onLoadTrack}
        >
          {loading ? 'Ładowanie…' : 'Pokaż trasę'}
        </button>
        {track && (
          <button type="button" className="ghost" onClick={onClearTrack}>
            Wyczyść
          </button>
        )}
      </div>

      {track && (
        <dl className="stats">
          <div>
            <dt>Punkty</dt>
            <dd className="mono">{track.pointCount}</dd>
          </div>
          <div>
            <dt>Dystans</dt>
            <dd className="mono">{formatDistance(track.totalDistanceMeters)}</dd>
          </div>
        </dl>
      )}
    </div>
  )
}
