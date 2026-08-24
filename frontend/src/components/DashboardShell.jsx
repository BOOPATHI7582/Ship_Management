import { useEffect, useState } from 'react'
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { motion, AnimatePresence } from 'framer-motion'
import { useAuth } from '../context/AuthContext'
import { fetchUnreadCount } from '../api/contactMessages'

/**
 * Shared dashboard chrome for the Admin / Operations / Client areas.
 * Renders a fixed sidebar on md+ screens and a hamburger drawer on mobile.
 * When `badgePath` is set, an unread count from the staff contact inbox
 * is fetched and shown next to the matching nav item.
 */
export default function DashboardShell({
  homeTo,
  tag,
  tagClasses,
  items,
  headerExtras,
  badgePath,
}) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [navOpen, setNavOpen] = useState(false)
  const [badge, setBadge] = useState(0)

  useEffect(() => {
    setNavOpen(false)
  }, [location.pathname])

  useEffect(() => {
    if (!badgePath) return undefined
    let active = true
    fetchUnreadCount()
      .then((res) => {
        if (active) setBadge(res.data?.data ?? 0)
      })
      .catch(() => {})
    return () => {
      active = false
    }
  }, [badgePath])

  function handleLogout() {
    logout()
    navigate('/login')
  }

  function renderLinks() {
    return items.map((item) => {
      const showBadge =
        badgePath && item.to === badgePath && typeof badge === 'number' && badge > 0
      return (
        <NavLink
          key={item.to}
          to={item.to}
          end={item.end}
          className={({ isActive }) =>
            `flex items-center justify-between rounded-lg px-4 py-2.5 text-sm font-semibold transition ${
              isActive ? 'bg-navy-950 text-white' : 'text-navy-700 hover:bg-navy-50'
            }`
          }
        >
          <span>{item.label}</span>
          {showBadge && (
            <span className="ml-2 inline-flex h-5 min-w-5 items-center justify-center rounded-full bg-red-500 px-1.5 text-[11px] font-bold text-white">
              {badge > 99 ? '99+' : badge}
            </span>
          )}
        </NavLink>
      )
    })
  }

  const sidebarBody = (
    <>
      <nav className="flex flex-col gap-1">{renderLinks()}</nav>
      <div className="my-3 border-t border-navy-100" />
      <button
        type="button"
        onClick={handleLogout}
        className="w-full rounded-lg px-4 py-2.5 text-left text-sm font-semibold text-navy-700 transition hover:bg-navy-50 lg:hidden"
      >
        Logout
      </button>
    </>
  )

  return (
    <div className="min-h-screen bg-navy-50">
      <header className="sticky top-0 z-30 bg-navy-950 text-white">
        <div className="container-page flex h-16 items-center justify-between gap-3">
          <div className="flex min-w-0 items-center gap-2">
            <button
              type="button"
              aria-label="Open navigation"
              onClick={() => setNavOpen(true)}
              className="rounded-lg p-2 transition hover:bg-white/10 md:hidden"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="h-5 w-5">
                <path strokeLinecap="round" d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            </button>
            <NavLink to={homeTo} className="truncate font-display text-lg font-bold">
              Export<span className="text-gold-500">Platform</span>
            </NavLink>
            {tag && (
              <span
                className={`hidden rounded-full px-2.5 py-0.5 align-middle text-xs font-bold uppercase sm:inline-block ${tagClasses}`}
              >
                {tag}
              </span>
            )}
          </div>
          <div className="flex shrink-0 items-center gap-2 sm:gap-4">
            <span className="hidden max-w-52 truncate text-sm text-white/70 lg:block">
              {user?.fullName} · <span className="uppercase">{user?.role}</span>
            </span>
            {headerExtras}
            <button
              type="button"
              onClick={handleLogout}
              className="rounded-lg border border-white/30 px-3 py-1.5 text-sm font-semibold transition hover:bg-white/10 sm:px-3.5"
            >
              Logout
            </button>
          </div>
        </div>
      </header>

      <div className="container-page flex gap-8 py-8">
        <aside className="hidden w-56 shrink-0 md:block">
          <nav className="sticky top-24 rounded-2xl border border-navy-100 bg-white p-3 shadow-sm">
            {sidebarBody}
          </nav>
        </aside>

        <div className="min-w-0 flex-1">
          <Outlet />
        </div>
      </div>

      <AnimatePresence>
        {navOpen && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.15 }}
            className="fixed inset-0 z-40 md:hidden"
          >
            <div
              className="absolute inset-0 bg-navy-950/60 backdrop-blur-sm"
              onClick={() => setNavOpen(false)}
              aria-hidden="true"
            />
            <motion.div
              initial={{ x: '-100%' }}
              animate={{ x: 0 }}
              exit={{ x: '-100%' }}
              transition={{ type: 'tween', duration: 0.22, ease: 'easeOut' }}
              className="absolute inset-y-0 left-0 flex w-64 flex-col overflow-y-auto bg-white p-4 shadow-xl"
            >
              <div className="mb-4 flex items-center justify-between">
                <span className="font-display text-base font-bold text-navy-900">Menu</span>
                <button
                  type="button"
                  aria-label="Close navigation"
                  onClick={() => setNavOpen(false)}
                  className="rounded-lg p-1.5 text-navy-500 transition hover:bg-navy-50"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="h-5 w-5">
                    <path strokeLinecap="round" d="M6 6l12 12M18 6L6 18" />
                  </svg>
                </button>
              </div>
              {sidebarBody}
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  )
}
