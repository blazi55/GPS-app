import L from 'leaflet'
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png'
import markerIcon from 'leaflet/dist/images/marker-icon.png'
import markerShadow from 'leaflet/dist/images/marker-shadow.png'

import 'leaflet/dist/leaflet.css'

// Vite breaks Leaflet's default icon URLs — fix once at module load.
const DefaultIcon = L.icon({
  iconUrl: markerIcon,
  iconRetinaUrl: markerIcon2x,
  shadowUrl: markerShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41],
})

L.Marker.prototype.options.icon = DefaultIcon

export function deviceIcon(selected: boolean) {
  const color = selected ? '#0f766e' : '#1e3a5f'
  return L.divIcon({
    className: 'gps-marker',
    html: `<span style="
      display:block;
      width:14px;height:14px;
      border-radius:50%;
      background:${color};
      border:2px solid #fff;
      box-shadow:0 1px 4px rgba(15,23,42,.35);
    "></span>`,
    iconSize: [14, 14],
    iconAnchor: [7, 7],
  })
}
