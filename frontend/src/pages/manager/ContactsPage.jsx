import { useCallback, useEffect, useState } from 'react'
import { Modal, Pager, primaryBtnCls, secondaryBtnCls } from '../../components/ui/admin'
import { EmptyState, ErrorAlert, TableSkeleton } from '../../components/ui/feedback'
import { fetchContactMessages, markHandled, reopenMessage } from '../../api/contactMessages'
import { formatDate } from '../../api/invoices'

const STATUSES = ['NEW', 'HANDLED', 'ALL']

function StatusChip({ handled }) {
  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${
        handled ? 'bg-emerald-100 text-emerald-800' : 'bg-amber-100 text-amber-800'
      }`}
    >
      {handled ? 'Handled' : 'New'}
    </span>
  )
}

export default function ContactsPage() {
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)
  const [status, setStatus] = useState('NEW')
  const [searchInput, setSearchInput] = useState('')
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)
  const [detail, setDetail] = useState(null)
  const [busy, setBusy] = useState(false)

  const load = useCallback(() => {
    setLoading(true)
    const params = { status, page, size: 10 }
    if (search) params.search = search
    fetchContactMessages(params)
      .then((res) => setData(res.data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load messages'))
      .finally(() => setLoading(false))
  }, [status, page, search])

  useEffect(() => {
    load()
  }, [load])

  function submitSearch(e) {
    e.preventDefault()
    setPage(0)
    setSearch(searchInput.trim())
  }

  async function act(fn) {
    setBusy(true)
    try {
      await fn()
      setDetail(null)
      load()
    } catch (err) {
      setError(err.response?.data?.message || 'Action failed')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="font-display text-2xl font-bold text-navy-950">Contact Inbox</h1>
          <p className="mt-1 text-sm text-navy-500">
            Messages from the public contact form.
          </p>
        </div>
        <form onSubmit={submitSearch} className="flex gap-2">
          <input
            type="search"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            placeholder="Search name, email, subject…"
            className="w-full min-w-0 rounded-lg border border-navy-100 bg-white px-3.5 py-2 text-sm focus:border-gold-500 focus:outline-none focus:ring-1 focus:ring-gold-500 sm:w-64"
          />
          <button type="submit" className={`${primaryBtnCls} shrink-0 px-4 py-2 text-sm`}>
            Search
          </button>
        </form>
      </div>

      <div className="flex flex-wrap gap-1 rounded-xl border border-navy-100 bg-white p-1 shadow-sm sm:inline-flex">
        {STATUSES.map((s) => (
          <button
            key={s}
            type="button"
            onClick={() => {
              setStatus(s)
              setPage(0)
            }}
            className={`rounded-lg px-4 py-1.5 text-sm font-semibold transition ${
              status === s ? 'bg-navy-950 text-white' : 'text-navy-600 hover:bg-navy-50'
            }`}
          >
            {s === 'NEW' ? 'New' : s === 'HANDLED' ? 'Handled' : 'All'}
          </button>
        ))}
      </div>

      <ErrorAlert message={error} onRetry={load} />

      {loading && !data && <TableSkeleton rows={6} />}

      {!loading && data && data.content.length === 0 && (
        <EmptyState title="No messages here" hint="Public contact-form submissions will appear in this inbox." />
      )}

      {data && data.content.length > 0 && (
        <>
          {/* Desktop table */}
          <div className="hidden overflow-x-auto rounded-xl border border-navy-100 bg-white shadow-sm md:block">
            <table className="min-w-full divide-y divide-navy-100 text-left text-sm">
              <thead className="bg-navy-50 text-xs uppercase tracking-wide text-navy-500">
                <tr>
                  <th className="px-5 py-3">From</th>
                  <th className="px-5 py-3">Subject</th>
                  <th className="px-5 py-3">Received</th>
                  <th className="px-5 py-3">Status</th>
                  <th className="px-5 py-3" />
                </tr>
              </thead>
              <tbody className="divide-y divide-navy-100">
                {data.content.map((m) => (
                  <tr key={m.id} className="transition hover:bg-navy-50/60">
                    <td className="px-5 py-3.5">
                      <p className="font-semibold text-navy-900">{m.fullName}</p>
                      <p className="text-xs text-navy-500">{m.email}</p>
                      {m.company && <p className="text-xs text-navy-400">{m.company}</p>}
                    </td>
                    <td className="max-w-xs truncate px-5 py-3.5 text-navy-700">{m.subject}</td>
                    <td className="whitespace-nowrap px-5 py-3.5 text-navy-500">{formatDate(m.createdAt)}</td>
                    <td className="px-5 py-3.5"><StatusChip handled={m.handled} /></td>
                    <td className="px-5 py-3.5 text-right">
                      <button
                        type="button"
                        onClick={() => setDetail(m)}
                        className="rounded-lg border border-navy-200 px-3 py-1.5 text-xs font-semibold text-navy-700 transition hover:bg-navy-50"
                      >
                        View
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Mobile cards */}
          <div className="space-y-3 md:hidden">
            {data.content.map((m) => (
              <button
                key={m.id}
                type="button"
                onClick={() => setDetail(m)}
                className="block w-full rounded-xl border border-navy-100 bg-white p-4 text-left shadow-sm transition active:bg-navy-50"
              >
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0">
                    <p className="truncate font-semibold text-navy-900">{m.fullName}</p>
                    <p className="truncate text-xs text-navy-500">{m.email}</p>
                  </div>
                  <StatusChip handled={m.handled} />
                </div>
                <p className="mt-2 truncate text-sm font-medium text-navy-700">{m.subject}</p>
                <p className="mt-1 text-xs text-navy-400">{formatDate(m.createdAt)}</p>
              </button>
            ))}
          </div>

          <Pager page={data.number} totalPages={data.totalPages} onPage={setPage} />
        </>
      )}

      {detail && (
        <Modal title={detail.subject || 'Message'} onClose={() => setDetail(null)}>
          <div className="space-y-3 text-sm text-navy-700">
            <div className="grid grid-cols-1 gap-x-6 gap-y-2 sm:grid-cols-2">
              <p><span className="text-navy-400">From:</span> <span className="font-semibold">{detail.fullName}</span></p>
              <p><span className="text-navy-400">Email:</span> {detail.email}</p>
              {detail.phone && <p><span className="text-navy-400">Phone:</span> {detail.phone}</p>}
              {detail.company && <p><span className="text-navy-400">Company:</span> {detail.company}</p>}
              <p><span className="text-navy-400">Received:</span> {formatDate(detail.createdAt)}</p>
              <p><span className="text-navy-400">Status:</span> <StatusChip handled={detail.handled} /></p>
            </div>
            {detail.handled && detail.handledByName && (
              <p className="rounded-lg bg-emerald-50 px-3 py-2 text-xs text-emerald-800">
                Handled by {detail.handledByName}
                {detail.handledAt && ` on ${formatDate(detail.handledAt)}`}
              </p>
            )}
            <div className="rounded-lg bg-navy-50 p-4 whitespace-pre-wrap leading-relaxed">
              {detail.message}
            </div>
          </div>
          <div className="mt-5 flex flex-wrap gap-2">
            <a href={`mailto:${detail.email}?subject=Re: ${encodeURIComponent(detail.subject || '')}`} className={primaryBtnCls}>
              Reply by Email
            </a>
            {detail.handled ? (
              <button
                type="button"
                disabled={busy}
                onClick={() => act(() => reopenMessage(detail.id))}
                className={secondaryBtnCls}
              >
                Reopen
              </button>
            ) : (
              <button
                type="button"
                disabled={busy}
                onClick={() => act(() => markHandled(detail.id))}
                className={primaryBtnCls}
              >
                Mark Handled
              </button>
            )}
          </div>
        </Modal>
      )}
    </div>
  )
}
