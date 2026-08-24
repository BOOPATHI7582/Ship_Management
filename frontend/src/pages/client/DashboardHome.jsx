import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { fetchDashboardSummary } from '../../api/client'
import { ErrorAlert, Skeleton } from '../../components/ui/feedback'

const cards = [
  { key: 'totalEnquiries', label: 'Total Enquiries' },
  { key: 'activeShipments', label: 'Active Shipments' },
  { key: 'pendingQuotations', label: 'Pending Quotations' },
  { key: 'openNegotiations', label: 'Open Negotiations' },
  { key: 'pendingPayments', label: 'Payments In Progress' },
]

function formatMoney(value) {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    maximumFractionDigits: 2,
  }).format(value ?? 0)
}

export default function DashboardHome() {
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

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <h1 className="font-display text-2xl font-bold text-navy-950">Client Dashboard</h1>
        <Link
          to="/client/enquiries/new"
          className="rounded-lg bg-gold-500 px-5 py-2.5 text-sm font-bold text-navy-950 transition hover:bg-gold-400"
        >
          Submit New Enquiry
        </Link>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
        {cards.map((card, i) => (
          <motion.div
            key={card.key}
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.25, delay: i * 0.05 }}
            className="rounded-2xl border border-navy-100 bg-white p-6 shadow-sm"
          >
            <p className="text-xs font-semibold uppercase tracking-wide text-navy-400">
              {card.label}
            </p>
            {summary ? (
              <p className="mt-3 font-display text-3xl font-bold text-navy-950">
                {summary[card.key]}
              </p>
            ) : (
              <Skeleton className="mt-4 h-8 w-24" />
            )}
          </motion.div>
        ))}
        <div className="rounded-2xl border border-navy-100 bg-white p-6 shadow-sm">
          <p className="text-xs font-semibold uppercase tracking-wide text-navy-400">
            Outstanding Balance
          </p>
          {summary ? (
            <p className="mt-3 font-display text-3xl font-bold text-navy-950">
              {formatMoney(summary.outstandingAmount)}
            </p>
          ) : (
            <Skeleton className="mt-4 h-8 w-32" />
          )}
        </div>
        <div className="rounded-2xl border border-navy-100 bg-white p-6 shadow-sm">
          <p className="text-xs font-semibold uppercase tracking-wide text-navy-400">
            Completed Shipments
          </p>
          {summary ? (
            <p className="mt-3 font-display text-3xl font-bold text-navy-950">
              {summary.completedShipments}
            </p>
          ) : (
            <Skeleton className="mt-4 h-8 w-24" />
          )}
        </div>
      </div>

      <p className="rounded-xl bg-navy-100/60 px-5 py-4 text-sm text-navy-500">
        Negotiations, quotations and billing views unlock as our operations team responds to your enquiries.
      </p>
    </div>
  )
}
