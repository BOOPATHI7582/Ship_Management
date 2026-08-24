import { useCallback, useEffect, useState } from 'react'
import { Modal, Pager, primaryBtnCls, secondaryBtnCls } from '../../components/ui/admin'
import { EmptyState } from '../../components/ui/feedback'
import Stars from '../../components/Stars'
import { fetchReviews, approveReview, rejectReview } from '../../api/reviews'
import { formatDate } from '../../api/invoices'

const MODERATION_BADGE = {
  PENDING: 'bg-amber-100 text-amber-800',
  APPROVED: 'bg-emerald-100 text-emerald-800',
  REJECTED: 'bg-navy-100 text-navy-500',
}

export default function ReviewsPage() {
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)
  const [page, setPage] = useState(0)
  const [moderation, setModeration] = useState('PENDING')
  const [search, setSearch] = useState('')
  const [detail, setDetail] = useState(null)
  const [busy, setBusy] = useState(false)

  const load = useCallback(() => {
    const params = { moderation, page, size: 10 }
    if (search) params.search = search
    fetchReviews(params)
      .then((res) => setData(res.data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load reviews'))
  }, [moderation, page, search])

  useEffect(() => { load() }, [load])

  async function act(fn) {
    setBusy(true)
    setError(null)
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
      <div>
        <h1 className="font-display text-2xl font-bold text-navy-950">Reviews</h1>
        <p className="text-sm text-navy-500">Moderate client feedback before it appears on the public site.</p>
      </div>

      {error && <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>}

      <div className="flex flex-wrap gap-3">
        {['PENDING', 'APPROVED', 'ALL'].map((m) => (
          <button
            key={m}
            type="button"
            onClick={() => { setPage(0); setModeration(m) }}
            className={`rounded-full px-4 py-1.5 text-sm font-semibold transition ${moderation === m ? 'bg-navy-900 text-white' : 'border border-navy-200 bg-white text-navy-600 hover:bg-navy-50'}`}
          >
            {m === 'PENDING' ? 'Pending moderation' : m.charAt(0) + m.slice(1).toLowerCase()}
          </button>
        ))}
        <input placeholder="Search ref / client / text…" value={search} onChange={(e) => { setPage(0); setSearch(e.target.value) }} className="input max-w-xs rounded-xl border border-navy-200 bg-white px-3.5 py-2 text-sm" />
      </div>

      {data && (
        <>
          <div className="overflow-hidden rounded-2xl border border-navy-100 bg-white shadow-sm">
            <table className="w-full text-left text-sm">
              <thead className="bg-navy-50 text-xs uppercase tracking-wide text-navy-500">
                <tr>
                  <th className="px-5 py-3">Client</th>
                  <th className="px-5 py-3 hidden lg:table-cell">Shipment</th>
                  <th className="px-5 py-3">Rating</th>
                  <th className="px-5 py-3 hidden md:table-cell">Title</th>
                  <th className="px-5 py-3 hidden xl:table-cell">Date</th>
                  <th className="px-5 py-3">Status</th>
                  <th className="px-5 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-navy-100">
                {data.content.map((r) => (
                  <tr key={r.id}>
                    <td className="px-5 py-3.5 font-semibold text-navy-950">{r.clientName || '—'}</td>
                    <td className="hidden px-5 py-3.5 text-navy-600 lg:table-cell">{r.shipmentRef || '—'}</td>
                    <td className="px-5 py-3.5"><Stars rating={r.rating} /></td>
                    <td className="hidden max-w-[220px] truncate px-5 py-3.5 text-navy-600 md:table-cell">{r.title || r.reviewText || '—'}</td>
                    <td className="hidden px-5 py-3.5 text-navy-600 xl:table-cell">{formatDate(r.createdAt)}</td>
                    <td className="px-5 py-3.5">
                      <span className={`rounded-full px-2.5 py-1 text-xs font-bold ${MODERATION_BADGE[r.approved ? 'APPROVED' : 'REJECTED']}`}>
                        {r.approved ? 'Published' : 'Hidden'}
                      </span>
                    </td>
                    <td className="whitespace-nowrap px-5 py-3.5 text-right">
                      <button type="button" onClick={() => setDetail(r)} className={secondaryBtnCls}>View</button>
                      {!r.approved && (
                        <button type="button" disabled={busy} onClick={() => act(() => approveReview(r.id))} className={`${primaryBtnCls} ml-2`}>Approve</button>
                      )}
                      {r.approved && (
                        <button type="button" disabled={busy} onClick={() => act(() => rejectReview(r.id))} className="ml-2 rounded-lg border border-red-200 px-3 py-1.5 text-sm font-semibold text-red-700 transition hover:bg-red-50">Unpublish</button>
                      )}
                    </td>
                  </tr>
                ))}
                {data.content.length === 0 && (
                  <tr>
                    <td colSpan={7}>
                      <EmptyState
                        icon="doc"
                        title="No reviews in this view"
                        hint="Clients can review shipments once they are completed and delivered."
                      />
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
          <Pager page={data.number} totalPages={data.totalPages} onPage={setPage} />
        </>
      )}

      {detail && (
        <Modal title={`Review — ${detail.shipmentRef || ''}`} onClose={() => setDetail(null)}>
          <div className="space-y-3 text-sm">
            <div className="flex items-center justify-between">
              <Stars rating={detail.rating} size="text-xl" />
              <span className={`rounded-full px-2.5 py-1 text-xs font-bold ${detail.approved ? 'bg-emerald-100 text-emerald-800' : 'bg-navy-100 text-navy-500'}`}>
                {detail.approved ? 'Published' : 'Hidden'}
              </span>
            </div>
            {detail.title && <p className="text-base font-bold text-navy-950">{detail.title}</p>}
            <p className="whitespace-pre-line text-navy-700">{detail.reviewText || '—'}</p>
            <dl className="grid grid-cols-2 gap-x-6 gap-y-1 border-t border-navy-100 pt-3 text-xs">
              <dt className="text-navy-400">Client</dt><dd>{detail.clientName || '—'}</dd>
              <dt className="text-navy-400">Submitted</dt><dd>{formatDate(detail.createdAt)}</dd>
              {detail.moderatedByEmail && (<><dt className="text-navy-400">Moderated by</dt><dd>{detail.moderatedByEmail}</dd></>)}
            </dl>
            <div className="flex justify-end space-x-3 pt-2">
              {!detail.approved
                ? <button type="button" disabled={busy} onClick={() => act(() => approveReview(detail.id))} className={primaryBtnCls}>Approve &amp; Publish</button>
                : <button type="button" disabled={busy} onClick={() => act(() => rejectReview(detail.id))} className={secondaryBtnCls}>Unpublish</button>}
            </div>
          </div>
        </Modal>
      )}
    </div>
  )
}
