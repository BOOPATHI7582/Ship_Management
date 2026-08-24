import toast from 'react-hot-toast'
import api from './axios'

// Manager
export const fetchQuotations = (params) => api.get('/manager/quotations', { params })
export const fetchQuotation = (id) => api.get(`/manager/quotations/${id}`)
export const createQuotation = (payload) => api.post('/manager/quotations', payload)
export const updateQuotation = (id, payload) => api.put(`/manager/quotations/${id}`, payload)
export const sendQuotation = (id) => api.post(`/manager/quotations/${id}/send`)
export const quotationPdfUrl = (id) => `/api/manager/quotations/${id}/pdf`

// Client
export const fetchMyQuotations = (params) => api.get('/client/quotations', { params })
export const fetchEnquiryQuotations = (enquiryId) =>
  api.get(`/client/quotations/enquiry/${enquiryId}`)
export const fetchClientQuotation = (id) => api.get(`/client/quotations/${id}`)
export const acceptQuotation = (id, reason = null) =>
  api.post(`/client/quotations/${id}/accept`, { decision: 'ACCEPT', reason })
export const rejectQuotation = (id, reason) =>
  api.post(`/client/quotations/${id}/reject`, { decision: 'REJECT', reason })
export const clientQuotationPdfUrl = (id) => `/api/client/quotations/${id}/pdf`

// Public secure-token link
export const fetchQuotationByToken = (token) => api.get(`/public/quotations/${token}`)
export const tokenQuotationPdfUrl = (token) => `/api/public/quotations/${token}/pdf`

// Authenticated PDF fetch (JWT header required, so plain links won't work).
// The axios interceptor unwraps responses; with responseType 'blob' the
// resolved value IS the Blob, so handle both shapes defensively.
// URL builders pass full "/api/..." paths while the instance already has
// baseURL "/api" - strip the prefix to avoid /api/api/... 404s.
async function fetchPdfBlob(url) {
  const path = url.replace(/^\/api(?=\/)/, '')
  const res = await api.get(path, { responseType: 'blob' })
  if (res instanceof Blob) return res
  return new Blob([res.data ?? res], { type: 'application/pdf' })
}

export async function downloadPdf(url, filename) {
  try {
    const blob = await fetchPdfBlob(url)
    const blobUrl = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = blobUrl
    link.download = filename
    document.body.appendChild(link)
    link.click()
    link.remove()
    URL.revokeObjectURL(blobUrl)
  } catch (err) {
    toast.error(err?.response?.data?.message || 'Could not download the PDF')
    throw err
  }
}

/**
 * Opens the document in a new browser tab. The window is opened
 * synchronously (before any await) so popup blockers don't kill it.
 */
export async function viewPdf(url) {
  const win = window.open('', '_blank')
  try {
    const blob = await fetchPdfBlob(url)
    const blobUrl = URL.createObjectURL(blob)
    if (win) {
      win.location.href = blobUrl
    } else {
      window.location.href = blobUrl
    }
    setTimeout(() => URL.revokeObjectURL(blobUrl), 60_000)
  } catch (err) {
    win?.close()
    toast.error(err?.response?.data?.message || 'Could not open the PDF')
  }
}
