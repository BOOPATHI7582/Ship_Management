import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import OtpInput from '../../components/OtpInput'
import { useAuth } from '../../context/AuthContext'
import { apiErrorMessage } from '../../api/axios'
import { resendLoginOtp } from '../../api/auth'

export default function VerifyLoginOtpPage() {
  const { finalizeLogin } = useAuth()
  const navigate = useNavigate()
  const { state } = useLocation()
  const [email, setEmail] = useState(state?.email || '')
  const [otp, setOtp] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [resent, setResent] = useState(false)

  function fallbackFor(user) {
    return user?.role === 'ADMIN'
      ? '/admin/dashboard'
      : user?.role === 'SHIP_MANAGER'
        ? '/manager/enquiries'
        : '/dashboard'
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    if (otp.length < 6) {
      setError('Enter the 6-digit code sent to your email.')
      return
    }
    setSubmitting(true)
    try {
      const user = await finalizeLogin(email.trim().toLowerCase(), otp)
      navigate(fallbackFor(user), { replace: true })
    } catch (err) {
      setError(apiErrorMessage(err, 'That code is invalid or has expired. Try again.'))
      setOtp('')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleResend(e) {
    e.preventDefault()
    if (!email) return
    setError('')
    setResent(false)
    try {
      await resendLoginOtp(email.trim().toLowerCase())
      setResent(true)
    } catch (err) {
      setError(apiErrorMessage(err, 'Unable to resend the code.'))
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-navy-950 px-4 py-16">
      <div className="w-full max-w-md">
        <Link to="/" className="mb-8 block text-center font-display text-2xl font-bold text-white">
          Export<span className="text-gold-500">Platform</span>
        </Link>
        <div className="auth-card">
          <h1 className="font-display text-2xl font-bold text-white">Verify it&apos;s you</h1>
          <p className="mt-1 text-sm text-white/60">
            Enter the 6-digit code we sent to your email to finish signing in.
          </p>
          {error && (
            <p className="mt-4 rounded-lg border border-red-400/40 bg-red-400/10 px-4 py-2 text-sm text-red-300">
              {error}
            </p>
          )}
          {resent && (
            <p className="mt-4 rounded-lg border border-emerald-400/40 bg-emerald-400/10 px-4 py-2 text-sm text-emerald-300">
              A fresh code is on its way.
            </p>
          )}
          <form onSubmit={handleSubmit} className="mt-6 space-y-5">
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-white/80">
                Email
              </label>
              <input
                id="email"
                type="email"
                required
                autoComplete="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="auth-input"
                placeholder="you@company.com"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-white/80">Verification code</label>
              <div className="mt-2" data-testid="otp">
                <OtpInput value={otp} onChange={setOtp} disabled={submitting} />
              </div>
              <div className="mt-2 flex items-center justify-between text-xs">
                <span className="text-white/40">Codes expire after 10 minutes.</span>
                <button
                  type="button"
                  onClick={handleResend}
                  className="font-semibold text-gold-400 hover:text-gold-500"
                >
                  Resend code
                </button>
              </div>
            </div>
            {devOtp && (
              <div className="rounded-lg border border-amber-300/30 bg-amber-400/10 p-3 text-center text-xs text-amber-200">
                Mail delivery is disabled locally. Dev code:{' '}
                <span className="font-mono text-sm font-bold tracking-widest">{devOtp}</span>
              </div>
            )}
            <button type="submit" disabled={submitting || otp.length < 6} className="btn-primary w-full">
              {submitting ? 'Verifying…' : 'Verify & Log In'}
            </button>
          </form>
          <p className="mt-6 text-center text-sm text-white/60">
            Changed your mind?{' '}
            <Link to="/login" className="font-semibold text-gold-400 hover:text-gold-500">
              Back to Login
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}