import { useCallback, useEffect, useState } from 'react'
import { Pager, secondaryBtnCls, primaryBtnCls, Field } from '../../components/ui/admin'
import { EmptyState } from '../../components/ui/feedback'
import {
  downloadClientInvoicePdf,
  fetchMyInvoices,
  formatDate,
  money,
  clientInvoicePdfUrl,
} from '../../api/invoices'
import { viewPdf } from '../../api/quotations'
import { createOrder, verifyPayment } from '../../api/payments'

const statusBadge = (status) => {
  const map = {
    ISSUED: 'bg-blue-100 text-blue-800',
    PARTIALLY_PAID: 'bg-amber-100 text-amber-800',
    PAID: 'bg-emerald-100 text-emerald-800',
    OVERDUE: 'bg-red-100 text-red-700',
    CANCELLED: 'bg-navy-100 text-navy-500',
  }
  return map[status] || 'bg-navy-100 text-navy-700'
}

function loadRazorpayScript() {
  return new Promise((resolve) => {
    if (window.Razorpay) return resolve(true)
    const script = document.createElement('script')
    script.src = 'https://checkout.razorpay.com/v1/checkout.js'
    script.onload = () => resolve(true)
    script.onerror = () => resolve(false)
    document.body.appendChild(script)
  })
}

function PayModal({ invoice, onClose, onPaid }) {
  const [stage, setStage] = useState('creating') // creating | mock | ready | done
  const [order, setOrder] = useState(null)
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    let alive = true
    createOrder(invoice.id)
      .then((res) => {
        if (!alive) return
        setOrder(res.data)
        if (res.data.mockPaymentId) setStage('mock')
        else setStage('ready')
      })
      .catch((err) => setError(err.response?.data?.message || 'Could not start checkout'))
    return () => { alive = false }
  }, [invoice.id])

  const finalize = (orderId, paymentId, signature) => {
    setBusy(true)
    verifyPayment({ razorpayOrderId: orderId, razorpayPaymentId: paymentId, razorpaySignature: signature })
      .then(() => { setStage('done'); onPaid() })
      .catch((err) => setError(err.response?.data?.message || 'Payment verification failed'))
      .finally(() => setBusy(false))
  }

  const payWithGateway = async () => {
    const ok = await loadRazorpayScript()
    if (!ok) return setError('Unable to load payment gateway')
    const rz = new window.Razorpay({
      key: order.razorpayKeyId,
      amount: Number(order.amount) * 100,
      currency: order.currency,
      name: 'ExportPlatform',
      description: `Invoice ${invoice.invoiceNo}`,
      order_id: order.razorpayOrderId,
      handler: (resp) => finalize(resp.razorpay_order_id, resp.razorpay_payment_id, resp.razorpay_signature),
      theme: { color: '#0f2a43' },
    })
    rz.open()
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-navy-950/50 p-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl">
        {error && (
          <div className="mb-4 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{error}</div>
        )}
        <h3 className="font-display text-lg font-bold text-navy-950">Pay {money(invoice.balanceAmount, invoice.currency)}</h3>
        <p className="mt-1 text-sm text-navy-500">Tax invoice {invoice.invoiceNo}</p>

        {stage === 'creating' && <p className="mt-6 text-sm text-navy-500">Starting secure checkout…</p>}

        {stage === 'mock' && (
          <div className="mt-5 space-y-3">
            <div className="rounded-lg bg-amber-50 px-3 py-2 text-xs text-amber-800">
              Dev gateway active (no Razorpay keys configured). Confirming here runs the identical
              signature-verified capture path used in production.
            </div>
            <div className="text-xs text-navy-500">Order <span className="font-mono">{order?.razorpayOrderId}</span></div>
            <button type="button" disabled={busy} onClick={() => finalize(order.razorpayOrderId, order.mockPaymentId, order.mockSignature)} className={`${primaryBtnCls} w-full`}>
              {busy ? 'Verifying…' : `Simulate successful payment of ${money(invoice.balanceAmount, invoice.currency)}`}
            </button>
          </div>
        )}

        {stage === 'ready' && (
          <button type="button" onClick={payWithGateway} className={`${primaryBtnCls} mt-5 w-full`}>
            Pay with UPI / Cards / NetBanking
          </button>
        )}

        {stage === 'done' && (
          <div className="mt-5 rounded-lg bg-emerald-50 px-3 py-3 text-sm font-semibold text-emerald-700">
            Payment captured — invoice balance updated.
          </div>
        )}

        <button type="button" onClick={onClose} className={`${secondaryBtnCls} mt-4 w-full`}>
          {stage === 'done' ? 'Close' : 'Cancel'}
        </button>
      </div>
    </div>
  )
}

