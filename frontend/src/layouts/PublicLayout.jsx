import { useEffect, useState } from 'react'
import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const publicLinks = [
  { label: 'Home', to: '/' },
  { label: 'About', to: '/about' },
  { label: 'Services', to: '/services' },
  { label: 'Cargo', to: '/cargo' },
  { label: 'Shipments', to: '/shipments' },
  { label: 'Tracking', to: '/tracking' },
  { label: 'Contact', to: '/contact' },
]

export default function PublicLayout() {
  const { isAuthenticated, user, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const authState = { from: location.pathname }
  const [menuOpen, setMenuOpen] = useState(false)

  useEffect(() => {
    setMenuOpen(false)
  }, [navigate])

  function handleLogout() {
    logout()
    setMenuOpen(false)
    navigate('/')
  }

  const homeFor = () =>
    user?.role === 'ADMIN' ? '/admin/dashboard'
      : user?.role === 'SHIP_MANAGER' ? '/manager/enquiries'
      : '/dashboard'

  const linkClass = ({ isActive }) =>
    `text-sm font-medium transition ${isActive ? 'text-gold-400' : 'text-white/80 hover:text-white'}`

  return (
    <div className="flex min-h-screen flex-col">
      <header className="sticky top-0 z-40 border-b border-white/10 bg-navy-950/95 text-white backdrop-blur">
        <div className="container-page flex h-16 items-center justify-between gap-6">
          <Link to="/" className="font-display text-xl font-bold tracking-tight">
            Export<span className="text-gold-500">Platform</span>
          </Link>

          <nav className="hidden items-center gap-7 lg:flex">
            {publicLinks.map((link) => (
              <NavLink key={link.label} to={link.to} className={linkClass} end={link.to === '/'}>
                {link.label}
              </NavLink>
            ))}
          </nav>

          <div className="hidden items-center gap-3 lg:flex">
            {isAuthenticated ? (
              <>
                <Link
                  to={homeFor()}
                  className="rounded-lg px-4 py-2 text-sm font-semibold text-white transition hover:bg-white/10"
                >
                  {user?.fullName?.split(' ')[0] || 'Dashboard'}
                </Link>
                <button
                  type="button"
                  onClick={handleLogout}
                  className="rounded-lg border border-white/30 px-4 py-2 text-sm font-semibold text-white transition hover:bg-white/10"
                >
                  Logout
                </button>
              </>
            ) : (
              <>
                <Link to="/login" state={authState} className="rounded-lg px-4 py-2 text-sm font-semibold text-white transition hover:bg-white/10">
                  Login
                </Link>
                <Link
                  to="/register"
                  state={authState}
                  className="rounded-lg bg-gold-500 px-4 py-2 text-sm font-bold text-navy-950 transition hover:bg-gold-400"
                >
                  Register
                </Link>
              </>
            )}
          </div>

          <button
            type="button"
            onClick={() => setMenuOpen((open) => !open)}
            aria-label="Toggle menu"
            aria-expanded={menuOpen}
            className="lg:hidden"
          >
            <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              {menuOpen ? (
                <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
              ) : (
                <path strokeLinecap="round" strokeLinejoin="round" d="M4 7h16M4 12h16M4 17h16" />
              )}
            </svg>
          </button>
        </div>

        {menuOpen && (
          <nav className="border-t border-white/10 bg-navy-950 px-4 pb-6 pt-3 lg:hidden">
            <div className="container-page flex flex-col gap-1">
              {publicLinks.map((link) => (
                <NavLink
                  key={link.label}
                  to={link.to}
                  end={link.to === '/'}
                  onClick={() => setMenuOpen(false)}
                  className={({ isActive }) =>
                    `rounded-lg px-3 py-2.5 text-sm font-medium ${
                      isActive ? 'bg-white/10 text-gold-400' : 'text-white/80 hover:bg-white/5'
                    }`
                  }
                >
                  {link.label}
                </NavLink>
              ))}
              <hr className="my-3 border-white/10" />
              {isAuthenticated ? (
                <>
                  <Link
                    to={homeFor()}
                    onClick={() => setMenuOpen(false)}
                    className="rounded-lg px-3 py-2.5 text-sm font-semibold text-gold-400 hover:bg-white/5"
                  >
                    My Dashboard
                  </Link>
                  <button
                    type="button"
                    onClick={handleLogout}
                    className="rounded-lg px-3 py-2.5 text-left text-sm font-semibold text-white/80 hover:bg-white/5"
                  >
                    Logout
                  </button>
                </>
              ) : (
                <>
                  <Link
                    to="/login"
                    state={authState}
                    onClick={() => setMenuOpen(false)}
                    className="rounded-lg px-3 py-2.5 text-sm font-semibold text-white hover:bg-white/5"
                  >
                    Login
                  </Link>
                  <Link
                    to="/register"
                    state={authState}
                    onClick={() => setMenuOpen(false)}
                    className="mt-1 rounded-lg bg-gold-500 px-3 py-2.5 text-center text-sm font-bold text-navy-950"
                  >
                    Register
                  </Link>
                </>
              )}
            </div>
          </nav>
        )}
      </header>

      <main className="flex-1">
        <Outlet />
      </main>

      <footer className="border-t border-white/10 bg-navy-950 py-14 text-white">
        <div className="container-page grid gap-10 md:grid-cols-[1.4fr_1fr_1fr_1fr]">
          <div>
            <p className="font-display text-xl font-bold">
              Export<span className="text-gold-500">Platform</span>
            </p>
            <p className="mt-3 max-w-xs text-sm leading-relaxed text-white/60">
              Connecting Global Trade Through Reliable Export Solutions — bulk cargo, quotations,
              secure payments and shipment tracking worldwide.
            </p>
          </div>
          <div>
            <h3 className="text-sm font-bold uppercase tracking-wider text-white/90">Company</h3>
            <ul className="mt-4 space-y-2.5 text-sm text-white/60">
              {[['About', '/about'], ['Services', '/services'], ['Careers', '/careers'], ['Contact', '/contact']].map(([label, to]) => (
                <li key={label}><Link className="transition hover:text-gold-400" to={to}>{label}</Link></li>
              ))}
            </ul>
          </div>
          <div>
            <h3 className="text-sm font-bold uppercase tracking-wider text-white/90">Trade</h3>
            <ul className="mt-4 space-y-2.5 text-sm text-white/60">
              {[['Cargo Categories', '/cargo'], ['Available Shipments', '/shipments'], ['Track Shipment', '/tracking']].map(([label, to]) => (
                <li key={label}><Link className="transition hover:text-gold-400" to={to}>{label}</Link></li>
              ))}
            </ul>
          </div>
          <div>
            <h3 className="text-sm font-bold uppercase tracking-wider text-white/90">Account</h3>
            <ul className="mt-4 space-y-2.5 text-sm text-white/60">
              <li><Link className="transition hover:text-gold-400" to="/login" state={authState}>Login</Link></li>
              <li><Link className="transition hover:text-gold-400" to="/register">Register</Link></li>
              <li><Link className="transition hover:text-gold-400" to="/forgot-password">Reset Password</Link></li>
            </ul>
          </div>
        </div>
        <div className="container-page mt-10 flex flex-col items-center justify-between gap-3 border-t border-white/10 pt-6 text-xs text-white/40 md:flex-row">
          <p>© {new Date().getFullYear()} ExportPlatform. All rights reserved.</p>
          <p>Mumbai · Dubai · Singapore</p>
        </div>
      </footer>
    </div>
  )
}
