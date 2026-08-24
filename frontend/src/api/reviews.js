import api from './axios'

export const createReview = (payload) => api.post('/client/reviews', payload)
export const fetchMyReviews = (params) => api.get('/client/reviews', { params })
export const hasReviewed = (shipmentId) =>
  api.get('/client/reviews/exists', { params: { shipmentId } })

export const fetchReviews = (params) => api.get('/manager/reviews', { params })
export const approveReview = (id) => api.post(`/manager/reviews/${id}/approve`)
export const rejectReview = (id) => api.post(`/manager/reviews/${id}/reject`)
export const fetchPendingCount = () => api.get('/manager/reviews/pending-count')
