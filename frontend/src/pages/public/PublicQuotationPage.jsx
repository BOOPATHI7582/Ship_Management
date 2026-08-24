import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { fetchQuotationByToken } from '../../api/quotations'

function Row({ label, value }) {
  return (
    <div className="flex justify-between gap-6 px-4 py-2.5 text-sm odd:bg-navy-50/60">
      <dt className="font-semibold text-navy-700">{label}</dt>
      <dd className="text-right text-navy-900">{value ?? '-'}</dd>
    </div>
  )
}

export default function PublicQuotationPage() {
  const { secureToken } = useParams()
  const [quote, setQuote] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    fetchQuotationByToken(secureToken)
      .then((res) => setQuote(res.data))
      .catch((err) => setError(err.response?.data?.message || 'Quotation not found or link expired'))
  }, [secureToken])

  if (error) {
    return (
      <div className="mx-auto max-w-2xl px-4 py-20 text-center">
        <h1 className="font-display text-2xl font-bold text-navy-950">Quotation unavailable</h1>
        <p className="mt-3 text-sm text-navy-500">{error}</p>
        <Link to="/" className="mt-6 inline-block rounded-lg bg-navy-950 px-6 py-2.5 text-sm font-bold text-white">
          Back to Home
        </Link>
      </div>
    )
  }

  if (!quote) return <p className="px-4 py-20 text-center text-sm text-navy-400">Loading quotation…</p>

  return (
    <div className="mx-auto max-w-3xl space-y-6 px-4 py-10">
      <div className="rounded-2xl border border-navy-100 bg-white p-8 shadow-sm">
        <div className="flex flex-wrap items-start justify-between gap-4 border-b border-navy-100 pb-5">
          <div>
            <p className="font-display text-xl font-black tracking-tight text-navy-950">GLOBAL EXPORT</p>
            <p className="text-xs text-navy-400">Cargo • Vessels • Worldwide Shipping</p>
          </div>
          <div className="text-right">
            <p className="font-display text-lg font-bold text-gold-600">QUOTATION</p>
            <p className="font-bold text-navy-950">{quote.quoteNo}</p>
            {quote.quotationDate && <p className="text-xs text-navy-400">Date: {quote.quotationDate}</p>}
            {quote.validUntil && <p className="text-xs text-navy-400">Valid until: {quote.validUntil}</p>}
          </div>
        </div>

        <dl className="mt-5 divide-y divide-transparent rounded-xl border border-navy-100">
          <Row label="Bill To" value={quote.clientCompanyName} />
          <Row label="Enquiry" value={quote.enquiryRef} />
          <Row label="Currency" value={quote.currency} />
          {quote.incoterms && <Row label="Incoterms" value={quote.incoterms} />}
        </dl>

        <table className="mt-6 w-full text-left text-sm">
          <thead className="bg-navy-950 text-xs uppercase tracking-wide text-white">
            <tr>
              <th className="px-3 py-2.5">Description</th>
              <th className="px-3 py-2.5 text-center">Qty</th>
              <th className="px-3 py-2.5">Unit</th>
              <th className="px-3 py-2.5 text-right">Rate</th>
              <th className="px-3 py-2.5 text-right">Amount</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-navy-100">
            {(quote.items || []).map((item, i) => (
              <tr key={item.id ?? i}>
                <td className="px-3 py-2.5">{item.description}</td>
                <td className="px-3 py-2.5 text-center">{item.quantity}</td>
                <td className="px-3 py-2.5">{item.unit ?? '-'}</td>
                <td className="px-3 py-2.5 text-right">{item.ratePerUnit}</td>
                <td className="px-3 py-2.5 text-right font-semibold">{item.lineAmount}</td>
              </tr>
            ))}
          </tbody>
        </table>

        <div className="ml-auto mt-4 max-w-xs space-y-1 text-sm">
          <div className="flex justify-between"><span className="text-navy-500">Subtotal</span><span>{quote.subtotal}</span></div>
          {Number(quote.discount) > 0 && (
            <div className="flex justify-between"><span className="text-navy-500">Discount</span><span>-{quote.discount}</span></div>
          )}
          <div className="flex justify-between"><span className="text-navy-500">Taxable amount</span><span>{quote.taxableAmount}</span></div>
          {Number(quote.taxAmount) > 0 && (
            <div className="flex justify-between">
              <span className="text-navy-500">
                Tax{quote.taxRateName ? ` (${quote.taxRateName})` : ''}
              </span>
              <span>{quote.taxAmount}</span>
            </div>
          )}
          <div className="flex justify-between border-t-2 border-navy-950 pt-1.5 font-display text-base font-bold text-navy-950">
            <span>Grand Total ({quote.currency})</span>
            <span>{new Intl.NumberFormat('en-US').format(quote.grandTotal)}</span>
          </div>
        </div>

        {quote.paymentTerms && (
          <div className="mt-6">
            <p className="text-xs font-bold uppercase tracking-wide text-navy-400">Payment Terms</p>
            <p className="whitespace-pre-wrap text-sm text-navy-700">{quote.paymentTerms}</p>
          </div>
        )}
        {quote.deliveryTerms && (
          <div className="mt-4">
            <p className="text-xs font-bold uppercase tracking-wide text-navy-400">Delivery Terms</p>
            <p className="whitespace-pre-wrap text-sm text-navy-700">{quote.deliveryTerms}</p>
          </div>
        )}
        {quote.notes && (
          <div className="mt-4">
            <p className="text-xs font-bold uppercase tracking-wide text-navy-400">Notes</p>
            <p className="whitespace-pre-wrap text-sm text-navy-700">{quote.notes}</p>
          </div>
        )}
        {quote.termsConditions && (
          <div className="mt-4">
            <p className="text-xs font-bold uppercase tracking-wide text-navy-400">Terms & Conditions</p>
            <p className="whitespace-pre-wrap text-sm text-navy-700">{quote.termsConditions}</p>
          </div>
        )}
      </div>

      <a href={`/api/public/quotations/${secureToken}/pdf`} target="_blank" rel="noreferrer"
        className="block rounded-xl bg-gold-500 py-3 text-center text-sm font-bold text-navy-950 transition hover:bg-gold-400">
        Download PDF
      </a>
    </div>
  )
}
