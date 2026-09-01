import api from './axios'

export async function registerUser(payload) {
  return api.post('/auth/register', payload)
}

export async function loginUser(payload) {
  return api.post('/auth/login', payload)
}

export async function loginOtp(payload) {
  return api.post('/auth/login/otp', payload)
}

export async function resendLoginOtp(email) {
  return api.post('/auth/login/otp/resend', { email })
}

export async function googleLoginUser(payload) {
  return api.post('/auth/google', payload)
}

export async function fetchCurrentUser() {
  return api.get('/auth/me')
}

export async function forgotPassword(email) {
  return api.post('/auth/forgot-password', { email })
}

export async function resetPassword(token, newPassword) {
  return api.post('/auth/reset-password', { token, newPassword })
}
