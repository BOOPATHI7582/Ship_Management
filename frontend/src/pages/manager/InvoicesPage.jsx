import { useCallback, useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Field, Modal, Pager, inputCls, primaryBtnCls, secondaryBtnCls } from '../../components/ui/admin'
import { EmptyState } from '../../components/ui/feedback'
import { fetchProforma, managerProformaPdfUrl } from '../../api/proforma'
import { recordOfflinePayment } from '../../api/payments'
import { viewPdf } from '../../api/quotations'
import {
  INVOICE_STATUSES,
  cancelInvoice,
  downloadManagerInvoicePdf,
  fetchInvoice,
  fetchInvoices,
  formatDate,
  issueInvoice,
  money,
  sendInvoice,
} from '../../api/invoices'

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

const emptyItem = { description: '', hsnCode: '', quantity: '', unit: 'MT', ratePerUnit: '' }
const emptyOffline = { invoiceId: null, method: 'NEFT', transactionReference: '', amount: '', notes: '' }

function buildFormFromPi(pi, invoiceType = 'TAX_INVOICE') {
  return {
    proformaInvoiceId: pi.id,
    piNo: pi.piNo,
    invoiceType,
    clientName: pi.clientCompanyName || '',
    currency: pi.currency || 'INR',
    dueDate: '',
    placeOfSupply: '',
    exchangeRate: '',
    portOfLoading: '',
    portOfDischarge: '',
    exportReference: '',
    discount: Number(pi.discount ?? 0),
    freightCharges: Number(pi.freightCharges ?? 0),
    loadingCharges: Number(pi.loadingCharges ?? 0),
    documentationCharges: Number(pi.documentationCharges ?? 0),
    insuranceCharges: Number(pi.insuranceCharges ?? 0),
    otherCharges: Number(pi.otherCharges ?? 0),
    additionalCharges: 0,
    invoiceType: 'TAX_INVOICE',
    taxTreatment: pi.taxTreatment || 'EXEMPT',
    paymentTerms: pi.paymentTerms || '',
    bankDetails: pi.bankDetails || '',
    notes: '',
    termsConditions:
      'Goods once sold will not be taken back. Interest @18% p.a. applies on overdue amounts. Subject to Mumbai jurisdiction.',
    items: (pi.items || []).map((it) => ({
      description: it.description,
      hsnCode: it.hsnCode || '',
      quantity: String(Number(it.quantity)),
      unit: it.unit || '',
      ratePerUnit: String(Number(it.ratePerUnit)),
    })),
  }
}

