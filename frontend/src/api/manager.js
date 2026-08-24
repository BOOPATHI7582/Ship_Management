import api from './axios'

export const fetchManagerEnquiries = (params) => api.get('/manager/enquiries', { params })
export const fetchManagerEnquiry = (id) => api.get(`/manager/enquiries/${id}`)
export const updateEnquiryStatus = (id, status) =>
  api.put(`/manager/enquiries/${id}/status`, { status })
export const fetchThreadAsManager = (enquiryId) =>
  api.get(`/manager/enquiries/${enquiryId}/negotiation`)
export const sendOffer = (enquiryId, payload) =>
  api.post(`/manager/enquiries/${enquiryId}/offers`, payload)

export const fetchThreadAsClient = (enquiryId) =>
  api.get(`/negotiations/enquiry/${enquiryId}`)
export const replyToThread = (enquiryId, payload) =>
  api.post(`/negotiations/enquiry/${enquiryId}/messages`, payload)
export const acceptOffer = (messageId) => api.post(`/negotiations/messages/${messageId}/accept`)
export const rejectOffer = (messageId) => api.post(`/negotiations/messages/${messageId}/reject`)
