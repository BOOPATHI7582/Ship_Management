import api from './axios'

export const DOCUMENT_CATEGORIES = [
  'INVOICE',
  'PACKING_LIST',
  'BILL_OF_LADING',
  'CUSTOMS_DOCUMENT',
  'EXPORT_DOCUMENT',
  'CERTIFICATE',
  'CONTRACT',
  'PROOF_OF_PAYMENT',
  'IMAGE',
  'OTHER',
]

export const fetchStaffDocuments = (params) => api.get('/manager/documents', { params })
export const uploadStaffDocument = (formData) => api.post('/manager/documents/upload', formData)
export const deleteDocument = (id) => api.delete(`/manager/documents/${id}`)

export const fetchMyDocuments = () => api.get('/client/documents')
export const fetchDocumentsForOwner = (ownerType, ownerId) =>
  api.get('/client/documents', { params: { ownerType, ownerId } })
export const uploadMyDocument = (formData) => api.post('/client/documents/upload', formData)

const documentUrl = (path) => api.get(path, { responseType: 'blob' })

/** Fetches a document as a Blob (handles the envelope-unwrapping interceptor). */
export async function fetchDocumentBlob(url) {
  const res = await documentUrl(url)
  return res instanceof Blob ? res : res.data
}

export function saveBlob(blob, filename) {
  const objectUrl = URL.createObjectURL(blob)
  const a = window.document.createElement('a')
  a.href = objectUrl
  a.download = filename || 'document'
  window.document.body.appendChild(a)
  a.click()
  a.remove()
  URL.revokeObjectURL(objectUrl)
}

export async function downloadDocument(url, filename) {
  try {
    const blob = await fetchDocumentBlob(url)
    saveBlob(blob, filename)
    return true
  } catch (err) {
    throw new Error(err.response?.data?.message || 'Download failed')
  }
}

export function formatBytes(bytes) {
  if (!bytes && bytes !== 0) return '—'
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

export function formatDate(value) {
  if (!value) return '—'
  return new Date(value).toLocaleDateString(undefined, { day: 'numeric', month: 'short', year: 'numeric' })
}
