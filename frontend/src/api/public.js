import api from './axios'

export async function fetchAvailableCargo() {
  return api.get('/public/cargo')
}

export async function fetchCargoCategories() {
  return api.get('/public/cargo-categories')
}

export async function fetchPorts() {
  return api.get('/public/ports')
}

export async function trackShipment(shipmentRef) {
  return api.get(`/public/tracking/${encodeURIComponent(shipmentRef)}`)
}

export async function fetchStats() {
  return api.get('/public/stats')
}

export async function fetchReviews() {
  return api.get('/public/reviews')
}

export async function submitContactMessage(payload) {
  return api.post('/contact', payload)
}
