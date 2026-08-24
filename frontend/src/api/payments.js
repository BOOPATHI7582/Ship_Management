import api from './axios'

export const createOrder = (invoiceId) =>
  api.post('/client/payments/create-order', { invoiceId })

export const verifyPayment = (payload) =>
  api.post('/client/payments/verify', payload)

export const fetchMyPayments = ({ page = 0, size = 10 } = {}) =>
  api.get('/client/payments', { params: { page, size } })

export const fetchPayments = ({ page = 0, size = 10 } = {}) =>
  api.get('/manager/payments', { params: { page, size } })

export const recordOfflinePayment = (payload) =>
  api.post('/manager/payments/offline', payload)

export const money = (value, currency) => {
  if (value === null || value === undefined) return '—'
  try {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: currency || 'INR',
      maximumFractionDigits: 2,
    }).format(value)
  } catch {
    return String(value)
  }
}
