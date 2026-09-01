import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { apiErrorMessage } from '../api/axios'

const CLIENT_ID = (import.meta.env.VITE_GOOGLE_CLIENT_ID || '').trim()

export default function GoogleLoginButton() {
  const { continueWithGoogle } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const loadedRef = useRef(false)

  useEffect(() => {
    if (!CLIENT_ID || loadedRef.current) return
    loadedRef.current = true
    const script = document.createElement('script')
    script.src = 'https://accounts.google.com/gsi/client'
    script.async = true
    script.defer = true
    document.head.appendChild(script)
  }, [])

  if (!CLIENT_ID) return null

  function fallbackFor(user) {
    return user?.role === 'ADMIN'
      ? '/admin/dashboard'
      : user?.role === 'SHIP_MANAGER'
        ? '/manager/enquiries'
        : '/dashboard'
  }

  function handleCredential(credential) {
    setBusy(true)
    setError('')
    continueWithGoogle(credential)
      .then((user) => {
        navigate(fallbackFor(user), { replace: true })
      })
      .catch((err) => setError(apiErrorMessage(err, 'Google sign-in failed.')))
      .finally(() => setBusy(false))
  }

  function handleClick() {
    if (busy || !window.google?.accounts?.id) return
    window.google.accounts.id.initialize({
      client_id: CLIENT_ID,
      callback: (res) => res?.credential && handleCredential(res.credential),
    })
    window.google.accounts.id.prompt()
  }

  return (
    <div>
      <button type="button" onClick={handleClick} disabled={busy} className="btn-google">
        <svg viewBox="0 0 24 24" className="h-5 w-5" aria-hidden="true">
          <path
            fill="#4285F4"
            d="M23.49 12.27c0-.79-.07-1.54-.19-2.27H12v4.51h6.47c-.29 1.48-1.14 2.73-2.4 3.58v3h3.86c2.26-2.09 3.56-5.17 3.56-8.82z"
          />
          <path
            fill="#34A853"
            d="M12 24c3.24 0 5.95-1.08 7.93-2.91l-3.86-3c-1.08.72-2.45 1.16-4.07 1.16-3.13 0-5.78-2.11-6.73-4.96H1.29v3.09C3.26 21.3 7.31 24 12 24z"
          />
          <path
            fill="#FBBC05"
            d="M5.27 14.29c-.25-.72-.38-1.49-.38-2.29s.14-1.57.38-2.29V6.62H1.29C.47 8.24 0 10.06 0 12s.47 3.76 1.29 5.38l3.98-3.09z"
          />
          <path
            fill="#EA4335"
            d="M12 4.75c1.77 0 3.35.61 4.6 1.8l3.42-3.42C17.95 1.19 15.24 0 12 0 7.31 0 3.26 2.7 1.29 6.62l3.98 3.09c.95-2.85 3.6-4.96 6.73-4.96z"
          />
        </svg>
        {busy ? 'Signing in…' : 'Continue with Google'}
      </button>
      {error && (
        <p className="mt-3 rounded-lg border border-red-400/40 bg-red-400/10 px-4 py-2 text-sm text-red-300">
          {error}
        </p>
      )}
    </div>
  )
}