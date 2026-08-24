import { useState } from 'react'
import { Link } from 'react-router-dom'
import { forgotPassword } from '../../api/auth'
import { apiErrorMessage } from '../../api/axios'

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [sent, setSent] = useState(false)
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await forgotPassword(email.trim())
      setSent(true)
    } catch (err) {
      setError(apiErrorMessage(err, 'Unable to send reset link. Please try again.'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-navy-950 px-4 py-16">
      <div className="w-full max-w-md">
        <Link to="/" className="mb-8 block text-center font-display text-2xl font-bold text-white">
          Export<span className="text-gold-500">Platform</span>
        </Link>
        <div className="rounded-2xl border border-white/10 bg-white/5 p-8 backdrop-blur">
          <h1 className="font-display text-2xl font-bold text-white">Forgot password</h1>
          {sent ? (
            <>
              <p className="mt-4 text-sm text-white/70">
                If an account exists for <span className="font-semibold text-gold-400">{email}</span>, a
                password reset link has been sent. Check your inbox (and spam folder).
              </p>
              <p className="mt-3 rounded-lg border border-white/10 bg-navy-900/60 px-4 py-3 text-xs leading-relaxed text-white/50">
                Dev note: while SMTP is not configured on the server, reset links are printed to the
                backend console log.
              </p>
            </>
          ) : (
            <>
              <p className="mt-1 text-sm text-white/60">
                Enter your account email and we will send you a secure reset link.
              </p>
              {error && (
                <p className="mt-4 rounded-lg border border-red-400/40 bg-red-400/10 px-4 py-2 text-sm text-red-300">
                  {error}
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
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="mt-1.5 w-full rounded-lg border border-white/15 bg-navy-900 px-4 py-2.5 text-white placeholder-white/30 focus:border-gold-500 focus:outline-none focus:ring-1 focus:ring-gold-500"
                    placeholder="you@company.com"
                  />
                </div>
                <button type="submit" disabled={submitting} className="btn-primary w-full">
                  {submitting ? 'Sending…' : 'Send Reset Link'}
                </button>
              </form>
            </>
          )}
          <p className="mt-6 text-center text-sm text-white/60">
            Remembered it?{' '}
            <Link to="/login" className="font-semibold text-gold-400 hover:text-gold-500">
              Back to login
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
