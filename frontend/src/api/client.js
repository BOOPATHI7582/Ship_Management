import api from './axios'

export async function fetchDashboardSummary() {
  return api.get('/client/dashboard')
}

export async function fetchClientProfile() {
  return api.get('/client/profile')
}

export async function updateClientProfile(payload) {
  return api.put('/client/profile', payload)
}

export async function createEnquiry(payload) {
  return api.post('/enquiries', payload)
}

export async function fetchMyEnquiries(page = 0, size = 10) {
  return api.get('/enquiries', { params: { page, size } })
}

export async function fetchEnquiry(id) {
  return api.get(`/enquiries/${id}`)
}

export async function fetchNotifications(page = 0, size = 10) {
  return api.get('/notifications', { params: { page, size } })
}

export async function markNotificationRead(id) {
  return api.put(`/notifications/${id}/read`)
}
