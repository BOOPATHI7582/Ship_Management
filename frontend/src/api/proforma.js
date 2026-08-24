import api from './axios'
import { downloadPdf as downloadBlob } from './quotations'

// Manager
export const fetchProformas = (params) => api.get('/manager/proforma-invoices', { params })
export const fetchProforma = (id) => api.get(`/manager/proforma-invoices/${id}`)
export const createProforma = (payload) => api.post('/manager/proforma-invoices', payload)
export const updateProforma = (id, payload) => api.put(`/manager/proforma-invoices/${id}`, payload)
export const sendProforma = (id) => api.post(`/manager/proforma-invoices/${id}/send`)
export const cancelProforma = (id) => api.post(`/manager/proforma-invoices/${id}/cancel`)
export const managerProformaPdfUrl = (id) => `/api/manager/proforma-invoices/${id}/pdf`
export const downloadManagerProformaPdf = (id, piNo) =>
  downloadBlob(managerProformaPdfUrl(id), `${piNo || 'proforma-invoice'}.pdf`)

// Client
export const fetchMyProformas = (params) => api.get('/client/proforma-invoices', { params })
export const fetchMyProforma = (id) => api.get(`/client/proforma-invoices/${id}`)
export const clientProformaPdfUrl = (id) => `/api/client/proforma-invoices/${id}/pdf`
export const downloadClientProformaPdf = (id, piNo) =>
  downloadBlob(clientProformaPdfUrl(id), `${piNo || 'proforma-invoice'}.pdf`)

export const PROFORMA_STATUSES = [
  'DRAFT',
  'SENT',
  'PAYMENT_PENDING',
  'ADVANCE_PAID',
  'CONVERTED',
  'CANCELLED',
  'EXPIRED',
]

export function money(amount, currency = 'INR') {
  const n = Number(amount ?? 0)
  return `${currency} ${n.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

export function formatDate(value) {
  if (!value) return '—'
  return new Date(value).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })
}
