import api from './axios'

export const fetchTaxRates = (params) => api.get('/admin/tax-rates', { params })
export const fetchTaxRate = (id) => api.get(`/admin/tax-rates/${id}`)
export const createTaxRate = (payload) => api.post('/admin/tax-rates', payload)
export const updateTaxRate = (id, payload) => api.put(`/admin/tax-rates/${id}`, payload)
export const toggleTaxRate = (id, active) =>
  api.put(`/admin/tax-rates/${id}/toggle?active=${active}`)
export const deleteTaxRate = (id) => api.delete(`/admin/tax-rates/${id}`)

export const TAX_TYPES = ['CGST', 'SGST', 'IGST', 'EXEMPT', 'ZERO_RATED', 'CUSTOM']

export const emptyTaxRateForm = {
  id: null,
  name: '',
  taxType: 'IGST',
  rate: '',
  country: 'India',
  jurisdiction: '',
  effectiveFrom: new Date().toISOString().slice(0, 10),
  active: true,
  description: '',
}
