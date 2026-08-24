import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  fetchManagerEnquiry,
  fetchThreadAsManager,
  sendOffer,
  updateEnquiryStatus,
} from '../../api/manager'
 import { downloadPdf, fetchQuotation, fetchQuotations, quotationPdfUrl, sendQuotation, viewPdf } from '../../api/quotations'
import QuotationBuilder from '../../components/manager/QuotationBuilder'
import { Field, inputCls, Modal, primaryBtnCls, secondaryBtnCls } from '../../components/ui/admin'

function Row({ label, value }) {
  return (
    <div className="flex justify-between gap-6 px-4 py-3 text-sm odd:bg-navy-50/60">
      <dt className="font-semibold text-navy-700">{label}</dt>
      <dd className="text-right text-navy-900">{value ?? '-'}</dd>
    </div>
  )
}

const messageStyles = {
  PROPOSED: 'bg-blue-50 border-blue-200',
  COUNTERED: 'bg-violet-50 border-violet-200',
  ACCEPTED: 'bg-emerald-50 border-emerald-300',
  REJECTED: 'bg-red-50 border-red-200',
  WITHDRAWN: 'bg-navy-50 border-navy-100 opacity-60',
}

const quoteStatusStyles = {
  DRAFT: 'bg-navy-100 text-navy-700',
  SENT: 'bg-blue-100 text-blue-800',
  VIEWED: 'bg-cyan-100 text-cyan-800',
  NEGOTIATING: 'bg-violet-100 text-violet-800',
  ACCEPTED: 'bg-emerald-100 text-emerald-800',
  REJECTED: 'bg-red-100 text-red-700',
  CANCELLED: 'bg-navy-50 text-navy-400 line-through',
  EXPIRED: 'bg-amber-100 text-amber-800',
  CONVERTED: 'bg-green-100 text-green-800',
}

