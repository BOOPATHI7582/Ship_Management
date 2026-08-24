import { useEffect, useState } from 'react'
import { fetchReportOverview } from '../../api/reports'
import { money } from '../../api/invoices'
import { CardsSkeleton, ErrorAlert, Skeleton } from '../../components/ui/feedback'

const ENQUIRY_LABELS = {
  NEW: 'New', REVIEWING: 'Reviewing', CONTACTED: 'Contacted', NEGOTIATING: 'Negotiating',
  QUOTATION_SENT: 'Quotation sent', APPROVED: 'Approved', REJECTED: 'Rejected',
  CONVERTED: 'Converted', CLOSED: 'Closed',
}
const SHIPMENT_LABELS = {
  BOOKING_CONFIRMED: 'Booked', CARGO_PREPARATION: 'Cargo prep', LOADING: 'Loading',
  LOADING_COMPLETED: 'Loaded', DEPARTED: 'Departed', IN_TRANSIT: 'In transit',
  NEAR_DESTINATION: 'Near destination', ARRIVED: 'Arrived', UNLOADING: 'Unloading',
  DELIVERED: 'Delivered', COMPLETED: 'Completed', CANCELLED: 'Cancelled',
}
const INVOICE_LABELS = {
  ISSUED: 'Issued', PAYMENT_PENDING: 'Payment pending', ADVANCE_PAID: 'Advance paid',
  PARTIALLY_PAID: 'Partial', PAID: 'Paid', OVERDUE: 'Overdue', CANCELLED: 'Cancelled',
}

function KpiCard({ label, value, sub, accent }) {
  return (
    <div className="rounded-xl border border-navy-100 bg-white p-4 shadow-sm">
      <p className="text-xs font-semibold uppercase tracking-wide text-navy-500">{label}</p>
      <p className={`mt-1 text-2xl font-bold ${accent || 'text-navy-900'}`}>{value}</p>
      {sub && <p className="mt-0.5 text-xs text-navy-400">{sub}</p>}
    </div>
  )
}

function BarList({ title, items, format }) {
  const max = Math.max(...items.map((i) => Number(i.value) || 0), 1)
  return (
    <div className="rounded-xl border border-navy-100 bg-white p-4 shadow-sm">
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
              <div className="mt-0.5 h-2 overflow-hidden rounded-full bg-navy-50">
                <div
                  className="h-full rounded-full bg-sky-500"
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

function TrendChart({ title, points, comparePoints }) {
  const max = Math.max(
    ...points.map((p) => Number(p.amount) || 0),
    ...(comparePoints || []).map((p) => Number(p.amount) || 0),
    1,
  )
  return (
    <div className="rounded-xl border border-navy-100 bg-white p-4 shadow-sm lg:col-span-2">
      <h3 className="mb-3 text-sm font-semibold text-navy-900">{title}</h3>
      <div className="flex items-end gap-1.5" style={{ height: 160 }}>
        {points.map((point, idx) => {
          const h = (Number(point.amount) / max) * 100
          const ch = comparePoints ? (Number(comparePoints[idx]?.amount) || 0) / max * 100 : null
          return (
            <div key={point.month} className="group relative flex flex-1 flex-col items-center justify-end gap-0.5" style={{ height: '100%' }}>
              <div className="pointer-events-none absolute bottom-full z-10 mb-1 hidden whitespace-nowrap rounded bg-navy-900 px-2 py-1 text-[10px] text-white group-hover:block">
                {point.month}: {money(point.amount)}
              </div>
              <div className="flex w-full items-end justify-center gap-0.5" style={{ height: '100%' }}>
                {ch !== null && (
                  <div className="w-2 rounded-t bg-navy-200" style={{ height: `${ch}%` }} title={`Invoiced`} />
                )}
                <div className={comparePoints ? 'w-2 rounded-t bg-emerald-500' : 'w-full rounded-t bg-emerald-500'} style={{ height: `${h}%` }} />
              </div>
            </div>
          )
        })}
      </div>
      <div className="mt-1 flex gap-1.5">
        {points.map((point) => (
          <span key={point.month} className="flex-1 text-center text-[9px] text-navy-400">
            {point.month.slice(5)}
          </span>
        ))}
      </div>
      {comparePoints && (
        <div className="mt-2 flex gap-4 text-xs text-navy-500">
          <span className="flex items-center gap-1"><span className="inline-block h-2 w-2 rounded bg-emerald-500" /> Collected</span>
          <span className="flex items-center gap-1"><span className="inline-block h-2 w-2 rounded bg-navy-200" /> Invoiced</span>
        </div>
      )}
    </div>
  )
}

export default function ReportsPage() {
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)

  const load = () => {
    setError(null)
    fetchReportOverview()
      .then((res) => setData(res.data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load reports'))
  }

  useEffect(() => { load() }, [])

  if (error) return <ErrorAlert message={error} onRetry={load} />
  if (!data) {
    return (
      <div className="space-y-6">
        <CardsSkeleton count={8} />
        <CardsSkeleton count={1} className="grid grid-cols-1" />
        {Array.from({ length: 3 }).map((_, i) => (
          <div key={i} className="rounded-xl border border-navy-100 bg-white p-4 shadow-sm">
            <Skeleton className="mb-3 h-3 w-32" />
            <Skeleton className="h-24 w-full" />
          </div>
        ))}
      </div>
    )
  }

  const { totals, revenueTrend, invoicedTrend, enquiryFunnel, shipmentStatus, invoiceStatus, topDebtors, topClients } = data

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-2 gap-4 xl:grid-cols-4">
        <KpiCard label="Collected" value={money(totals.totalCollected)} sub="Payments received" accent="text-emerald-600" />
        <KpiCard label="Invoiced" value={money(totals.totalInvoiced)} sub="All non-cancelled invoices" />
        <KpiCard label="Outstanding" value={money(totals.outstanding)} sub="Issued + partial + overdue" accent={Number(totals.outstanding) > 0 ? 'text-amber-600' : undefined} />
        <KpiCard label="Clients" value={totals.clients} />
        <KpiCard label="Active shipments" value={totals.activeShipments} />
        <KpiCard label="Completed shipments" value={totals.completedShipments} />
        <KpiCard label="Open enquiries" value={totals.openEnquiries} />
        <KpiCard label="Reviews awaiting moderation" value={totals.pendingReviews} accent={totals.pendingReviews > 0 ? 'text-amber-600' : undefined} />
      </div>

      <TrendChart title="Cash flow — last 12 months" points={revenueTrend} comparePoints={invoicedTrend} />

      <div className="grid gap-4 lg:grid-cols-3">
        <BarList
          title="Enquiry funnel"
          items={enquiryFunnel.map((e) => ({ label: ENQUIRY_LABELS[e.name] || e.name, value: e.count }))}
        />
        <BarList
          title="Shipments by status"
          items={shipmentStatus.filter((s) => s.count > 0).map((s) => ({ label: SHIPMENT_LABELS[s.name] || s.name, value: s.count }))}
        />
        <BarList
          title="Invoices by status"
          items={invoiceStatus.filter((s) => s.count > 0).map((s) => ({ label: INVOICE_LABELS[s.name] || s.name, value: s.count }))}
        />
      </div>

      <div className="grid gap-4 lg:grid-cols-2">
        <BarList
          title="Top outstanding balances"
          items={topDebtors.map((c) => ({ label: c.clientName, value: c.amount }))}
          format={(v) => money(v)}
        />
        <BarList
          title="Top clients by revenue"
          items={topClients.map((c) => ({ label: c.clientName, value: c.amount }))}
          format={(v) => money(v)}
        />
      </div>
    </div>
  )
}