export default function InvoicesPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)
  const [page, setPage] = useState(0)
  const [statusFilter, setStatusFilter] = useState('')
  const [form, setForm] = useState(null)
  const [saving, setSaving] = useState(false)
  const [detail, setDetail] = useState(null)
  const [offline, setOffline] = useState(null)
  const [actionError, setActionError] = useState(null)

  const load = useCallback(() => {
    const params = { page, size: 10 }
    if (statusFilter) params.status = statusFilter
    fetchInvoices(params)
      .then((res) => setData(res.data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load invoices'))
  }, [page, statusFilter])

  useEffect(() => { load() }, [load])

  async function saveOffline() {
    setSaving(true)
    setActionError(null)
    try {
      await recordOfflinePayment({
        invoiceId: offline.invoiceId,
        method: offline.method,
        transactionReference: offline.transactionReference,
        amount: Number(offline.amount),
        notes: offline.notes,
      })
      setOffline(null)
      setDetail(null)
      load()
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to record payment')
    } finally {
      setSaving(false)
    }
  }

  // Deep link: /manager/invoices?create=<piId>
  useEffect(() => {
    const piId = searchParams.get('create')
    if (!piId || form) return
    fetchProforma(piId)
      .then((res) => {
        const pi = res.data
        if (!['SENT', 'PAYMENT_PENDING', 'ADVANCE_PAID', 'CONVERTED'].includes(pi.status)) {
          setError(`Proforma ${pi.piNo} is ${pi.status.replace(/_/g, ' ')} — invoices need a sent or converted proforma`)
        } else {
          setForm(buildFormFromPi(pi, pi.status === 'CONVERTED' ? 'FINAL_BILL' : 'TAX_INVOICE'))
        }
        setSearchParams({}, { replace: true })
      })
      .catch((err) => setError(err.response?.data?.message || 'Failed to load proforma invoice'))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams])

  async function save(e) {
    e.preventDefault()
    setSaving(true)
    try {
      await issueInvoice({
        proformaInvoiceId: form.proformaInvoiceId,
        invoiceType: form.invoiceType || 'TAX_INVOICE',
        dueDate: form.dueDate || null,
        placeOfSupply: form.placeOfSupply || null,
        exchangeRate: form.exchangeRate ? Number(form.exchangeRate) : null,
        portOfLoading: form.portOfLoading || null,
        portOfDischarge: form.portOfDischarge || null,
        exportReference: form.exportReference || null,
        discount: form.discount || 0,
        freightCharges: form.freightCharges || 0,
        loadingCharges: form.loadingCharges || 0,
        documentationCharges: form.documentationCharges || 0,
        insuranceCharges: form.insuranceCharges || 0,
        otherCharges: form.otherCharges || 0,
        additionalCharges: form.additionalCharges || 0,
        taxTreatment: form.taxTreatment,
        paymentTerms: form.paymentTerms,
        bankDetails: form.bankDetails,
        notes: form.notes || null,
        termsConditions: form.termsConditions || null,
        items: form.items.map((it) => ({
          description: it.description,
          hsnCode: it.hsnCode || null,
          quantity: Number(it.quantity),
          unit: it.unit || null,
          ratePerUnit: Number(it.ratePerUnit),
        })),
      })
      setForm(null)
      setPage(0)
      setStatusFilter('')
      load()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to issue tax invoice')
    } finally {
      setSaving(false)
    }
  }

  async function act(fn) {
    setError(null)
    try { await fn(); load() } catch (err) {
      setError(err.response?.data?.message || 'Action failed')
    }
  }

  async function openDetail(id) {
    try {
      const res = await fetchInvoice(id)
      setDetail(res.data)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load invoice')
    }
  }

  const subtotal = form
    ? form.items.reduce((sum, it) => sum + (Number(it.quantity) || 0) * (Number(it.ratePerUnit) || 0), 0)
    : 0

  return (
    <div className="space-y-5">
      <div>
        <h1 className="font-display text-2xl font-bold text-navy-950">Tax Invoices</h1>
        <p className="text-sm text-navy-500">
          Sequential legal documents (INV-YYYY-NNNNNN). Issued from sent or advance-paid proformas; the source PI is marked converted.
        </p>
      </div>

      {error && (
        <div className="flex items-center justify-between rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700">
          <span>{error}</span>
          <button type="button" onClick={() => setError(null)} className="font-bold">×</button>
        </div>
      )}

      <select value={statusFilter} onChange={(e) => { setPage(0); setStatusFilter(e.target.value) }} className={`${inputCls} max-w-xs`}>
        <option value="">All statuses</option>
        {INVOICE_STATUSES.map((s) => <option key={s} value={s}>{s.replace(/_/g, ' ')}</option>)}
      </select>

      {data && (
        <>
          <div className="overflow-hidden rounded-2xl border border-navy-100 bg-white shadow-sm">
            <table className="w-full text-left text-sm">
              <thead className="bg-navy-50 text-xs uppercase tracking-wide text-navy-500">
                <tr>
                  <th className="px-5 py-3">Invoice No</th>
                  <th className="px-5 py-3 hidden md:table-cell">Client</th>
                  <th className="px-5 py-3 hidden lg:table-cell">Against PI</th>
                  <th className="px-5 py-3 hidden lg:table-cell">Due</th>
                  <th className="px-5 py-3 text-right">Total</th>
                  <th className="px-5 py-3 text-right hidden md:table-cell">Balance</th>
                  <th className="px-5 py-3">Status</th>
                  <th className="px-5 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-navy-100">
                {data.content.map((inv) => (
                  <tr key={inv.id}>
                    <td className="px-5 py-3.5 font-semibold text-navy-950">
                      {inv.invoiceNo}
                      {inv.invoiceType === 'FINAL_BILL' && (
                        <span className="ml-2 rounded-full bg-amber-100 px-2 py-0.5 text-[10px] font-bold uppercase tracking-wide text-amber-700">Final</span>
                      )}
                    </td>
                    <td className="hidden px-5 py-3.5 text-navy-600 md:table-cell">{inv.clientCompanyName || '—'}</td>
                    <td className="hidden px-5 py-3.5 text-navy-600 lg:table-cell">{inv.piNo || inv.quoteNo || '—'}</td>
                    <td className="hidden px-5 py-3.5 text-navy-600 lg:table-cell">{formatDate(inv.dueDate)}</td>
                    <td className="px-5 py-3.5 text-right font-mono text-navy-900">{money(inv.grandTotal, inv.currency)}</td>
                    <td className="hidden px-5 py-3.5 text-right font-mono text-navy-700 md:table-cell">{money(inv.balanceAmount, inv.currency)}</td>
                    <td className="px-5 py-3.5">
                      <span className={`rounded-full px-2.5 py-1 text-xs font-bold ${statusBadge(inv.status)}`}>
                        {inv.status.replace(/_/g, ' ')}
                      </span>
                    </td>
                    <td className="space-x-2 whitespace-nowrap px-5 py-3.5 text-right">
                      <button type="button" onClick={() => openDetail(inv.id)} className={secondaryBtnCls}>View</button>
                      <button type="button" onClick={() => viewPdf(managerInvoicePdfUrl(inv.id))} className={`${secondaryBtnCls} mr-2`}>View</button>
                    <button type="button" onClick={() => downloadManagerInvoicePdf(inv.id, inv.invoiceNo)} className={secondaryBtnCls}>PDF</button>
                      {!inv.sentAt && inv.status !== 'CANCELLED' && (
                        <button type="button" onClick={() => act(() => sendInvoice(inv.id))} className={primaryBtnCls}>Send</button>
                      )}
                      {['ISSUED', 'OVERDUE'].includes(inv.status) && (
                        <button type="button" onClick={() => act(() => cancelInvoice(inv.id))} className="rounded-lg border border-red-200 px-3 py-1.5 text-sm font-semibold text-red-700 transition hover:bg-red-50">Cancel</button>
                      )}
                    </td>
                  </tr>
                ))}
                {data.content.length === 0 && (
                  <tr><td colSpan={8}><EmptyState icon="invoice" title="No tax invoices yet" hint="Issue one from a sent proforma invoice to start billing." /></td></tr>
                )}
              </tbody>
            </table>
          </div>
          <Pager page={data.number} totalPages={data.totalPages} onPage={setPage} />
        </>
      )}

      {/* Detail modal with GST split */}
      {detail && (
        <Modal title={`${detail.invoiceNo} — ${detail.clientCompanyName || 'Client'}`} onClose={() => setDetail(null)} wide>
          <dl className="grid grid-cols-1 gap-x-6 gap-y-2 text-sm sm:grid-cols-2">
            <dt className="text-navy-500">Document type</dt><dd>{(detail.invoiceType || 'TAX_INVOICE').replace(/_/g, ' ')}</dd>
            <dt className="text-navy-500">Issue / Due</dt><dd>{formatDate(detail.issueDate)} → {formatDate(detail.dueDate)}</dd>
            <dt className="text-navy-500">GSTIN / PAN</dt><dd>{detail.gstin || '—'} / {detail.pan || '—'}</dd>
            <dt className="text-navy-500">Place of supply</dt><dd>{detail.placeOfSupply || '—'}</dd>
            <dt className="text-navy-500">Taxable amount</dt><dd>{money(detail.taxableAmount, detail.currency)}</dd>
            {Number(detail.cgstAmount) > 0 && (<><dt className="text-navy-500">CGST + SGST</dt><dd>{money(detail.cgstAmount, detail.currency)} + {money(detail.sgstAmount, detail.currency)}</dd></>)}
            {Number(detail.igstAmount) > 0 && (<><dt className="text-navy-500">IGST ({(detail.taxTreatment || '').replace(/_/g, ' ')})</dt><dd>{money(detail.igstAmount, detail.currency)}</dd></>)}
            {Number(detail.cgstAmount) === 0 && Number(detail.igstAmount) === 0 && Number(detail.totalTaxAmount) === 0 && (
              <><dt className="text-navy-500">Tax ({(detail.taxTreatment || 'EXEMPT').replace(/_/g, ' ')})</dt><dd>Zero rated / exempt</dd></>
            )}
            <dt className="text-navy-500 font-bold">Grand total</dt><dd className="font-mono font-bold">{money(detail.grandTotal, detail.currency)}</dd>
            <dt className="text-navy-500">Paid / Balance</dt><dd>{money(detail.paidAmount, detail.currency)} / {money(detail.balanceAmount, detail.currency)}</dd>
          </dl>
          <div className="mt-4 max-h-48 overflow-y-auto rounded-lg border border-navy-100">
            <table className="w-full text-left text-xs">
              <thead className="bg-navy-50 uppercase text-navy-500">
                <tr><th className="px-3 py-2">#</th><th className="px-3 py-2">Description</th><th className="hidden sm:table-cell px-3 py-2">HSN</th><th className="px-3 py-2">Qty</th><th className="px-3 py-2">Rate</th><th className="px-3 py-2 text-right">Amount</th></tr>
              </thead>
              <tbody className="divide-y divide-navy-100">
                {detail.items.map((it, i) => (
                  <tr key={it.id}>
                    <td className="px-3 py-2">{i + 1}</td>
                    <td className="px-3 py-2">{it.description}</td>
                    <td className="hidden sm:table-cell px-3 py-2">{it.hsnCode || '—'}</td>
                    <td className="px-3 py-2">{Number(it.quantity)} {it.unit}</td>
                    <td className="px-3 py-2">{Number(it.ratePerUnit)}</td>
                    <td className="px-3 py-2 text-right font-mono">{money(it.lineAmount, detail.currency)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="mt-5 flex justify-end gap-3">
            {detail.status !== 'CANCELLED' && Number(detail.balanceAmount) > 0 && (
              <button type="button" onClick={() => setOffline({ ...emptyOffline, invoiceId: detail.id })} className={secondaryBtnCls}>Offline Payment</button>
            )}
                    <button type="button" onClick={() => viewPdf(managerInvoicePdfUrl(detail.id))} className={`${secondaryBtnCls} mr-2`}>View</button>
                    <button type="button" onClick={() => downloadManagerInvoicePdf(detail.id, detail.invoiceNo)} className={secondaryBtnCls}>Download PDF</button>
            <button type="button" onClick={() => setDetail(null)} className={primaryBtnCls}>Close</button>
          </div>
        </Modal>
      )}

      {/* Offline payment recorder */}
      {offline && (
        <Modal title={`Record Offline Payment — ${detail?.invoiceNo || ''}`} onClose={() => setOffline(null)}>
          <form onSubmit={(e) => { e.preventDefault(); saveOffline() }} className="space-y-4">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <Field label="Method">
                <select className={inputCls} value={offline.method} onChange={(e) => setOffline({ ...offline, method: e.target.value })}>
                  {['NEFT', 'RTGS', 'IMPS', 'BANK_TRANSFER', 'CHEQUE', 'OTHER'].map((m) => <option key={m} value={m}>{m.replace(/_/g, ' ')}</option>)}
                </select>
              </Field>
              <Field label="Reference / UTR No."><input required className={inputCls} value={offline.transactionReference} onChange={(e) => setOffline({ ...offline, transactionReference: e.target.value })} /></Field>
              <Field label="Amount"><input required type="number" min="0.01" step="0.01" max={Number(detail?.balanceAmount)} className={inputCls} value={offline.amount} onChange={(e) => setOffline({ ...offline, amount: e.target.value })} /></Field>
            </div>
            <Field label="Notes"><textarea rows={2} className={inputCls} value={offline.notes} onChange={(e) => setOffline({ ...offline, notes: e.target.value })} /></Field>
            {actionError && <div className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{actionError}</div>}
            <button type="submit" disabled={saving} className={`${primaryBtnCls} w-full`}>{saving ? 'Recording…' : 'Record Payment'}</button>
          </form>
        </Modal>
      )}

      {/* Issue-from-PI builder */}
      {form && (
        <Modal title={`Issue ${form.invoiceType === 'FINAL_BILL' ? 'Final Bill' : 'Tax Invoice'} — against ${form.piNo}`} onClose={() => setForm(null)} wide>
          <form onSubmit={save} className="space-y-4">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-4">
              <Field label="Document Type">
                <select className={inputCls} value={form.invoiceType} onChange={(e) => setForm({ ...form, invoiceType: e.target.value })}>
                  <option value="TAX_INVOICE">Tax Invoice</option>
                  <option value="FINAL_BILL">Final Bill</option>
                </select>
              </Field>
              <Field label="Due Date"><input type="date" className={inputCls} value={form.dueDate} onChange={(e) => setForm({ ...form, dueDate: e.target.value })} /></Field>
              <Field label="Place of Supply"><input className={inputCls} placeholder="Maharashtra" value={form.placeOfSupply} onChange={(e) => setForm({ ...form, placeOfSupply: e.target.value })} /></Field>
              <Field label="Discount"><input type="number" min="0" step="0.01" className={inputCls} value={form.discount} onChange={(e) => setForm({ ...form, discount: e.target.value })} /></Field>
              <Field label="Additional Charges"><input type="number" min="0" step="0.01" className={inputCls} value={form.additionalCharges} onChange={(e) => setForm({ ...form, additionalCharges: e.target.value })} /></Field>
            </div>
            {form.invoiceType === 'FINAL_BILL' && (
              <p className="rounded-lg bg-amber-50 px-3 py-2 text-xs text-amber-700">
                Final bill: advance payments already collected on this proforma&apos;s earlier invoices are adjusted automatically — the client is billed only the remaining balance.
              </p>
            )}

            <div className="rounded-xl border border-navy-100 p-3">
              <div className="mb-2 flex items-center justify-between">
                <span className="text-xs font-bold uppercase tracking-wide text-navy-500">Line Items (HSN/SAC)</span>
                <button type="button" onClick={() => setForm({ ...form, items: [...form.items, { ...emptyItem }] })} className={secondaryBtnCls}>+ Item</button>
              </div>
              <div className="space-y-2">
                {form.items.map((it, idx) => (
                  <div key={idx} className="grid grid-cols-12 gap-2">
                    <input required placeholder="Description" className={`${inputCls} col-span-4`} value={it.description} onChange={(e) => setForm({ ...form, items: form.items.map((x, i) => i === idx ? { ...x, description: e.target.value } : x) })} />
                    <input placeholder="HSN" className={`${inputCls} col-span-2`} value={it.hsnCode} onChange={(e) => setForm({ ...form, items: form.items.map((x, i) => i === idx ? { ...x, hsnCode: e.target.value } : x) })} />
                    <input required type="number" min="0" step="0.0001" placeholder="Qty" className={`${inputCls} col-span-2`} value={it.quantity} onChange={(e) => setForm({ ...form, items: form.items.map((x, i) => i === idx ? { ...x, quantity: e.target.value } : x) })} />
                    <input placeholder="Unit" className={`${inputCls} col-span-1`} value={it.unit} onChange={(e) => setForm({ ...form, items: form.items.map((x, i) => i === idx ? { ...x, unit: e.target.value } : x) })} />
                    <input required type="number" min="0" step="0.01" placeholder="Rate" className={`${inputCls} col-span-2`} value={it.ratePerUnit} onChange={(e) => setForm({ ...form, items: form.items.map((x, i) => i === idx ? { ...x, ratePerUnit: e.target.value } : x) })} />
                    <button type="button" disabled={form.items.length <= 1} onClick={() => setForm({ ...form, items: form.items.filter((_, i) => i !== idx) })} className="col-span-1 rounded-lg text-navy-400 transition hover:bg-red-50 hover:text-red-600 disabled:opacity-30">×</button>
                  </div>
                ))}
              </div>
              <p className="mt-2 text-right text-sm font-semibold text-navy-700">Subtotal: <span className="font-mono">{money(subtotal, form.currency)}</span></p>
            </div>

            <Field label="Notes"><textarea rows={2} className={inputCls} value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })} /></Field>

            <div className="flex justify-end gap-3 pt-2">
              <button type="button" onClick={() => setForm(null)} className={secondaryBtnCls}>Cancel</button>
              <button type="submit" disabled={saving} className={primaryBtnCls}>{saving ? 'Issuing…' : 'Issue Tax Invoice'}</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