export default function EnquiryDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [enquiry, setEnquiry] = useState(null)
  const [thread, setThread] = useState(null)
  const [error, setError] = useState(null)
  const [offer, setOffer] = useState({ offerPrice: '', message: '' })
  const [sending, setSending] = useState(false)
  const [actionMsg, setActionMsg] = useState(null)
  const [quotes, setQuotes] = useState([])
  const [builderQuote, setBuilderQuote] = useState(null)
  const [showBuilder, setShowBuilder] = useState(false)

  const load = useCallback(() => {
    Promise.all([fetchManagerEnquiry(id), fetchThreadAsManager(id), fetchQuotations({ enquiryId: id })])
      .then(([enqRes, threadRes, quotesRes]) => {
        setEnquiry(enqRes.data)
        setThread(threadRes.data)
        setQuotes(quotesRes.data.content || [])
      })
      .catch((err) => setError(err.response?.data?.message || 'Failed to load enquiry'))
  }, [id])

  useEffect(() => { load() }, [load])

  async function setStatus(status) {
    setActionMsg(null)
    try {
      const res = await updateEnquiryStatus(id, status)
      setEnquiry(res.data)
      setActionMsg(`Status changed to ${status.replace(/_/g, ' ')}`)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update status')
    }
  }

  async function submitOffer(e) {
    e.preventDefault()
    setSending(true)
    setActionMsg(null)
    try {
      const payload = {
        offerPrice: offer.offerPrice ? Number(offer.offerPrice) : null,
        message: offer.message || null,
      }
      const res = await sendOffer(id, payload)
      setThread(res.data)
      setOffer({ offerPrice: '', message: '' })
      load()
      setActionMsg('Offer sent to client')
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to send offer')
    } finally {
      setSending(false)
    }
  }

  async function doSendQuotation(quoteId) {
    setActionMsg(null)
    try {
      const res = await sendQuotation(quoteId)
      load()
      setActionMsg(`${res.data.quoteNo} sent to client`)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to send quotation')
    }
  }

  function doDownloadPdf(quote) {
    downloadPdf(quotationPdfUrl(quote.id), `${quote.quoteNo}.pdf`).catch(() => {})
  }

  function doViewPdf(quote) {
    viewPdf(quotationPdfUrl(quote.id))
  }

  if (error && !enquiry) {
    return (
      <div className="space-y-4">
        <Link to="/manager/enquiries" className="text-sm font-semibold text-navy-600 hover:underline">← Back to queue</Link>
        <div className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>
      </div>
    )
  }

  if (!enquiry) return <p className="text-sm text-navy-400">Loading enquiry…</p>

  return (
    <div className="space-y-6">
      <div>
        <Link to="/manager/enquiries" className="text-sm font-semibold text-navy-500 hover:underline">← Back to queue</Link>
        <h1 className="mt-2 font-display text-2xl font-bold text-navy-950">
          {enquiry.referenceNo}
          <span className="ml-3 rounded-full bg-blue-100 px-3 py-1 align-middle text-xs font-bold uppercase text-blue-800">
            {enquiry.status.replace(/_/g, ' ')}
          </span>
        </h1>
      </div>

      {actionMsg && <p className="rounded-lg bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-800">{actionMsg}</p>}
      {error && <p className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700">{error}</p>}

      <section className="rounded-2xl border border-navy-100 bg-white p-5 shadow-sm">
        <h2 className="font-display text-base font-bold text-navy-950">Client & Cargo</h2>
        <dl className="mt-3 divide-y divide-transparent rounded-xl border border-navy-100">
          <Row label="Client" value={`${enquiry.clientName ?? '-'} (${enquiry.clientEmail ?? '-'})`} />
          <Row label="Company" value={enquiry.companyName} />
          <Row label="Contact" value={enquiry.contactName ? `${enquiry.contactName} · ${enquiry.contactPhone ?? ''}` : null} />
          <Row label="Cargo" value={`${enquiry.cargoType}${enquiry.quantity ? ` — ${enquiry.quantity} ${enquiry.unit}` : ''}`} />
          <Row label="Category" value={enquiry.categoryName} />
          <Row label="Route" value={`${enquiry.originCountry ?? '-'} → ${enquiry.destinationCountry ?? '-'}`} />
          <Row label="Ports" value={`${enquiry.loadingPortName ?? '-'} → ${enquiry.destinationPortName ?? '-'}`} />
          <Row label="Budget" value={enquiry.estimatedBudget != null
            ? `${enquiry.currency} ${enquiry.estimatedBudget}` : enquiry.targetPricePerUnit != null
              ? `${enquiry.currency} ${enquiry.targetPricePerUnit}/unit target` : '-'} />
          <Row label="Notes" value={enquiry.message} />
        </dl>
      </section>

      <section className="rounded-2xl border border-navy-100 bg-white p-5 shadow-sm">
        <h2 className="font-display text-base font-bold text-navy-950">Actions</h2>
        <div className="mt-3 flex flex-wrap gap-2">
          {['REVIEWING', 'CONTACTED', 'REJECTED', 'CLOSED'].map((s) => (
            <button key={s} type="button" onClick={() => setStatus(s)} disabled={enquiry.status === s}
              className={`${secondaryBtnCls} disabled:opacity-30`}>
              Mark {s.replace(/_/g, ' ')}
            </button>
          ))}
        </div>
      </section>

      <section className="rounded-2xl border border-navy-100 bg-white p-5 shadow-sm">
        <div className="flex items-center justify-between">
          <h2 className="font-display text-base font-bold text-navy-950">Quotations</h2>
          <button type="button" onClick={() => { setBuilderQuote(null); setShowBuilder(true) }}
            className={primaryBtnCls}>New Quotation</button>
        </div>
        {quotes.length === 0 ? (
          <p className="mt-3 text-sm text-navy-500">
            No quotations yet. Create a draft once commercial terms are settled.
          </p>
        ) : (
          <ul className="mt-4 space-y-3">
            {quotes.map((quote) => (
              <li key={quote.id} className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-navy-100 px-4 py-3">
                <div>
                  <span className="font-display font-bold text-navy-950">{quote.quoteNo}</span>
                  <span className={`ml-2 rounded-full px-2 py-0.5 text-xs font-bold ${
                    quoteStatusStyles[quote.status] || 'bg-navy-100 text-navy-700'
                  }`}>{quote.status}</span>
                  <p className="mt-0.5 text-xs text-navy-400">
                    {quote.currency} {new Intl.NumberFormat('en-US').format(quote.grandTotal)}
                    {quote.validUntil ? ` · valid until ${quote.validUntil}` : ''}
                  </p>
                </div>
                <div className="flex gap-2">
                  <button type="button" onClick={() => doViewPdf(quote)} className={secondaryBtnCls}>View PDF</button>
                        <button type="button" onClick={() => doDownloadPdf(quote)} className={secondaryBtnCls}>PDF</button>
                  {quote.status === 'ACCEPTED' && (
                    <button
                      type="button"
                      onClick={() => navigate(`/manager/proforma?create=${quote.id}`)}
                      className={primaryBtnCls}
                    >
                      Generate PI
                    </button>
                  )}
                  {quote.status === 'DRAFT' && (
                    <>
                      <button type="button" onClick={async () => {
                        try {
                          const res = await fetchQuotation(quote.id)
                          setBuilderQuote(res.data)
                          setShowBuilder(true)
                        } catch (err) {
                          setError(err.response?.data?.message || 'Failed to load quotation')
                        }
                      }} className={secondaryBtnCls}>Edit</button>
                      <button type="button" onClick={() => doSendQuotation(quote.id)} className={primaryBtnCls}>Send</button>
                    </>
                  )}
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="rounded-2xl border border-navy-100 bg-white p-5 shadow-sm">
        <h2 className="font-display text-base font-bold text-navy-950">Negotiation History</h2>
        {!thread?.threadId && (
          <p className="mt-3 text-sm text-navy-500">No negotiation yet. Send the first offer below.</p>
        )}
        {thread?.messages?.length > 0 && (
          <ul className="mt-4 space-y-3">
            {[...thread.messages].reverse().map((msg) => (
              <li key={msg.id} className={`rounded-xl border p-4 text-sm ${messageStyles[msg.status] || 'border-navy-100'}`}>
                <div className="flex items-center justify-between gap-4">
                  <span className="font-bold text-navy-900">
                    {msg.senderType === 'ADMIN' ? 'Operations Team' : msg.senderName}
                    <span className={`ml-2 rounded-full px-2 py-0.5 text-xs font-semibold ${
                      msg.senderType === 'ADMIN' ? 'bg-navy-950/10 text-navy-800' : 'bg-gold-500/20 text-navy-800'
                    }`}>{msg.status}</span>
                  </span>
                  <span className="text-xs text-navy-400">{new Date(msg.createdAt).toLocaleString()}</span>
                </div>
                {msg.offerPrice != null && (
                  <p className="mt-2 font-display text-lg font-bold text-navy-950">
                    Offer: {enquiry.currency} {new Intl.NumberFormat('en-IN').format(msg.offerPrice)}
                  </p>
                )}
                {msg.message && <p className="mt-1 whitespace-pre-wrap text-navy-700">{msg.message}</p>}
              </li>
            ))}
          </ul>
        )}

        {thread?.agreedPrice != null && (
          <p className="mt-4 rounded-lg bg-emerald-50 px-4 py-3 text-sm font-bold text-emerald-900">
            Deal accepted at {enquiry.currency} {thread.agreedPrice}. Proceed to quotation.
          </p>
        )}

        <form onSubmit={submitOffer} className="mt-5 grid grid-cols-1 gap-4 border-t border-navy-100 pt-5 sm:grid-cols-3">
          <Field label="Offer Price (per unit)">
            <input type="number" step="0.01" min="0" className={inputCls} value={offer.offerPrice}
              onChange={(e) => setOffer({ ...offer, offerPrice: e.target.value })} placeholder={`${enquiry.currency}`} />
          </Field>
          <Field label="Message (optional)">
            <input className={inputCls} value={offer.message}
              onChange={(e) => setOffer({ ...offer, message: e.target.value })} placeholder="Terms, validity, notes…" />
          </Field>
          <div className="flex items-end">
            <button type="submit" disabled={sending} className={`${primaryBtnCls} w-full`}>
              {sending ? 'Sending…' : 'Send / Counter Offer'}
            </button>
          </div>
        </form>
      </section>

      {showBuilder && (
        <Modal title={builderQuote ? `Edit ${builderQuote.quoteNo || 'quotation'}` : 'New Quotation'} onClose={() => setShowBuilder(false)} wide>
          <QuotationBuilder
            enquiryId={id}
            currency={enquiry.currency}
            quotation={builderQuote}
            onClose={() => setShowBuilder(false)}
            onSaved={() => { setShowBuilder(false); load(); setActionMsg('Quotation saved') }}
          />
        </Modal>
      )}
    </div>
  )
}
