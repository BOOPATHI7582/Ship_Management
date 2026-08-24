import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { apiErrorMessage } from '../../api/axios'

export default function RegisterPage() {
  const { register, isAuthenticated, user } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
    confirmPassword: '',
    companyName: '',
    phone: '',
    country: '',
  })
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [pendingEmail, setPendingEmail] = useState(null)
  const [devLink, setDevLink] = useState(null)

  useEffect(() => {
    if (isAuthenticated && user) {
      navigate('/dashboard', { replace: true })
    }
  }, [isAuthenticated, user, navigate])

  function update(field) {
    return (e) => setForm({ ...form, [field]: e.target.value })
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match.')
      return
    }
    if (form.password.length < 8) {
      setError('Password must be at least 8 characters long.')
      return
    }
    setSubmitting(true)
    try {
      const result = await register({
        fullName: form.fullName,
        email: form.email,
        password: form.password,
        companyName: form.companyName || undefined,
        phone: form.phone || undefined,
        country: form.country || undefined,
      })
      setPendingEmail(result?.email || form.email)
      setDevLink(result?.devVerificationUrl || null)
    } catch (err) {
      setError(apiErrorMessage(err, 'Registration failed. Please try again.'))
    } finally {
      setSubmitting(false)
    }
  }

  if (pendingEmail) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-navy-950 px-4 py-16">
        <div className="w-full max-w-md">
          <Link to="/" className="mb-8 block text-center font-display text-2xl font-bold text-white">
            Export<span className="text-gold-500">Platform</span>
          </Link>
          <div className="rounded-2xl border border-emerald-400/20 bg-white/5 p-8 text-center backdrop-blur">
            <h1 className="font-display text-2xl font-bold text-white">Verify your email</h1>
            <p className="mt-3 text-sm leading-relaxed text-white/70">
              We sent a verification link to{' '}
              <span className="font-semibold text-gold-400">{pendingEmail}</span>.
              Click the link in the email to activate your account before logging in.
            </p>
            {devLink && (
              <div className="mt-5 rounded-lg border border-amber-300/30 bg-amber-400/10 p-4 text-left text-xs text-amber-200">
                <p className="font-semibold">Mail delivery is disabled locally.</p>
                <a href={devLink} className="mt-1 block break-all font-mono underline">{devLink}</a>
              </div>
            )}
            <Link
              to="/login"
              state={{ from: '/dashboard' }}
              className="btn-primary mt-6 inline-block w-full"
            >
              Go to Login
            </Link>
          </div>
        </div>
      </div>
    )
  }

  const inputClass =
    'mt-1.5 w-full rounded-lg border border-white/15 bg-navy-900 px-4 py-2.5 text-white placeholder-white/30 focus:border-gold-500 focus:outline-none focus:ring-1 focus:ring-gold-500'

  return (
    <div className="flex min-h-screen items-center justify-center bg-navy-950 px-4 py-16">
      <div className="w-full max-w-xl">
        <Link to="/" className="mb-8 block text-center font-display text-2xl font-bold text-white">
          Export<span className="text-gold-500">Platform</span>
        </Link>
        <div className="rounded-2xl border border-white/10 bg-white/5 p-8 backdrop-blur">
          <h1 className="font-display text-2xl font-bold text-white">Create your account</h1>
          <p className="mt-1 text-sm text-white/60">
            Start requesting export quotations in minutes.
          </p>
          {error && (
            <p className="mt-4 rounded-lg border border-red-400/40 bg-red-400/10 px-4 py-2 text-sm text-red-300">
              {error}
            </p>
          )}
          <form onSubmit={handleSubmit} className="mt-6 grid gap-5 sm:grid-cols-2">
            <div className="sm:col-span-2">
              <label htmlFor="fullName" className="block text-sm font-medium text-white/80">
                Full Name *
              </label>
              <input id="fullName" type="text" required maxLength={150} value={form.fullName} onChange={update('fullName')} className={inputClass} placeholder="Jane Trader" />
            </div>
            <div className="sm:col-span-2">
              <label htmlFor="email" className="block text-sm font-medium text-white/80">
                Email *
              </label>
              <input id="email" type="email" required autoComplete="email" value={form.email} onChange={update('email')} className={inputClass} placeholder="you@company.com" />
            </div>
            <div>
              <label htmlFor="password" className="block text-sm font-medium text-white/80">
                Password * <span className="text-white/40">(min 8 chars)</span>
              </label>
              <input id="password" type="password" required minLength={8} autoComplete="new-password" value={form.password} onChange={update('password')} className={inputClass} placeholder="••••••••" />
            </div>
            <div>
              <label htmlFor="confirmPassword" className="block text-sm font-medium text-white/80">
                Confirm Password *
              </label>
              <input id="confirmPassword" type="password" required minLength={8} autoComplete="new-password" value={form.confirmPassword} onChange={update('confirmPassword')} className={inputClass} placeholder="••••••••" />
            </div>
            <div className="sm:col-span-2">
              <label htmlFor="companyName" className="block text-sm font-medium text-white/80">
                Company
              </label>
              <input id="companyName" type="text" maxLength={200} value={form.companyName} onChange={update('companyName')} className={inputClass} placeholder="Acme Trading Ltd." />
            </div>
            <div>
              <label htmlFor="phone" className="block text-sm font-medium text-white/80">
                Phone
              </label>
              <input id="phone" type="tel" maxLength={30} value={form.phone} onChange={update('phone')} className={inputClass} placeholder="+91 98765 43210" />
            </div>
            <div>
              <label htmlFor="country" className="block text-sm font-medium text-white/80">
                Country
              </label>
              <input id="country" type="text" maxLength={80} value={form.country} onChange={update('country')} className={inputClass} placeholder="India" />
            </div>
            <button type="submit" disabled={submitting} className="btn-primary sm:col-span-2">
              {submitting ? 'Creating account…' : 'Create Account'}
            </button>
          </form>
          <p className="mt-6 text-center text-sm text-white/60">
            Already have an account?{' '}
            <Link to="/login" className="font-semibold text-gold-400 hover:text-gold-500">
              Log in
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
