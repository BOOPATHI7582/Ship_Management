import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { verifyEmail, resendVerification } from '../../api/verification'
import { apiErrorMessage } from '../../api/axios'
import OtpInput from '../../components/OtpInput'

export default function VerifyEmailPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token') || ''

  const [state, setState] = useState(token ? 'verifying' : 'code')
  const [email, setEmail] = useState('')
  const [otp, setOtp] = useState('')
  const [error, setError] = useState('')
  const [verifying, setVerifying] = useState(Boolean(token))
  const [resent, setResent] = useState(false)

  useEffect(() => {
    if (!token || state === 'success') return
    let cancelled = false
    verifyEmail(token)
      .then((res) => {
        if (cancelled) return
        setEmail(res.data?.email || '')
        setState('success')
      })
      .catch(() => {
        if (cancelled) return
        setState('code')
      })
      .finally(() => {
        if (!cancelled) setVerifying(false)
      })
    return () => {
      cancelled = true
    }
  }, [token, state])

  async function handleVerify(e) {
    e.preventDefault()
    setError('')
    if (otp.length < 6) {
      setError('Enter the 6-digit verification code.')
      return
    }
    setVerifying(true)
    try {
      const res = await verifyEmail(token, email.trim().toLowerCase())
      setEmail(res.data?.email || email.trim())
      setOtp('')
      setState('success')
    } catch (err) {
      setError(apiErrorMessage(err, 'That code is invalid or has expired. Request a new one.'))
      setOtp('')
    } finally {
      setVerifying(false)
    }
  }

  async function handleResend(e) {
    e.preventDefault()
    if (!email) return
    setError('')
    setResent(false)
    try {
      await resendVerification(email.trim().toLowerCase())
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
        <div className="auth-card text-center">
          {state === 'verifying' && (
            <>
              <h1 className="font-display text-2xl font-bold text-white">Verifying…</h1>
              <p className="mt-3 text-sm text-white/60">Checking your verification link.</p>
            </>
          )}

          {state === 'success' && (
            <>
              <div className="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-emerald-500/20">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" className="h-6 w-6 text-emerald-400">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                </svg>
              </div>
              <h1 className="font-display text-2xl font-bold text-white">Email verified</h1>
              <p className="mt-3 text-sm leading-relaxed text-white/70">
                {email && <>Your account <span className="font-semibold text-gold-400">{email}</span> is now active. </>}
                You can log in with your credentials.
              </p>
              <Link to="/login" state={{ from: '/dashboard' }} className="btn-primary mt-6 inline-block w-full">
                Go to Login
              </Link>
            </>
          )}

          {state === 'code' && (
            <>
              <h1 className="font-display text-2xl font-bold text-white">Enter your code</h1>
              <p className="mt-1 text-sm leading-relaxed text-white/60">
                We sent a 6-digit verification code to your email. Enter it below to activate your
                account.
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
              <form onSubmit={handleVerify} className="mt-6 space-y-5 text-left">
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
                    <OtpInput value={otp} onChange={setOtp} disabled={verifying} />
                  </div>
                  <div className="mt-2 text-center text-xs text-white/40">
                    Codes expire after 24 hours.
                  </div>
                </div>
                <button type="submit" disabled={verifying || otp.length < 6} className="btn-primary w-full">
                  {verifying ? 'Verifying…' : 'Verify Email'}
                </button>
              </form>
              <form onSubmit={handleResend} className="mt-4">
                <button type="submit" className="text-sm font-semibold text-gold-400 hover:text-gold-500">
                  Resend code
                </button>
              </form>
              <Link to="/login" className="mt-4 inline-block text-sm text-white/60 transition hover:text-gold-400">
                Back to Login
              </Link>
            </>
          )}
        </div>
      </div>
    </div>
  )
}