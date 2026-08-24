import { useCallback, useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { Modal, Pager, secondaryBtnCls, inputCls, primaryBtnCls } from '../../components/ui/admin'
import { EmptyState } from '../../components/ui/feedback'
import ShipmentTimeline from '../../components/ShipmentTimeline'
import { createReview, hasReviewed } from '../../api/reviews'
import { fetchMyShipment, fetchMyShipments, statusBadge, statusLabel, statusPercent } from '../../api/shipments'
import { money, formatDate } from '../../api/invoices'

export default function ShipmentsPage() {
  const { id } = useParams()
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)
  const [page, setPage] = useState(0)
  const [detail, setDetail] = useState(null)
  const [reviewed, setReviewed] = useState(false)
  const [reviewForm, setReviewForm] = useState(null)
  const [reviewError, setReviewError] = useState(null)
  const [savingReview, setSavingReview] = useState(false)

  const load = useCallback(() => {
    fetchMyShipments({ page, size: 10 })
      .then((res) => setData(res.data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load shipments'))
  }, [page])

  useEffect(() => { load() }, [load])

  useEffect(() => {
    if (!id) return
    openDetail(Number(id))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  async function openDetail(id) {
    try {
      const res = await fetchMyShipment(id)
      setDetail(res.data)
      setReviewed(false)
      setReviewForm(null)
      setReviewError(null)
      if (res.data.status === 'COMPLETED') {
        const exists = await hasReviewed(id)
        setReviewed(exists.data === true)
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load shipment')
    }
  }

  async function submitReview(e) {
    e.preventDefault()
    setSavingReview(true)
    setReviewError(null)
    try {
      await createReview({
        shipmentId: detail.id,
        rating: reviewForm.rating,
        title: reviewForm.title || null,
        reviewText: reviewForm.reviewText || null,
      })
      setReviewed(true)
      setReviewForm(null)
    } catch (err) {
      setReviewError(err.response?.data?.message || 'Failed to submit review')
    } finally {
      setSavingReview(false)
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-display text-2xl font-bold text-navy-950">My Shipments</h1>
        <p className="text-sm text-navy-500">Live tracking for every consignment - booking through delivery.</p>
      </div>

      {error && <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>}

      {data && (
        <>
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            {data.content.map((s) => (
              <div key={s.id} className="rounded-2xl border border-navy-100 bg-white p-5 shadow-sm transition hover:shadow-md">
                <div className="flex items-center justify-between gap-2">
                  <span className="font-semibold text-navy-950">{s.shipmentRef}</span>
                  <span className={`rounded-full px-2.5 py-1 text-xs font-bold ${statusBadge(s.status)}`}>{statusLabel(s.status)}</span>
                </div>
                <p className="mt-2 text-sm text-navy-600">
                  {[s.originCountry || s.loadingPortName, s.destinationCountry || s.destinationPortName].filter(Boolean).join(' → ') || 'Route to be confirmed'}
                </p>
                <p className="text-xs text-navy-400">{s.vesselName ? `MV ${s.vesselName}` : 'Vessel to be assigned'} · ETD {formatDate(s.loadingDate)} · ETA {formatDate(s.estimatedArrival)}</p>
                <div className="mt-3 h-2 overflow-hidden rounded-full bg-navy-100">
                  <div className="h-full rounded-full bg-sky-500" style={{ width: `${statusPercent(s.status)}%` }} />
                </div>
                <div className="mt-3 flex items-center justify-between">
                  {s.currentLocation
                    ? <span className="text-xs font-medium text-sky-700">📍 {s.currentLocation}</span>
                    : <span />}
                  <button type="button" onClick={() => openDetail(s.id)} className={secondaryBtnCls}>Track</button>
                </div>
              </div>
            ))}
            {data.content.length === 0 && (
              <div className="md:col-span-2">
                <EmptyState
                  icon="search"
                  title="No shipments yet"
                  hint="Once your order is booked you can follow every milestone here."
                />
              </div>
            )}
          </div>
          <Pager page={data.number} totalPages={data.totalPages} onPage={setPage} />
        </>
      )}

      {detail && (
        <Modal title={`${detail.shipmentRef} — live tracking`} onClose={() => setDetail(null)}>
          <div className="mb-4 flex items-center gap-3">
            <span className={`rounded-full px-3 py-1 text-xs font-bold ${statusBadge(detail.status)}`}>{statusLabel(detail.status)}</span>
            <div className="h-2 flex-1 overflow-hidden rounded-full bg-navy-100">
              <div className="h-full rounded-full bg-sky-500" style={{ width: `${statusPercent(detail.status)}%` }} />
            </div>
            <span className="text-xs font-semibold text-navy-500">{statusPercent(detail.status)}%</span>
          </div>

          <dl className="grid grid-cols-1 gap-x-6 gap-y-2 text-sm sm:grid-cols-2">
            <dt className="text-navy-500">Cargo</dt><dd>{detail.cargoName || '—'}{detail.quantity ? ` — ${Number(detail.quantity)} ${detail.unit || ''}` : ''}</dd>
            <dt className="text-navy-500">Current position</dt><dd>{detail.currentLocation || '—'}</dd>
            <dt className="text-navy-500">ETD → ETA</dt><dd>{formatDate(detail.loadingDate)} → {formatDate(detail.estimatedArrival)}</dd>
            <dt className="text-navy-500">Contract value</dt><dd>{money(detail.finalPrice, detail.currency)}</dd>
          </dl>

          <h3 className="mb-3 mt-6 text-xs font-bold uppercase tracking-wide text-navy-500">Tracking Timeline</h3>
          <ShipmentTimeline timeline={detail.timeline} />

          {detail.status === 'COMPLETED' && (
            <div className="mt-6 border-t border-navy-100 pt-4">
              <h3 className="mb-2 text-xs font-bold uppercase tracking-wide text-navy-500">Rate this shipment</h3>
              {reviewed ? (
                <p className="rounded-lg bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
                  ✓ Thank you - your review has been submitted and is pending moderation.
                </p>
              ) : reviewForm ? (
                <form onSubmit={submitReview} className="space-y-3 rounded-xl border border-navy-100 bg-navy-50/50 p-4">
                  <div className="flex items-center gap-1">
                    {[1, 2, 3, 4, 5].map((n) => (
                      <button
                        key={n}
                        type="button"
                        onClick={() => setReviewForm({ ...reviewForm, rating: n })}
                        className={`text-2xl leading-none transition ${n <= reviewForm.rating ? 'text-amber-500' : 'text-navy-200 hover:text-amber-300'}`}
                        aria-label={`${n} star${n > 1 ? 's' : ''}`}
                      >
                        ★
                      </button>
                    ))}
                  </div>
                  <input placeholder="Title (optional)" maxLength={150} className={inputCls} value={reviewForm.title} onChange={(e) => setReviewForm({ ...reviewForm, title: e.target.value })} />
                  <textarea placeholder="How was the shipping experience?" rows={3} maxLength={2000} className={inputCls} value={reviewForm.reviewText} onChange={(e) => setReviewForm({ ...reviewForm, reviewText: e.target.value })} />
                  {reviewError && <p className="text-sm text-red-600">{reviewError}</p>}
                  <div className="flex justify-end space-x-3">
                    <button type="button" onClick={() => setReviewForm(null)} className={secondaryBtnCls}>Cancel</button>
                    <button type="submit" disabled={savingReview || !reviewForm.rating} className={primaryBtnCls}>{savingReview ? 'Submitting…' : 'Submit Review'}</button>
                  </div>
                </form>
              ) : (
                <button type="button" onClick={() => setReviewForm({ rating: 5, title: '', reviewText: '' })} className={primaryBtnCls}>★ Leave a Review</button>
              )}
            </div>
          )}
        </Modal>
      )}
    </div>
  )
}