export default function InvoicesPage() {
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)
  const [page, setPage] = useState(0)
  const [paying, setPaying] = useState(null)

  const load = useCallback(() => {
    fetchMyInvoices({ page, size: 10 })
      .then((res) => setData(res.data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load tax invoices'))
  }, [page])

  useEffect(() => { load() }, [load])

  return (
    <div className="space-y-5">
      <div>
        <h1 className="font-display text-2xl font-bold text-navy-950">Tax Invoices</h1>
        <p className="text-sm text-navy-500">Final GST documents issued for your orders.</p>
      </div>

      {error && (
        <div className="flex items-center justify-between rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700">
          <span>{error}</span>
          <button type="button" onClick={() => setError(null)} className="font-bold">×</button>
        </div>
      )}

      {data && (
        <>
          <div className="overflow-x-auto rounded-2xl border border-navy-100 bg-white shadow-sm">
            <table className="w-full text-left text-sm">
              <thead className="bg-navy-50 text-xs uppercase tracking-wide text-navy-500">
                <tr>
                  <th className="px-5 py-3">Invoice No</th>
                  <th className="px-5 py-3 hidden md:table-cell">Issued</th>
                  <th className="px-5 py-3 hidden md:table-cell">Due</th>
                  <th className="px-5 py-3 text-right">Total</th>
                  <th className="px-5 py-3 text-right hidden sm:table-cell">Balance</th>
                  <th className="px-5 py-3">Status</th>
                  <th className="px-5 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-navy-100">
                {data.content.map((inv) => (
                  <tr key={inv.id}>
                    <td className="px-5 py-3.5 font-semibold text-navy-950">{inv.invoiceNo}</td>
                    <td className="hidden px-5 py-3.5 text-navy-600 md:table-cell">{formatDate(inv.issueDate)}</td>
                    <td className="hidden px-5 py-3.5 text-navy-600 md:table-cell">{formatDate(inv.dueDate)}</td>
                    <td className="px-5 py-3.5 text-right font-mono text-navy-900">{money(inv.grandTotal, inv.currency)}</td>
                    <td className="hidden px-5 py-3.5 text-right font-mono text-navy-700 sm:table-cell">{money(inv.balanceAmount, inv.currency)}</td>
                    <td className="px-5 py-3.5">
                      <span className={`rounded-full px-2.5 py-1 text-xs font-bold ${statusBadge(inv.status)}`}>
                        {inv.status.replace(/_/g, ' ')}
                      </span>
                    </td>
                    <td className="px-5 py-3.5">
                      <div className="flex items-center justify-end gap-2">
                        {inv.status !== 'CANCELLED' && Number(inv.balanceAmount) > 0 && (
                          <button
                            type="button"
                            onClick={() => setPaying(inv)}
                            className="inline-flex items-center gap-1.5 rounded-2xl bg-gradient-to-r from-gold-400 via-gold-500 to-gold-600 px-4 py-2 text-xs font-bold text-navy-950 shadow-lg shadow-gold-500/30 transition hover:shadow-xl hover:shadow-gold-500/50 hover:brightness-105 active:scale-95"
                          >
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="h-4 w-4">
                              <rect x="2.5" y="5" width="19" height="14" rx="2.5" />
                              <path strokeLinecap="round" d="M2.5 10h19" />
                            </svg>
                            Pay Now
                          </button>
                        )}
                        {inv.status !== 'CANCELLED' ? (
                          <>
                            <button
                              type="button"
                              onClick={() => viewPdf(clientInvoicePdfUrl(inv.id))}
                              className="inline-flex items-center gap-1.5 rounded-2xl bg-gradient-to-r from-sky-500 to-blue-600 px-4 py-2 text-xs font-bold text-white shadow-lg shadow-sky-500/30 transition hover:shadow-xl hover:shadow-sky-500/50 hover:brightness-105 active:scale-95"
                            >
                              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="h-4 w-4">
                                <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                <path strokeLinecap="round" strokeLinejoin="round" d="M2.458 12C3.732 7.943 7.523 5.5 12 5.5s8.268 2.443 9.542 6.5c-1.274 4.057-5.065 6.5-9.542 6.5S3.732 16.057 2.458 12z" />
                              </svg>
                              View
                            </button>
                            <button
                              type="button"
                              onClick={() => downloadClientInvoicePdf(inv.id, inv.invoiceNo)}
                              className="inline-flex items-center gap-1.5 rounded-2xl bg-gradient-to-r from-emerald-500 to-teal-600 px-4 py-2 text-xs font-bold text-white shadow-lg shadow-emerald-500/30 transition hover:shadow-xl hover:shadow-emerald-500/50 hover:brightness-105 active:scale-95"
                            >
                              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="h-4 w-4">
                                <path strokeLinecap="round" strokeLinejoin="round" d="M12 3v10m0 0l4-4m-4 4l-4-4" />
                                <path strokeLinecap="round" strokeLinejoin="round" d="M4 17v2a2 2 0 002 2h12a2 2 0 002-2v-2" />
                              </svg>
                              Download
                            </button>
                          </>
                        ) : (
                          <span className="text-xs text-navy-400">—</span>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
                {data.content.length === 0 && (
                  <tr><td colSpan={7}><EmptyState icon="invoice" title="No tax invoices yet" hint="Invoices appear here once our team bills your enquiry." /></td></tr>
                )}
              </tbody>
            </table>
          </div>
          <Pager page={data.number} totalPages={data.totalPages} onPage={setPage} />
        </>
      )}

      <p className="text-xs text-navy-400">
        Payments are reconciled automatically; your invoice status updates as funds are verified.
      </p>

      {paying && (
        <PayModal
          invoice={paying}
          onClose={() => setPaying(null)}
          onPaid={() => load()}
        />
      )}
    </div>
  )
}
