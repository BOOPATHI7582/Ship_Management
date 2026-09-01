import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { fetchCurrentUser, googleLoginUser, loginOtp, loginUser, registerUser } from '../api/auth'
import { TOKEN_KEY } from '../api/axios'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_KEY))
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(Boolean(token))

  useEffect(() => {
    let cancelled = false
    async function bootstrap() {
      if (!token) {
        setLoading(false)
        return
      }
      try {
        const res = await fetchCurrentUser()
        if (!cancelled) setUser(res.data)
      } catch {
        if (!cancelled) {
          localStorage.removeItem(TOKEN_KEY)
          setToken(null)
          setUser(null)
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    bootstrap()
    return () => {
      cancelled = true
    }
  }, [token])

  const applySession = useCallback((res) => {
    localStorage.setItem(TOKEN_KEY, res.data.accessToken)
    setUser(res.data.user)
    setToken(res.data.accessToken)
    return res.data.user
  }, [])

  const login = useCallback(async (email, password) => {
    const res = await loginUser({ email, password })
    if (res.data?.requiresOtp) {
      return { requiresOtp: true, devOtp: res.data?.devOtp || null, user: res.data?.user || null }
    }
    return { requiresOtp: false, user: applySession(res) }
  }, [applySession])

  // Complete the password flow: verify the 6-digit login code and start a session.
  const finalizeLogin = useCallback(async (email, otp) => {
    const res = await loginOtp({ email, otp })
    return applySession(res)
  }, [applySession])

  const continueWithGoogle = useCallback(async (idToken) => {
    const res = await googleLoginUser({ idToken })
    return applySession(res)
  }, [applySession])

  const register = useCallback(async (payload) => {
    // Registration no longer signs the user in: email verification is required.
    const res = await registerUser(payload)
    return res.data
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY)
    setToken(null)
    setUser(null)
  }, [])

  const value = useMemo(
    () => ({
      user,
      token,
      loading,
      isAuthenticated: Boolean(user),
      isAdmin: user?.role === 'ADMIN',
      isShipManager: user?.role === 'SHIP_MANAGER',
      login,
      finalizeLogin,
      continueWithGoogle,
      register,
      logout,
    }),
    [user, token, loading, login, finalizeLogin, continueWithGoogle, register, logout]
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used inside an AuthProvider')
  }
  return context
}