import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { useAuth } from '../../context/AuthContext'
import { fetchDashboardSummary } from '../../api/client'
import { money } from '../../api/invoices'
import { ErrorAlert, Skeleton } from '../../components/ui/feedback'

const statCards = [
  { key: 'totalEnquiries', label: 'Total Enquiries', hint: 'All enquiries you have submitted' },
  { key: 'activeShipments', label: 'Active Shipments', hint: 'In transit or in preparation' },
  { key: 'pendingQuotations', label: 'Pending Quotations', hint: 'Quotes awaiting your review' },
  { key: 'activeNegotiations', label: 'Open Negotiations', hint: 'Live price discussions' },
  { key: 'pendingPayments', label: 'Payments In Progress', hint: 'Advance or full payments' },
  { key: 'completedShipments', label: 'Completed Shipments', hint: 'Delivered and signed off' },
]

const quickActions = [
  { to: '/client/enquiries/new', label: 'Submit New Enquiry', hint: 'Request a quote for your cargo', accent: 'bg-gold-500 text-navy-950 hover:bg-gold-400' },
  { to: '/client/enquiries', label: 'My Enquiries', hint: 'Track status and quotations', accent: 'border border-navy-200 text-navy-700 hover:bg-navy-50' },
  { to: '/client/shipments', label: 'Shipment Tracking', hint: 'Follow cargo in real time', accent: 'border border-navy-200 text-navy-700 hover:bg-navy-50' },
  { to: '/client/invoices', label: 'Tax Invoices', hint: 'View dues, pay and download', accent: 'border border-navy-200 text-navy-700 hover:bg-navy-50' },
]

export default function DashboardHome() {
  const { user } = useAuth()
  const [summary, setSummary] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelled = false
    fetchDashboardSummary()
      .then((res) => {
        if (!cancelled) setSummary(res.data)
      })
      .catch((err) => {
        if (!cancelled) setError(err.response?.data?.message || 'Failed to load dashboard')
      })
    return () => {
      cancelled = true
    }
  }, [])

  if (error) {
    return <ErrorAlert message={error} onRetry={() => window.location.reload()} />
  }

  const firstName = user?.fullName?.split(' ')[0] || 'there'
  const company = user?.companyName || ''

  return (
    <div className="space-y-6">
      <motion.div
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3 }}
        className="relative overflow-hidden rounded-3xl bg-navy-950 p-7 text-white shadow-lg sm:p-9"
      >
        <div className="pointer-events-none absolute -right-16 -top-16 h-56 w-56 rounded-full bg-gold-500/20 blur-2xl" />
        <div className="pointer-events-none absolute -bottom-20 right-24 h-44 w-44 rounded-full bg-sky-400/10 blur-2xl" />
        <div className="relative">
          <p className="text-xs font-semibold uppercase tracking-widest text-gold-400">
            {company || 'Export Trading'}
          </p>
          <h1 className="mt-2 font-display text-2xl font-bold sm:text-3xl">
            Welcome back, {firstName}
          </h1>
          <p className="mt-2 max-w-xl text-sm text-white/70">
            Enquiries, quotations, invoicing and shipment tracking — everything for your cargo in one place.
          </p>
          <div className="mt-5 flex flex-wrap gap-3">
            <Link
              to="/client/enquiries/new"
              className="rounded-lg bg-gold-500 px-5 py-2.5 text-sm font-bold text-navy-950 transition hover:bg-gold-400"
            >
              Submit New Enquiry
            </Link>
            <Link
              to="/client/shipments"
              className="rounded-lg border border-white/25 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-white/10"
            >
              Track Shipments
            </Link>
          </div>
        </div>
      </motion.div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {quickActions.map((action, i) => (
          <motion.div
            key={action.to}
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.25, delay: i * 0.05 }}
          >
            <Link to={action.to} className={`flex h-full flex-col justify-between gap-3 rounded-2xl p-5 shadow-sm transition ${action.accent}`}>
              <div>
                <p className="text-sm font-bold">{action.label}</p>
                <p className="mt-1 text-xs opacity-70">{action.hint}</p>
              </div>
              <span className="text-lg leading-none" aria-hidden>→</span>
            </Link>
          </motion.div>
        ))}
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
        {statCards.map((card, i) => (
          <motion.div
            key={card.key}
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.25, delay: 0.1 + i * 0.04 }}
            className="rounded-2xl border border-navy-100 bg-white p-6 shadow-sm"
          >
            <p className="text-xs font-semibold uppercase tracking-wide text-navy-400">{card.label}</p>
            {summary ? (
              <p className="mt-3 font-display text-3xl font-bold text-navy-950">{summary[card.key]}</p>
            ) : (
              <Skeleton className="mt-4 h-8 w-24" />
            )}
            <p className="mt-1 text-xs text-navy-400">{card.hint}</p>
          </motion.div>
        ))}
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.25, delay: 0.1 + statCards.length * 0.04 }}
          className="rounded-2xl border border-navy-100 bg-navy-950 p-6 text-white shadow-sm"
        >
          <p className="text-xs font-semibold uppercase tracking-wide text-white/50">Outstanding Balance</p>
          {summary ? (
            <p className="mt-3 font-display text-3xl font-bold text-gold-400">{money(summary.outstandingAmount)}</p>
          ) : (
            <Skeleton className="mt-4 h-8 w-32" />
          )}
          <p className="mt-1 text-xs text-white/40">Issued invoices awaiting settlement</p>
        </motion.div>
      </div>

      <p className="rounded-xl bg-navy-100/60 px-5 py-4 text-sm text-navy-500">
        Negotiations, quotations and billing views unlock as our operations team responds to your enquiries.
      </p>
    </div>
  )
}