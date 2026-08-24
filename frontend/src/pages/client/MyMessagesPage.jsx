import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Pager } from '../../components/ui/admin'
import { EmptyState, ErrorAlert, TableSkeleton } from '../../components/ui/feedback'
import { fetchMyMessages } from '../../api/contactMessages'
import { formatDate } from '../../api/invoices'

function StatusChip({ handled }) {
  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${
        handled ? 'bg-emerald-100 text-emerald-800' : 'bg-amber-100 text-amber-800'
      }`}
    >
      {handled ? 'Responded' : 'Pending'}
    </span>
  )
}

export default function MyMessagesPage() {
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)

  const load = useCallback(() => {
    setLoading(true)
    fetchMyMessages({ page, size: 10 })
      .then((res) => setData(res.data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load messages'))
      .finally(() => setLoading(false))
  }, [page])

  useEffect(() => {
    load()
  }, [load])

  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-display text-2xl font-bold text-navy-950">My Messages</h1>
        <p className="mt-1 text-sm text-navy-500">
          Enquiries you sent through the public contact form.
        </p>
      </div>

      <ErrorAlert message={error} onRetry={load} />

      {loading && !data && <TableSkeleton rows={4} />}

      {!loading && data && data.content.length === 0 && (
        <EmptyState title="No messages yet" hint="Messages you send via the public contact form will show up here with their status.">
          <Link to="/contact" className="btn-primary mt-4 inline-block">Contact Us</Link>
        </EmptyState>
      )}

      {data && data.content.length > 0 && (
        <>
          <div className="grid gap-3 sm:grid-cols-2">
            {data.content.map((m) => (
              <div key={m.id} className="rounded-xl border border-navy-100 bg-white p-5 shadow-sm">
                <div className="flex items-start justify-between gap-3">
                  <h2 className="min-w-0 truncate font-semibold text-navy-900">{m.subject}</h2>
                  <StatusChip handled={m.handled} />
                </div>
                <p className="mt-2 line-clamp-3 whitespace-pre-wrap text-sm leading-relaxed text-navy-600">
                  {m.message}
                </p>
                <p className="mt-3 text-xs text-navy-400">Sent {formatDate(m.createdAt)}</p>
              </div>
            ))}
          </div>
          <Pager page={data.number} totalPages={data.totalPages} onPage={setPage} />
        </>
      )}
    </div>
  )
}
