import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { apiErrorMessage } from '../../api/axios'
import GoogleLoginButton from '../../components/GoogleLoginButton'

export default function LoginPage() {
  const { login, isAuthenticated, user } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const fallbackFor = (role) =>
    role === 'ADMIN' ? '/admin/dashboard'
      : role === 'SHIP_MANAGER' ? '/manager/enquiries' : '/dashboard'

  useEffect(() => {
    if (isAuthenticated && user) {
      navigate(location.state?.from || fallbackFor(user.role), { replace: true })
    }
  }, [isAuthenticated, user, navigate, location.state])

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      const result = await login(form.email.trim(), form.password)
      if (result.requiresOtp) {
        navigate('/login/otp', { state: { email: form.email.trim() } })
        return
      }
      navigate(location.state?.from || fallbackFor(result.user?.role), { replace: true })
    } catch (err) {
      setError(apiErrorMessage(err, 'Unable to log in. Please check your credentials.'))
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
        <div className="auth-card">
          <h1 className="font-display text-2xl font-bold text-white">Welcome back</h1>
          <p className="mt-1 text-sm text-white/60">Log in to manage your export operations.</p>
          {new URLSearchParams(window.location.search).get('expired') && (
            <p className="mt-4 rounded-lg border border-gold-500/40 bg-gold-500/10 px-4 py-2 text-sm text-gold-400">
              Your session expired. Please log in again.
            </p>
          )}
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
                autoComplete="email"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
                className="auth-input"
                placeholder="you@company.com"
              />
            </div>
            <div>
              <label htmlFor="password" className="block text-sm font-medium text-white/80">
                Password
              </label>
              <input
                id="password"
                type="password"
                required
                autoComplete="current-password"
                value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })}
                className="auth-input"
                placeholder="••••••••"
              />
            </div>
            <div className="flex justify-end">
              <Link to="/forgot-password" className="text-sm text-gold-400 hover:text-gold-500">
                Forgot password?
              </Link>
            </div>
            <button type="submit" disabled={submitting} className="btn-primary w-full">
              {submitting ? 'Logging in…' : 'Log In'}
            </button>
          </form>
          <div className="my-5 flex items-center gap-3">
            <span className="h-px flex-1 bg-white/10" />
            <span className="text-xs uppercase tracking-wider text-white/40">or</span>
            <span className="h-px flex-1 bg-white/10" />
          </div>
          <GoogleLoginButton />
          <p className="mt-6 text-center text-sm text-white/60">
            New to ExportPlatform?{' '}
            <Link to="/register" className="font-semibold text-gold-400 hover:text-gold-500">
              Create an account
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}