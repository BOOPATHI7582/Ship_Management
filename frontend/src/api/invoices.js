import api from './axios'
import { downloadPdf as downloadBlob } from './quotations'

// Manager
export const fetchInvoices = (params) => api.get('/manager/invoices', { params })
export const fetchInvoice = (id) => api.get(`/manager/invoices/${id}`)
export const issueInvoice = (payload) => api.post('/manager/invoices', payload)
export const sendInvoice = (id) => api.post(`/manager/invoices/${id}/send`)
export const cancelInvoice = (id) => api.post(`/manager/invoices/${id}/cancel`)
export const managerInvoicePdfUrl = (id) => `/api/manager/invoices/${id}/pdf`
export const downloadManagerInvoicePdf = (id, invoiceNo) =>
  downloadBlob(managerInvoicePdfUrl(id), `${invoiceNo || 'tax-invoice'}.pdf`)

// Client
export const fetchMyInvoices = (params) => api.get('/client/invoices', { params })
export const fetchMyInvoice = (id) => api.get(`/client/invoices/${id}`)
export const clientInvoicePdfUrl = (id) => `/api/client/invoices/${id}/pdf`
export const downloadClientInvoicePdf = (id, invoiceNo) =>
  downloadBlob(clientInvoicePdfUrl(id), `${invoiceNo || 'tax-invoice'}.pdf`)

export const INVOICE_STATUSES = ['ISSUED', 'PARTIALLY_PAID', 'PAID', 'OVERDUE', 'CANCELLED']

export function money(amount, currency = 'INR') {
  const n = Number(amount ?? 0)
  return `${currency} ${n.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

export function formatDate(value) {
  if (!value) return '—'
  return new Date(value).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })
}
