import api from './axios'
import { downloadPdf as downloadBlob } from './quotations'

// Client
export const fetchMyReceipts = (params) => api.get('/client/receipts', { params })
export const clientReceiptPdfUrl = (id) => `/api/client/receipts/${id}/pdf`
export const downloadClientReceiptPdf = (id, receiptNo) =>
  downloadBlob(clientReceiptPdfUrl(id), `${receiptNo || 'receipt'}.pdf`)

// Manager / Admin
export const fetchAllReceipts = (params) => api.get('/manager/receipts', { params })
export const managerReceiptPdfUrl = (id) => `/api/manager/receipts/${id}/pdf`
export const downloadManagerReceiptPdf = (id, receiptNo) =>
  downloadBlob(managerReceiptPdfUrl(id), `${receiptNo || 'receipt'}.pdf`)

const METHOD_BADGES = {
  RAZORPAY: 'bg-blue-100 text-blue-800',
  NEFT: 'bg-emerald-100 text-emerald-800',
  RTGS: 'bg-emerald-100 text-emerald-800',
  UPI: 'bg-violet-100 text-violet-800',
  CHEQUE: 'bg-amber-100 text-amber-800',
  CASH: 'bg-amber-100 text-amber-800',
  WIRE_TRANSFER: 'bg-sky-100 text-sky-800',
}

export function methodBadge(method) {
  return METHOD_BADGES[method] || 'bg-navy-100 text-navy-700'
}
