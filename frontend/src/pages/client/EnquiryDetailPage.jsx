import { useCallback, useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { fetchEnquiry } from '../../api/client'
import { acceptOffer, fetchThreadAsClient, rejectOffer, replyToThread } from '../../api/manager'
import {
  acceptQuotation,
  clientQuotationPdfUrl,
  downloadPdf,
  fetchClientQuotation,
  fetchEnquiryQuotations,
  rejectQuotation,
  viewPdf,
} from '../../api/quotations'
import { Field, inputCls, primaryBtnCls, secondaryBtnCls } from '../../components/ui/admin'

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

export default function EnquiryDetailPage() {
  const { id } = useParams()
  const [enquiry, setEnquiry] = useState(null)
  const [thread, setThread] = useState(null)
  const [error, setError] = useState(null)
  const [reply, setReply] = useState({ message: '', counterPrice: '' })
  const [sending, setSending] = useState(false)
  const [notice, setNotice] = useState(null)
  const [quotes, setQuotes] = useState([])
  const [expandedQuote, setExpandedQuote] = useState(null)

  const load = useCallback(() => {
    Promise.all([fetchEnquiry(id), fetchThreadAsClient(id), fetchEnquiryQuotations(id)])
      .then(([enqRes, threadRes, quotesRes]) => {
        setEnquiry(enqRes.data)
        setThread(threadRes.data)
        setQuotes(quotesRes.data || [])
      })
      .catch((err) => setError(err.response?.data?.message || 'Failed to load enquiry'))
  }, [id])

  useEffect(() => { load() }, [load])

  async function sendReply(e) {
    e.preventDefault()
    setSending(true)
    setNotice(null)
    try {
      const res = await replyToThread(id, {
        message: reply.message || null,
        counterPrice: reply.counterPrice ? Number(reply.counterPrice) : null,
      })
      setThread(res.data)
      setReply({ message: '', counterPrice: '' })
      setNotice(res.data.message || 'Message sent')
    } catch (err) {
      setNotice(err.response?.data?.message || 'Failed to send message')
    } finally {
      setSending(false)
    }
  }

  async function act(fn, messageId) {
    setNotice(null)
    try {
      const res = await fn(messageId)
      setThread(res.data)
      setNotice(res.data.message)
    } catch (err) {
      setNotice(err.response?.data?.message || 'Action failed')
    }
  }

  async function decide(quote, decision) {
    setNotice(null)
    let reason = null
    if (decision === 'REJECT') {
      reason = window.prompt('Tell us why you are declining (optional):')
      if (reason === null) return
    }
    try {
      const res = decision === 'ACCEPT' ? await acceptQuotation(quote.id) : await rejectQuotation(quote.id, reason)
      setExpandedQuote(null)
      load()
      setNotice(res.data.message || (decision === 'ACCEPT' ? 'Quotation accepted' : 'Quotation declined'))
    } catch (err) {
      setNotice(err.response?.data?.message || 'Action failed')
    }
  }

  async function review(quote) {
    if (expandedQuote && expandedQuote.id === quote.id) {
      setExpandedQuote(null)
      return
    }
    try {
      const res = await fetchClientQuotation(quote.id)
      setExpandedQuote(res.data)
    } catch (err) {
      setNotice(err.response?.data?.message || 'Failed to load quotation details')
    }
  }

  if (error) {
    return (
      <div className="space-y-4">
        <Link to="/client/enquiries" className="text-sm font-semibold text-navy-600 hover:underline">← Back to enquiries</Link>
        <div className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>
      </div>
    )
  }

  if (!enquiry) return <p className="text-sm text-navy-400">Loading enquiry...</p>

  const openManagerOffer = thread?.messages
    ?.filter((m) => m.senderType === 'ADMIN' && (m.status === 'PROPOSED' || m.status === 'COUNTERED'))
    .at(-1)

  return (
    <div className="space-y-6">
      <div>
        <Link to="/client/enquiries" className="text-sm font-semibold text-navy-500 hover:underline">← Back to enquiries</Link>
        <h1 className="mt-2 font-display text-2xl font-bold text-navy-950">
          {enquiry.referenceNo}
          <span className="ml-3 rounded-full bg-blue-100 px-3 py-1 align-middle text-xs font-bold uppercase text-blue-800">
            {(enquiry.status || '').replace(/_/g, ' ')}
          </span>
        </h1>
      </div>

      {notice && <p className="rounded-lg bg-gold-500/15 px-4 py-3 text-sm font-medium text-navy-800">{notice}</p>}

      <section className="rounded-2xl border border-navy-100 bg-white shadow-sm">
        <h2 className="border-b border-navy-100 px-5 py-3.5 font-display text-base font-bold text-navy-950">Requirement</h2>
        <dl className="divide-y divide-transparent p-2">
          <Row label="Cargo Type" value={enquiry.cargoType} />
          <Row label="Category" value={enquiry.categoryName} />
          <Row label="Description" value={enquiry.cargoDescription} />
          <Row label="Quantity" value={`${enquiry.quantity} ${enquiry.unit}`} />
          <Row label="Route" value={`${enquiry.originCountry} → ${enquiry.destinationCountry}`} />
          <Row label="Locations"
            value={`${enquiry.originLocation ?? '-'} → ${enquiry.destinationLocation ?? '-'}`} />
          <Row label="Ports"
            value={
              enquiry.loadingPortName || enquiry.destinationPortName
                ? `${enquiry.loadingPortName ?? '-'} (${enquiry.loadingPortCode ?? ''}) → ${enquiry.destinationPortName ?? '-'} (${enquiry.destinationPortCode ?? ''})`
                : null
            } />
          <Row label="Target Price"
            value={enquiry.targetPricePerUnit != null ? `${enquiry.currency} ${enquiry.targetPricePerUnit} / ${enquiry.unit}` : null} />
          <Row label="Estimated Budget"
            value={enquiry.estimatedBudget != null ? `${enquiry.currency} ${enquiry.estimatedBudget}` : null} />
          <Row label="Required Loading From"
            value={enquiry.requiredLoadingDate} />
          <Row label="Expected Delivery"
            value={enquiry.expectedDeliveryDate} />
          <Row label="Notes" value={enquiry.message} />
        </dl>
      </section>

      {thread?.agreedPrice != null && (
        <p className="rounded-xl bg-emerald-50 px-5 py-4 text-sm font-bold text-emerald-900">
          Deal accepted at {enquiry.currency} {thread.agreedPrice} per unit. Our team is preparing the quotation.
        </p>
      )}

      {quotes.length > 0 && (
        <section className="rounded-2xl border border-navy-100 bg-white p-5 shadow-sm">
          <h2 className="font-display text-base font-bold text-navy-950">Quotations</h2>
          <ul className="mt-4 space-y-3">
            {quotes.map((quote) => {
              const answerable = quote.status === 'SENT' || quote.status === 'VIEWED'
              return (
                <li key={quote.id} className="rounded-xl border border-navy-100 px-4 py-3">
                  <div className="flex flex-wrap items-center justify-between gap-3">
                    <div>
                      <span className="font-display font-bold text-navy-950">{quote.quoteNo}</span>
                      <span className={`ml-2 rounded-full px-2 py-0.5 text-xs font-bold ${
                        quote.status === 'ACCEPTED' ? 'bg-emerald-100 text-emerald-800'
                          : quote.status === 'REJECTED' ? 'bg-red-100 text-red-700'
                            : quote.status === 'CANCELLED' ? 'bg-navy-100 text-navy-400 line-through'
                              : 'bg-blue-100 text-blue-800'
                      }`}>{quote.status}</span>
                      <p className="mt-0.5 text-xs text-navy-400">
                        {quote.currency} {new Intl.NumberFormat('en-US').format(quote.grandTotal)}
                      </p>
                    </div>
                    <div className="flex flex-wrap gap-2">
                      <button type="button" onClick={() => review(quote)} className={secondaryBtnCls}>
                        {expandedQuote?.id === quote.id ? 'Hide details' : 'Review'}
                      </button>
                      <button type="button"
                        onClick={() => viewPdf(clientQuotationPdfUrl(quote.id))}
                        className={secondaryBtnCls}>View PDF</button>
                      <button type="button"
                        onClick={() => downloadPdf(clientQuotationPdfUrl(quote.id), `${quote.quoteNo}.pdf`)}
                        className={secondaryBtnCls}>Download PDF</button>
                      {answerable && (
                        <>
                          <button type="button" onClick={() => decide(quote, 'ACCEPT')} className={primaryBtnCls}>Accept</button>
                          <button type="button" onClick={() => decide(quote, 'REJECT')} className={secondaryBtnCls}>Decline</button>
                        </>
                      )}
                    </div>
                  </div>

                  {expandedQuote?.id === quote.id && (
                    <div className="mt-3 border-t border-navy-100 pt-3">
                      <table className="w-full text-left text-sm">
                        <thead className="text-xs uppercase tracking-wide text-navy-400">
                          <tr>
                            <th className="py-1.5">Description</th>
                            <th className="py-1.5 text-center">Qty</th>
                            <th className="py-1.5">Unit</th>
                            <th className="py-1.5 text-right">Rate</th>
                            <th className="py-1.5 text-right">Amount</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-navy-100">
                          {(expandedQuote.items || []).map((item) => (
                            <tr key={item.id}>
                              <td className="py-1.5 pr-2">{item.description}</td>
                              <td className="py-1.5 text-center">{item.quantity}</td>
                              <td className="py-1.5">{item.unit ?? '-'}</td>
                              <td className="py-1.5 text-right">{item.ratePerUnit}</td>
                              <td className="py-1.5 text-right font-semibold">{item.lineAmount}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                      <dl className="mt-3 space-y-1 text-sm">
                        <div className="flex justify-between"><dt className="text-navy-500">Taxable amount</dt><dd>{expandedQuote.taxableAmount}</dd></div>
                        <div className="flex justify-between">
                          <dt className="text-navy-500">Tax {expandedQuote.taxTreatment ? `(${expandedQuote.taxTreatment.replace(/_/g, ' ')})` : ''}</dt>
                          <dd>{expandedQuote.taxAmount}</dd>
                        </div>
                        <div className="flex justify-between border-t border-navy-100 pt-1.5 font-display text-base font-bold text-navy-950">
                          <dt>Grand Total ({expandedQuote.currency})</dt>
                          <dd>{new Intl.NumberFormat('en-US').format(expandedQuote.grandTotal)}</dd>
                        </div>
                      </dl>
                      {expandedQuote.validUntil && (
                        <p className="mt-2 text-xs text-navy-400">Valid until {expandedQuote.validUntil}</p>
                      )}
                    </div>
                  )}
                </li>
              )
            })}
          </ul>
        </section>
      )}

      {(thread?.threadId || openManagerOffer) && (
        <section className="rounded-2xl border border-navy-100 bg-white p-5 shadow-sm">
          <h2 className="font-display text-base font-bold text-navy-950">Negotiation</h2>
          {thread?.messages?.length > 0 && (
            <ul className="mt-4 space-y-3">
              {[...thread.messages].reverse().map((msg) => (
                <li key={msg.id} className={`rounded-xl border p-4 text-sm ${messageStyles[msg.status] || 'border-navy-100'}`}>
                  <div className="flex items-center justify-between gap-4">
                    <span className="font-bold text-navy-900">
                      {msg.senderType === 'ADMIN' ? 'Operations Team' : 'You'}
                      <span className={`ml-2 rounded-full px-2 py-0.5 text-xs font-semibold ${
                        msg.senderType === 'ADMIN' ? 'bg-navy-950/10 text-navy-800' : 'bg-gold-500/20 text-navy-800'
                      }`}>{msg.status}</span>
                    </span>
                    <span className="text-xs text-navy-400">{new Date(msg.createdAt).toLocaleString()}</span>
                  </div>
                  {msg.offerPrice != null && (
                    <p className="mt-2 font-display text-lg font-bold text-navy-950">
                      Offer: {enquiry.currency} {new Intl.NumberFormat('en-US').format(msg.offerPrice)}
                    </p>
                  )}
                  {msg.message && <p className="mt-1 whitespace-pre-wrap text-navy-700">{msg.message}</p>}
                  {openManagerOffer && msg.id === openManagerOffer.id && thread.status === 'OPEN' && (
                    <div className="mt-3 flex gap-2">
                      <button type="button" onClick={() => act(acceptOffer, msg.id)} className={primaryBtnCls}>Accept Offer</button>
                      <button type="button" onClick={() => act(rejectOffer, msg.id)} className={secondaryBtnCls}>Decline</button>
                    </div>
                  )}
                </li>
              ))}
            </ul>
          )}

          {thread?.status === 'OPEN' && (
            <form onSubmit={sendReply} className="mt-5 grid grid-cols-1 gap-4 border-t border-navy-100 pt-5 sm:grid-cols-3">
              <Field label="Your Reply">
                <input className={inputCls} value={reply.message}
                  onChange={(e) => setReply({ ...reply, message: e.target.value })} placeholder="Questions or comments…" />
              </Field>
              <Field label="Counter Price (optional)">
                <input type="number" step="0.01" min="0" className={inputCls} value={reply.counterPrice}
                  onChange={(e) => setReply({ ...reply, counterPrice: e.target.value })} />
              </Field>
              <div className="flex items-end">
                <button type="submit" disabled={sending} className={`${primaryBtnCls} w-full`}>
                  {sending ? 'Sending…' : 'Send'}
                </button>
              </div>
            </form>
          )}
        </section>
      )}
    </div>
  )
}
