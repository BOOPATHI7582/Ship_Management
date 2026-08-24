import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function ProtectedRoute({ children, roles }) {
  const { isAuthenticated, loading, user } = useAuth()
  const location = useLocation()

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-navy-950">
        <div className="h-10 w-10 animate-spin rounded-full border-2 border-gold-500 border-t-transparent" />
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location.pathname }} replace />
  }

  if (roles && !roles.includes(user?.role)) {
    return <Navigate to={user?.role === 'ADMIN' ? '/admin/users' : '/dashboard'} replace />
  }

  return children
}
