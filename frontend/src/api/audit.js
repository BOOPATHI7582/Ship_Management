import api from './axios'

export function fetchAuditLog(params) {
  return api.get('/manager/audit', { params })
}
