import api from './axios'

export function verifyEmail(token, email = '') {
  return api.post('/auth/verify-email', { token, email })
}

export function resendVerification(email) {
  return api.post('/auth/resend-verification', { email })
}
