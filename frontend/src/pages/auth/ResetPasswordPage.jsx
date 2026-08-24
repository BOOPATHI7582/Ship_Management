import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { resetPassword } from '../../api/auth'
import { apiErrorMessage } from '../../api/axios'

export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const [token, setToken] = useState(searchParams.get('token') || '')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    if (newPassword !== confirmPassword) {
      setError('Passwords do not match.')
      return
    }
    if (newPassword.length < 8) {
      setError('Password must be at least 8 characters long.')
      return
    }
    setSubmitting(true)
    try {
      await resetPassword(token.trim(), newPassword)
      navigate('/login', { state: { resetSuccess: true }, replace: true })
    } catch (err) {
      setError(apiErrorMessage(err, 'Unable to reset password. The link may have expired.'))
    } finally {
      setSubmitting(false)
    }
  }

  const inputClass =
    'mt-1.5 w-full rounded-lg border border-white/15 bg-navy-900 px-4 py-2.5 text-white placeholder-white/30 focus:border-gold-500 focus:outline-none focus:ring-1 focus:ring-gold-500'

  return (
    <div className="flex min-h-screen items-center justify-center bg-navy-950 px-4 py-16">
      <div className="w-full max-w-md">
        <Link to="/" className="mb-8 block text-center font-display text-2xl font-bold text-white">
          Export<span className="text-gold-500">Platform</span>
        </Link>
        <div className="rounded-2xl border border-white/10 bg-white/5 p-8 backdrop-blur">
          <h1 className="font-display text-2xl font-bold text-white">Set a new password</h1>
          <p className="mt-1 text-sm text-white/60">
            Paste your reset code and choose a new password.
          </p>
          {error && (
            <p className="mt-4 rounded-lg border border-red-400/40 bg-red-400/10 px-4 py-2 text-sm text-red-300">
              {error}
            </p>
          )}
          <form onSubmit={handleSubmit} className="mt-6 space-y-5">
            <div>
              <label htmlFor="token" className="block text-sm font-medium text-white/80">
                Reset Code
              </label>
              <input
                id="token"
                type="text"
                required
                value={token}
                onChange={(e) => setToken(e.target.value)}
                className={inputClass}
                placeholder="From the email we sent you"
              />
            </div>
            <div>
              <label htmlFor="newPassword" className="block text-sm font-medium text-white/80">
                New Password
              </label>
              <input
                id="newPassword"
                type="password"
                required
                minLength={8}
                autoComplete="new-password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                className={inputClass}
                placeholder="••••••••"
              />
            </div>
            <div>
              <label htmlFor="confirmPassword" className="block text-sm font-medium text-white/80">
                Confirm New Password
              </label>
              <input
                id="confirmPassword"
                type="password"
                required
                minLength={8}
                autoComplete="new-password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                className={inputClass}
                placeholder="••••••••"
              />
            </div>
            <button type="submit" disabled={submitting} className="btn-primary w-full">
              {submitting ? 'Updating…' : 'Update Password'}
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}
