import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { fetchReportOverview } from '../../api/reports'
import { money } from '../../api/invoices'
import { CardsSkeleton, ErrorAlert, Skeleton } from '../../components/ui/feedback'

const SHIPMENT_LABELS = {
  BOOKING_CONFIRMED: 'Booked', CARGO_PREPARATION: 'Cargo prep', LOADING: 'Loading',
  LOADING_COMPLETED: 'Loaded', DEPARTED: 'Departed', IN_TRANSIT: 'In transit',
  NEAR_DESTINATION: 'Near destination', ARRIVED: 'Arrived', UNLOADING: 'Unloading',
  DELIVERED: 'Delivered', COMPLETED: 'Completed', CANCELLED: 'Cancelled',
}

function KpiCard({ label, value, sub, accent }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.25 }}
      className="rounded-2xl border border-navy-100 bg-white p-5 shadow-sm"
    >
      <p className="text-xs font-semibold uppercase tracking-wide text-navy-400">{label}</p>
      <p className={`mt-2 font-display text-2xl font-bold ${accent || 'text-navy-950'}`}>{value}</p>
      {sub && <p className="mt-1 text-xs text-navy-400">{sub}</p>}
    </motion.div>
  )
}

function BarList({ title, items, format }) {
  const max = Math.max(...items.map((i) => Number(i.value) || 0), 1)
  return (
    <div className="rounded-2xl border border-navy-100 bg-white p-5 shadow-sm">
      <h3 className="mb-3 text-sm font-semibold text-navy-900">{title}</h3>
      {items.length === 0 || items.every((i) => !Number(i.value)) ? (
        <p className="text-sm text-navy-400">No data yet</p>
      ) : (
        <ul className="space-y-2">
          {items.map((item) => (
            <li key={item.label}>
              <div className="flex justify-between text-xs text-navy-600">
                <span>{item.label}</span>
                <span className="font-semibold">{format ? format(item.value) : item.value}</span>
              </div>
              <div className="mt-0.5 h-1.5 overflow-hidden rounded-full bg-navy-50">
                <div
                  className="h-full rounded-full bg-gold-500"
                  style={{ width: `${Math.max((Number(item.value) / max) * 100, Number(item.value) ? 4 : 0)}%` }}
                />
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

export default function AdminDashboardPage() {
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)

  const load = () => {
    setError(null)
    fetchReportOverview()
      .then((res) => setData(res.data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load dashboard'))
  }

  useEffect(() => { load() }, [])

  if (error) return <ErrorAlert message={error} onRetry={load} />

  const { totals, enquiryFunnel, shipmentStatus } = data || {}

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="font-display text-2xl font-bold text-navy-950">Admin Dashboard</h1>
          <p className="text-sm text-navy-500">Operational overview across enquiries, shipments, billing and accounts.</p>
        </div>
        <Link
          to="/manager/reports"
          className="rounded-lg border border-navy-200 px-4 py-2 text-sm font-semibold text-navy-700 transition hover:bg-navy-50"
        >
          View full reports
        </Link>
      </div>

      {!data ? (
        <CardsSkeleton count={8} />
      ) : (
        <>
          <div className="grid grid-cols-2 gap-4 xl:grid-cols-4">
            <KpiCard label="Collected" value={money(totals.totalCollected)} accent="text-emerald-600" />
            <KpiCard label="Invoiced" value={money(totals.totalInvoiced)} />
            <KpiCard label="Outstanding" value={money(totals.outstanding)} accent={Number(totals.outstanding) > 0 ? 'text-amber-600' : undefined} />
            <KpiCard label="Active clients" value={totals.clients} />
          </div>
          <div className="grid grid-cols-2 gap-4 xl:grid-cols-4">
            <KpiCard label="Active shipments" value={totals.activeShipments} />
            <KpiCard label="Completed shipments" value={totals.completedShipments} />
            <KpiCard label="Open enquiries" value={totals.openEnquiries} />
            <KpiCard label="Pending reviews" value={totals.pendingReviews} accent={totals.pendingReviews > 0 ? 'text-amber-600' : undefined} />
          </div>
          <div className="grid gap-4 lg:grid-cols-3">
            <BarList
              title="Enquiry funnel"
              items={enquiryFunnel.map((e) => ({ label: e.name.replace(/_/g, ' ').toLowerCase().replace(/^\w/, (c) => c.toUpperCase()), value: e.count }))}
            />
            <BarList
              title="Shipments by status"
              items={shipmentStatus.filter((s) => s.count > 0).map((s) => ({ label: SHIPMENT_LABELS[s.name] || s.name, value: s.count }))}
            />
            <div className="rounded-2xl border border-navy-100 bg-white p-5 shadow-sm">
              <h3 className="mb-3 text-sm font-semibold text-navy-900">Quick links</h3>
              <ul className="space-y-2 text-sm text-navy-600">
                <li><Link className="transition hover:text-gold-600" to="/admin/users">Manage users</Link></li>
                <li><Link className="transition hover:text-gold-600" to="/admin/clients">Manage clients</Link></li>
                <li><Link className="transition hover:text-gold-600" to="/admin/vessels">Manage vessels</Link></li>
                <li><Link className="transition hover:text-gold-600" to="/manager/enquiries">Enquiry queue</Link></li>
                <li><Link className="transition hover:text-gold-600" to="/manager/contacts">Contact inbox</Link></li>
                <li><Link className="transition hover:text-gold-600" to="/manager/audit">Audit log</Link></li>
              </ul>
            </div>
          </div>
        </>
      )}

      {data && <p className="rounded-xl bg-navy-100/60 px-5 py-4 text-center text-xs text-navy-500">Updated live from operations data.</p>}
    </div>
  )
}