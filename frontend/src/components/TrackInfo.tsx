import { useI18n } from '../i18n/I18nProvider'
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
  const { t } = useI18n()

  return (
    <div className="track-info">
      <div className="track-info__actions">
        <button
          type="button"
          className="secondary"
          disabled={!hasSelection || loading}
          onClick={onLoadTrack}
        >
          {loading ? t.track.loading : t.track.show}
        </button>
        {track && (
          <button type="button" className="ghost" onClick={onClearTrack}>
            {t.track.clear}
          </button>
        )}
      </div>

      {track && (
        <dl className="stats">
          <div>
            <dt>{t.track.points}</dt>
            <dd className="mono">{track.pointCount}</dd>
          </div>
          <div>
            <dt>{t.track.distance}</dt>
            <dd className="mono">{formatDistance(track.totalDistanceMeters)}</dd>
          </div>
        </dl>
      )}
    </div>
  )
}
