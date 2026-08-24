import api from './axios'

export const fetchShipments = (params) => api.get('/manager/shipments', { params })
export const createShipment = (payload) => api.post('/manager/shipments', payload)
export const fetchShipment = (id) => api.get(`/manager/shipments/${id}`)
export const updateShipment = (id, payload) => api.put(`/manager/shipments/${id}`, payload)
export const addShipmentProgress = (id, payload) => api.post(`/manager/shipments/${id}/progress`, payload)

export const fetchMyShipments = (params) => api.get('/client/shipments', { params })
export const fetchMyShipment = (id) => api.get(`/client/shipments/${id}`)

export const SHIPMENT_STATUSES = [
  'BOOKING_CONFIRMED',
  'CARGO_PREPARATION',
  'LOADING',
  'LOADING_COMPLETED',
  'DEPARTED',
  'IN_TRANSIT',
  'NEAR_DESTINATION',
  'ARRIVED',
  'UNLOADING',
  'DELIVERED',
  'COMPLETED',
]

export const statusLabel = (status) => (status || '').replace(/_/g, ' ')

export function statusBadge(status) {
  switch (status) {
    case 'BOOKING_CONFIRMED': return 'bg-blue-100 text-blue-800'
    case 'CARGO_PREPARATION': return 'bg-indigo-100 text-indigo-800'
    case 'LOADING':
    case 'LOADING_COMPLETED': return 'bg-violet-100 text-violet-800'
    case 'DEPARTED': return 'bg-cyan-100 text-cyan-800'
    case 'IN_TRANSIT': return 'bg-sky-100 text-sky-800'
    case 'NEAR_DESTINATION': return 'bg-teal-100 text-teal-800'
    case 'ARRIVED': return 'bg-emerald-100 text-emerald-800'
    case 'UNLOADING': return 'bg-lime-100 text-lime-800'
    case 'DELIVERED': return 'bg-green-100 text-green-800'
    case 'COMPLETED': return 'bg-slate-200 text-slate-700'
    default: return 'bg-gray-100 text-gray-700'
  }
}

/** Progress percentage across the 11-state lifecycle (0..100). */
export function statusPercent(status) {
  const idx = SHIPMENT_STATUSES.indexOf(status)
  if (idx < 0) return 0
  return Math.round((idx / (SHIPMENT_STATUSES.length - 1)) * 100)
}
