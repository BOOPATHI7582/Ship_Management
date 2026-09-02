import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { fetchCurrentUser, googleLoginUser, loginUser, registerUser } from '../api/auth'
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

  // data is a raw AuthResponse: { accessToken, tokenType, expiresInMs, user }
  const applySession = useCallback((data) => {
    localStorage.setItem(TOKEN_KEY, data.accessToken)
    setUser(data.user)
    setToken(data.accessToken)
    return data.user
  }, [])

  const login = useCallback(async (email, password) => {
    const res = await loginUser({ email, password })
    return applySession(res.data)
  }, [applySession])

  // Establish a session from a raw AuthResponse (e.g. after email verification
  // returns a token).
  const establishSession = useCallback((data) => applySession(data), [applySession])

  const continueWithGoogle = useCallback(async (idToken) => {
    const res = await googleLoginUser({ idToken })
    return applySession(res.data)
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
      establishSession,
      continueWithGoogle,
      register,
      logout,
    }),
    [user, token, loading, login, establishSession, continueWithGoogle, register, logout]
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