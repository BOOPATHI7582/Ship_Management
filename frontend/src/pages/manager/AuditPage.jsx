import { useCallback, useEffect, useState } from 'react'
import { Pager } from '../../components/ui/admin'
import { ErrorAlert, TableSkeleton } from '../../components/ui/feedback'
import { fetchAuditLog } from '../../api/audit'

const ACTION_BADGE = {
  LOGIN_SUCCESS: 'bg-emerald-100 text-emerald-800',
  LOGIN_FAILED: 'bg-red-100 text-red-700',
  INVOICE_ISSUED: 'bg-sky-100 text-sky-800',
  INVOICE_CANCELLED: 'bg-red-100 text-red-700',
  SHIPMENT_CREATED: 'bg-navy-100 text-navy-700',
  SHIPMENT_PROGRESS: 'bg-indigo-100 text-indigo-800',
  DOCUMENT_DELETED: 'bg-amber-100 text-amber-800',
  REVIEW_APPROVED: 'bg-emerald-100 text-emerald-800',
  REVIEW_REJECTED: 'bg-amber-100 text-amber-800',
}

const ENTITY_TYPES = ['INVOICE', 'SHIPMENT', 'DOCUMENT', 'REVIEW', 'USER']

export default function AuditPage() {
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)
  const [page, setPage] = useState(0)
  const [action, setAction] = useState('')
  const [entityType, setEntityType] = useState('')
  const [search, setSearch] = useState('')
  const [query, setQuery] = useState('')
  const [detail, setDetail] = useState(null)

  const load = useCallback(() => {
    const params = { page, size: 15 }
    if (action) params.action = action
    if (entityType) params.entityType = entityType
    if (query) params.search = query
    fetchAuditLog(params)
      .then((res) => setData(res.data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load audit log'))
  }, [page, action, entityType, query])

  useEffect(() => { load() }, [load])

  if (error) return <ErrorAlert message={error} onRetry={load} />
  if (!data) return <TableSkeleton rows={8} />

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center gap-3">
        <select value={action} onChange={(e) => { setPage(0); setAction(e.target.value) }}
          className="rounded-lg border border-navy-100 bg-white px-3 py-2 text-sm">
          <option value="">All actions</option>
          <option value="LOGIN_SUCCESS">Login success</option>
          <option value="LOGIN_FAILED">Login failed</option>
          <option value="INVOICE_ISSUED">Invoice issued</option>
          <option value="INVOICE_CANCELLED">Invoice cancelled</option>
          <option value="SHIPMENT_CREATED">Shipment created</option>
          <option value="SHIPMENT_PROGRESS">Shipment progress</option>
          <option value="DOCUMENT_DELETED">Document deleted</option>
          <option value="REVIEW_APPROVED">Review approved</option>
          <option value="REVIEW_REJECTED">Review rejected</option>
        </select>
        <select value={entityType} onChange={(e) => { setPage(0); setEntityType(e.target.value) }}
          className="rounded-lg border border-navy-100 bg-white px-3 py-2 text-sm">
          <option value="">All entities</option>
          {ENTITY_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
        </select>
        <form
          className="flex flex-1 gap-2"
          onSubmit={(e) => { e.preventDefault(); setPage(0); setQuery(search) }}>
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search actor or payload…"
            className="w-full max-w-xs rounded-lg border border-navy-100 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-sky-200" />
          <button type="submit" className="rounded-lg bg-navy-900 px-4 py-2 text-sm font-semibold text-white hover:bg-navy-800">
            Search
          </button>
        </form>
      </div>

      <div className="overflow-x-auto rounded-xl border border-navy-100 bg-white shadow-sm">
        <table className="w-full text-left text-sm">
          <thead className="bg-navy-50 text-xs uppercase tracking-wide text-navy-500">
            <tr>
              <th className="px-5 py-3">When</th>
              <th className="px-5 py-3">Actor</th>
              <th className="px-5 py-3">Action</th>
              <th className="px-5 py-3 hidden md:table-cell">Entity</th>
              <th className="px-5 py-3 hidden lg:table-cell">IP</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-navy-50">
            {data.content.map((entry) => (
              <tr key={entry.id} onClick={() => setDetail(entry)}
                className="cursor-pointer hover:bg-navy-50/60">
                <td className="px-5 py-2.5 whitespace-nowrap text-navy-600">
                  {new Date(entry.createdAt).toLocaleString(undefined, {
                    day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit',
                  })}
                </td>
                <td className="px-5 py-2.5 text-navy-700">{entry.actorEmail || '-'}</td>
                <td className="px-5 py-2.5">
                  <span className={`rounded-full px-2 py-0.5 text-xs font-semibold ${ACTION_BADGE[entry.action] || 'bg-navy-100 text-navy-700'}`}>
                    {entry.action}
                  </span>
                </td>
                <td className="hidden px-5 py-2.5 text-navy-600 md:table-cell">
                  {entry.entityType}{entry.entityId ? ` #${entry.entityId}` : ''}
                </td>
                <td className="hidden px-5 py-2.5 font-mono text-xs text-navy-500 lg:table-cell">
                  {entry.ipAddress || '-'}
                </td>
              </tr>
            ))}
            {data.content.length === 0 && (
              <tr><td colSpan={5} className="px-5 py-8 text-center text-navy-400">No audit entries match these filters.</td></tr>
            )}
          </tbody>
        </table>
      </div>

      <Pager page={data.number} totalPages={data.totalPages} onPage={setPage} />

      {detail && (
        <div className="fixed inset-0 z-40 flex items-center justify-center bg-navy-950/50 p-4"
          onClick={() => setDetail(null)}>
          <div className="max-h-[80vh] w-full max-w-xl overflow-y-auto rounded-xl bg-white p-6 shadow-xl"
            onClick={(e) => e.stopPropagation()}>
            <div className="mb-4 flex items-start justify-between gap-4">
              <h3 className="text-base font-bold text-navy-900">{detail.action}</h3>
              <button onClick={() => setDetail(null)} className="text-sm text-navy-400 hover:text-navy-700">Close</button>
            </div>
            <dl className="space-y-2 text-sm">
              <div><dt className="inline font-semibold text-navy-500">Actor: </dt><dd className="inline">{detail.actorEmail || '-'}</dd></div>
              <div><dt className="inline font-semibold text-navy-500">Entity: </dt><dd className="inline">{detail.entityType}{detail.entityId ? ` #${detail.entityId}` : ''}</dd></div>
              <div><dt className="inline font-semibold text-navy-500">Time: </dt><dd className="inline">{new Date(detail.createdAt).toLocaleString()}</dd></div>
              <div><dt className="inline font-semibold text-navy-500">IP: </dt><dd className="inline font-mono">{detail.ipAddress || '-'}</dd></div>
            </dl>
            {[['Old value', detail.oldValue], ['New value', detail.newValue]].map(([label, value]) => (
              value ? (
                <div key={label} className="mt-3">
                  <p className="mb-1 text-xs font-semibold uppercase tracking-wide text-navy-500">{label}</p>
                  <pre className="overflow-x-auto rounded-lg bg-navy-50 p-3 text-xs text-navy-800">
                    {JSON.stringify(JSON.parse(value), null, 2)}
                  </pre>
                </div>
              ) : null
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
