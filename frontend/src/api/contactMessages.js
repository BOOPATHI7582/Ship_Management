import api from './axios'

export function fetchContactMessages(params) {
  return api.get('/manager/contact', { params })
}

export function fetchUnreadCount() {
  return api.get('/manager/contact/unread-count')
}

export function markHandled(id) {
  return api.post(`/manager/contact/${id}/handle`)
}

export function reopenMessage(id) {
  return api.post(`/manager/contact/${id}/reopen`)
}

export function fetchMyMessages(params) {
  return api.get('/client/contact-messages', { params })
}
