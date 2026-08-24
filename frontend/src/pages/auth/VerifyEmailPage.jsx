import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { verifyEmail, resendVerification } from '../../api/verification'
import { apiErrorMessage } from '../../api/axios'

export default function VerifyEmailPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token') || ''

  const [state, setState] = useState('verifying')
  const [email, setEmail] = useState('')
  const [error, setError] = useState('')
  const [resent, setResent] = useState(false)
  const [resendEmail, setResendEmail] = useState('')

  useEffect(() => {
    if (!token) {
      setState('error')
      setError('This verification link is missing its token.')
      return
    }
    verifyEmail(token)
      .then((res) => {
        setEmail(res.data?.email || '')
        setState('success')
      })
      .catch((err) => {
        setError(apiErrorMessage(err, 'Unable to verify your email.'))
        setState('error')
      })
  }, [token])

  async function handleResend(e) {
    e.preventDefault()
    setResent(false)
    try {
      await resendVerification(resendEmail)
      setResent(true)
    } catch {
      setResent(true)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-navy-950 px-4 py-16">
      <div className="w-full max-w-md">
        <Link to="/" className="mb-8 block text-center font-display text-2xl font-bold text-white">
          Export<span className="text-gold-500">Platform</span>
        </Link>
        <div className="rounded-2xl border border-white/10 bg-white/5 p-8 text-center backdrop-blur">
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

          {state === 'error' && (
            <>
              <h1 className="font-display text-2xl font-bold text-white">Verification failed</h1>
              <p className="mt-3 rounded-lg border border-red-400/40 bg-red-400/10 px-4 py-2.5 text-sm text-red-300">
                {error}
              </p>
              <p className="mt-5 text-xs text-white/50">
                Links expire after 24 hours. Enter your email below and we will send a fresh one.
              </p>
              <form onSubmit={handleResend} className="mt-4 flex flex-col gap-3">
                <input
                  type="email"
                  required
                  value={resendEmail}
                  onChange={(e) => setResendEmail(e.target.value)}
                  placeholder="you@company.com"
                  className="w-full rounded-lg border border-white/15 bg-navy-900 px-4 py-2.5 text-white placeholder-white/30 focus:border-gold-500 focus:outline-none focus:ring-1 focus:ring-gold-500"
                />
                <button type="submit" className="btn-primary w-full">
                  Resend verification link
                </button>
                {resent && (
                  <p className="text-xs text-emerald-400">
                    If an unverified account exists for this email, a new link has been sent.
                  </p>
                )}
              </form>
              <Link to="/login" className="mt-5 inline-block text-sm text-white/60 transition hover:text-gold-400">
                Back to Login
              </Link>
            </>
          )}
        </div>
      </div>
    </div>
  )
}
