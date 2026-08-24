import api from './axios'

export function verifyEmail(token) {
  return api.post('/auth/verify-email', { token })
}

export function resendVerification(email) {
  return api.post('/auth/resend-verification', { email })
}
