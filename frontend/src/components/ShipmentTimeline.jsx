import { statusLabel, statusBadge } from '../api/shipments'

function fmtDateTime(value) {
  if (!value) return '—'
  const d = new Date(value)
  return d.toLocaleString(undefined, { day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

export default function ShipmentTimeline({ timeline = [] }) {
  if (!timeline.length) {
    return <p className="py-4 text-center text-sm text-navy-400">No tracking updates yet.</p>
  }
  return (
    <ol className="relative space-y-5 border-l-2 border-navy-100 pl-5">
      {timeline.map((t) => (
        <li key={t.id} className="relative">
          <span className="absolute -left-[27px] top-1 h-3 w-3 rounded-full border-2 border-white bg-sky-500 shadow" />
          <div className="flex flex-wrap items-center gap-2">
            <span className={`rounded-full px-2.5 py-0.5 text-xs font-bold uppercase tracking-wide ${statusBadge(t.status)}`}>
              {statusLabel(t.status)}
            </span>
            <span className="text-xs text-navy-400">{fmtDateTime(t.occurredAt)}</span>
          </div>
          {t.locationLabel && <p className="mt-1 text-sm font-semibold text-navy-900">{t.locationLabel}</p>}
          {(t.latitude != null && t.longitude != null) && (
            <p className="text-xs font-mono text-navy-400">{Number(t.latitude).toFixed(4)}, {Number(t.longitude).toFixed(4)}</p>
          )}
          {t.notes && <p className="mt-0.5 text-sm text-navy-600">{t.notes}</p>}
        </li>
      ))}
    </ol>
  )
}
