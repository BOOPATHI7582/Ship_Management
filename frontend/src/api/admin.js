import api from './axios'

// Users & clients
export const fetchAdminUsers = (params) => api.get('/admin/users', { params })
export const updateAdminUser = (id, payload) => api.put(`/admin/users/${id}`, payload)
export const fetchAdminClients = (params) => api.get('/admin/clients', { params })

// Vessels
export const fetchVessels = (params) => api.get('/admin/vessels', { params })
export const createVessel = (payload) => api.post('/admin/vessels', payload)
export const updateVessel = (id, payload) => api.put(`/admin/vessels/${id}`, payload)

// Cargo lots
export const fetchAdminCargo = (params) => api.get('/admin/cargo', { params })
export const createCargoLot = (payload) => api.post('/admin/cargo', payload)
export const updateCargoLot = (id, payload) => api.put(`/admin/cargo/${id}`, payload)

// Categories
export const fetchAllCategories = () => api.get('/admin/categories')
export const createCategory = (payload) => api.post('/admin/categories', payload)
export const updateCategory = (id, payload) => api.put(`/admin/categories/${id}`, payload)

// Ports
export const fetchAllPorts = () => api.get('/admin/ports')
export const createPort = (payload) => api.post('/admin/ports', payload)
export const updatePort = (id, payload) => api.put(`/admin/ports/${id}`, payload)
