import { useCallback, useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { Field, Modal, Pager, inputCls, primaryBtnCls, secondaryBtnCls } from '../../components/ui/admin'
import { fetchQuotation, viewPdf } from '../../api/quotations'
import {
  PROFORMA_STATUSES,
  cancelProforma,
  createProforma,
  downloadManagerProformaPdf,
  fetchProforma,
  fetchProformas,
  formatDate,
  money,
  managerProformaPdfUrl,
  sendProforma,
} from '../../api/proforma'

const statusBadge = (status) => {
  const map = {
    DRAFT: 'bg-navy-100 text-navy-700',
    SENT: 'bg-blue-100 text-blue-800',
    PAYMENT_PENDING: 'bg-amber-100 text-amber-800',
    ADVANCE_PAID: 'bg-emerald-100 text-emerald-800',
    CONVERTED: 'bg-emerald-600/20 text-emerald-900',
    CANCELLED: 'bg-red-100 text-red-700',
    EXPIRED: 'bg-navy-100 text-navy-500',
  }
  return map[status] || 'bg-navy-100 text-navy-700'
}

const emptyItem = { description: '', quantity: '', unit: 'MT', ratePerUnit: '' }

function buildFormFromQuotation(q) {
  return {
    quotationId: q.id,
    quoteNo: q.quoteNo,
    clientName: q.clientCompanyName || '',
    currency: q.currency || 'USD',
    validUntil: q.validUntil || '',
    paymentTerms: q.paymentTerms || '',
    bankDetails:
      'Global Export Pvt. Ltd.\nHDFC Bank, Fort Branch, Mumbai\nA/C 50200012345678 • IFSC HDFC0000123 • SWIFT HDFCINBB\nPurpose code: P0103 (Export of goods)',
    notes: q.notes || '',
    discount: Number(q.discount ?? 0),
    freightCharges: Number(q.freightCharges ?? 0),
    loadingCharges: Number(q.loadingCharges ?? 0),
    documentationCharges: Number(q.documentationCharges ?? 0),
    insuranceCharges: Number(q.insuranceCharges ?? 0),
    otherCharges: Number(q.otherCharges ?? 0),
    taxTreatment: q.taxTreatment || 'EXEMPT',
    items: (q.items || []).map((it) => ({
      description: it.description,
      quantity: String(Number(it.quantity)),
      unit: it.unit || '',
      ratePerUnit: String(Number(it.ratePerUnit)),
    })),
  }
}

export default function ProformaPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const navigate = useNavigate()
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)
  const [page, setPage] = useState(0)
  const [statusFilter, setStatusFilter] = useState('')
  const [form, setForm] = useState(null)
  const [saving, setSaving] = useState(false)
  const [detail, setDetail] = useState(null)

  const load = useCallback(() => {
    const params = { page, size: 10 }
    if (statusFilter) params.status = statusFilter
    fetchProformas(params)
      .then((res) => setData(res.data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load proforma invoices'))
  }, [page, statusFilter])

  useEffect(() => { load() }, [load])

  // Deep link: /manager/proforma?create=<quotationId> opens the builder prefilled
  useEffect(() => {
    const quoteId = searchParams.get('create')
    if (!quoteId || form) return
    fetchQuotation(quoteId)
      .then((res) => {
        const q = res.data
        if (q.status !== 'ACCEPTED') {
          setError(`Quotation ${q.quoteNo} is ${q.status} — proforma invoices require an accepted quotation`)
        } else {
          setForm(buildFormFromQuotation(q))
        }
        setSearchParams({}, { replace: true })
      })
      .catch((err) => setError(err.response?.data?.message || 'Failed to load quotation'))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams])

  async function save(e) {
    e.preventDefault()
    setSaving(true)
    try {
      await createProforma({
        quotationId: form.quotationId,
        validUntil: form.validUntil || null,
        paymentTerms: form.paymentTerms,
        bankDetails: form.bankDetails,
        notes: form.notes,
        discount: form.discount || 0,
        freightCharges: form.freightCharges || 0,
        loadingCharges: form.loadingCharges || 0,
        documentationCharges: form.documentationCharges || 0,
        insuranceCharges: form.insuranceCharges || 0,
        otherCharges: form.otherCharges || 0,
        taxTreatment: form.taxTreatment,
        items: form.items.map((it) => ({
          description: it.description,
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
      setError(err.response?.data?.message || 'Failed to create proforma invoice')
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
      const res = await fetchProforma(id)
      setDetail(res.data)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load proforma invoice')
    }
  }

  const subtotal = form
    ? form.items.reduce((sum, it) => sum + (Number(it.quantity) || 0) * (Number(it.ratePerUnit) || 0), 0)
    : 0

  return (
    <div className="space-y-5">
      <div>
        <h1 className="font-display text-2xl font-bold text-navy-950">Proforma Invoices</h1>
        <p className="text-sm text-navy-500">Advance-payment documents generated from accepted quotations.</p>
      </div>

      {error && (
        <div className="flex items-center justify-between rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700">
          <span>{error}</span>
          <button type="button" onClick={() => setError(null)} className="font-bold">×</button>
        </div>
      )}

      <select value={statusFilter} onChange={(e) => { setPage(0); setStatusFilter(e.target.value) }} className={`${inputCls} max-w-xs`}>
        <option value="">All statuses</option>
        {PROFORMA_STATUSES.map((s) => <option key={s} value={s}>{s.replace(/_/g, ' ')}</option>)}
      </select>

      {data && (
        <>
          <div className="overflow-hidden rounded-2xl border border-navy-100 bg-white shadow-sm">
            <table className="w-full text-left text-sm">
              <thead className="bg-navy-50 text-xs uppercase tracking-wide text-navy-500">
                <tr>
                  <th className="px-5 py-3">PI No</th>
                  <th className="px-5 py-3 hidden md:table-cell">Client</th>
                  <th className="px-5 py-3 hidden lg:table-cell">Against Quote</th>
                  <th className="px-5 py-3">Issued</th>
                  <th className="px-5 py-3 text-right">Total</th>
                  <th className="px-5 py-3">Status</th>
                  <th className="px-5 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-navy-100">
                {data.content.map((pi) => (
                  <tr key={pi.id}>
                    <td className="px-5 py-3.5 font-semibold text-navy-950">{pi.piNo}</td>
                    <td className="hidden px-5 py-3.5 text-navy-600 md:table-cell">{pi.clientCompanyName || '—'}</td>
                    <td className="hidden px-5 py-3.5 text-navy-600 lg:table-cell">{pi.quoteNo || '—'}</td>
                    <td className="px-5 py-3.5 text-navy-600">{formatDate(pi.issueDate)}</td>
                    <td className="px-5 py-3.5 text-right font-mono text-navy-900">{money(pi.grandTotal, pi.currency)}</td>
                    <td className="px-5 py-3.5">
                      <span className={`rounded-full px-2.5 py-1 text-xs font-bold ${statusBadge(pi.status)}`}>{pi.status.replace(/_/g, ' ')}</span>
                    </td>
                    <td className="space-x-2 whitespace-nowrap px-5 py-3.5 text-right">
                      <button type="button" onClick={() => openDetail(pi.id)} className={secondaryBtnCls}>View</button>
                      <button type="button" onClick={() => viewPdf(managerProformaPdfUrl(pi.id))} className={`${secondaryBtnCls} mr-2`}>View</button>
                    <button type="button" onClick={() => downloadManagerProformaPdf(pi.id, pi.piNo)} className={secondaryBtnCls}>PDF</button>
                      {pi.status === 'DRAFT' && (
                        <button type="button" onClick={() => act(() => sendProforma(pi.id))} className={primaryBtnCls}>Send</button>
                      )}
                      {['SENT', 'PAYMENT_PENDING', 'ADVANCE_PAID'].includes(pi.status) && (
                        <button
                          type="button"
                          onClick={() => navigate(`/manager/invoices?create=${pi.id}`)}
                          className={primaryBtnCls}
                        >
                          Generate INV
                        </button>
                      )}
                      {pi.status === 'CONVERTED' && (
                        <button
                          type="button"
                          onClick={() => navigate(`/manager/invoices?create=${pi.id}`)}
                          className={secondaryBtnCls}
                        >
                          Final Bill
                        </button>
                      )}
                      {(pi.status === 'DRAFT' || pi.status === 'SENT') && pi.status !== 'CONVERTED' && (
                        <button type="button" onClick={() => act(() => cancelProforma(pi.id))} className="rounded-lg border border-red-200 px-3 py-1.5 text-sm font-semibold text-red-700 transition hover:bg-red-50">Cancel</button>
                      )}
                    </td>
                  </tr>
                ))}
                {data.content.length === 0 && (
                  <tr><td colSpan={7} className="px-5 py-8 text-center text-navy-400">No proforma invoices yet. Generate one from an accepted quotation.</td></tr>
                )}
              </tbody>
            </table>
          </div>
          <Pager page={data.number} totalPages={data.totalPages} onPage={setPage} />
        </>
      )}

      {/* Detail modal */}
      {detail && (
        <Modal title={`${detail.piNo} — ${detail.clientCompanyName || 'Client'}`} onClose={() => setDetail(null)} wide>
          <dl className="grid grid-cols-1 gap-x-6 gap-y-2 text-sm sm:grid-cols-2">
            <dt className="text-navy-500">Against quotation</dt><dd className="font-semibold text-navy-900">{detail.quoteNo || '—'}</dd>
            <dt className="text-navy-500">Issue date</dt><dd>{formatDate(detail.issueDate)}</dd>
            <dt className="text-navy-500">Valid until</dt><dd>{formatDate(detail.validUntil)}</dd>
            <dt className="text-navy-500">Taxable amount</dt><dd>{money(detail.taxableAmount, detail.currency)}</dd>
            <dt className="text-navy-500">Tax ({(detail.taxTreatment || '').replace(/_/g, ' ')})</dt><dd>{money(detail.taxAmount, detail.currency)}</dd>
            <dt className="text-navy-500 font-bold">Grand total</dt><dd className="font-mono font-bold">{money(detail.grandTotal, detail.currency)}</dd>
          </dl>
          <div className="mt-4 max-h-48 overflow-y-auto rounded-lg border border-navy-100">
            <table className="w-full text-left text-xs">
              <thead className="bg-navy-50 uppercase text-navy-500">
                <tr><th className="px-3 py-2">#</th><th className="px-3 py-2">Description</th><th className="px-3 py-2">Qty</th><th className="px-3 py-2">Rate</th><th className="px-3 py-2 text-right">Amount</th></tr>
              </thead>
              <tbody className="divide-y divide-navy-100">
                {detail.items.map((it, i) => (
                  <tr key={it.id}>
                    <td className="px-3 py-2">{i + 1}</td>
                    <td className="px-3 py-2">{it.description}</td>
                    <td className="px-3 py-2">{Number(it.quantity)} {it.unit}</td>
                    <td className="px-3 py-2">{Number(it.ratePerUnit)}</td>
                    <td className="px-3 py-2 text-right font-mono">{money(it.lineAmount, detail.currency)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="mt-5 flex justify-end gap-3">
                    <button type="button" onClick={() => viewPdf(managerProformaPdfUrl(detail.id))} className={`${secondaryBtnCls} mr-2`}>View</button>
                    <button type="button" onClick={() => downloadManagerProformaPdf(detail.id, detail.piNo)} className={secondaryBtnCls}>Download PDF</button>
            <button type="button" onClick={() => setDetail(null)} className={primaryBtnCls}>Close</button>
          </div>
        </Modal>
      )}

      {/* Create-from-quotation builder */}
      {form && (
        <Modal title={`New Proforma Invoice — against ${form.quoteNo}`} onClose={() => setForm(null)} wide>
          <form onSubmit={save} className="space-y-4">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
              <Field label="Valid Until"><input type="date" className={inputCls} value={form.validUntil || ''} onChange={(e) => setForm({ ...form, validUntil: e.target.value })} /></Field>
              <Field label="Discount"><input type="number" min="0" step="0.01" className={inputCls} value={form.discount} onChange={(e) => setForm({ ...form, discount: e.target.value })} /></Field>
              <Field label="Tax Treatment">
                <select className={inputCls} value={form.taxTreatment} onChange={(e) => setForm({ ...form, taxTreatment: e.target.value })}>
                  {['EXEMPT', 'ZERO_RATED', 'IGST', 'CGST_SGST', 'CUSTOM'].map((t) => <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>)}
                </select>
              </Field>
            </div>

            <div className="rounded-xl border border-navy-100 p-3">
              <div className="mb-2 flex items-center justify-between">
                <span className="text-xs font-bold uppercase tracking-wide text-navy-500">Line Items</span>
                <button type="button" onClick={() => setForm({ ...form, items: [...form.items, { ...emptyItem }] })} className={secondaryBtnCls}>+ Item</button>
              </div>
              <div className="space-y-2">
                {form.items.map((it, idx) => (
                  <div key={idx} className="grid grid-cols-12 gap-2">
                    <input required placeholder="Description" className={`${inputCls} col-span-5`} value={it.description} onChange={(e) => setForm({ ...form, items: form.items.map((x, i) => i === idx ? { ...x, description: e.target.value } : x) })} />
                    <input required type="number" min="0" step="0.0001" placeholder="Qty" className={`${inputCls} col-span-2`} value={it.quantity} onChange={(e) => setForm({ ...form, items: form.items.map((x, i) => i === idx ? { ...x, quantity: e.target.value } : x) })} />
                    <input placeholder="Unit" className={`${inputCls} col-span-2`} value={it.unit} onChange={(e) => setForm({ ...form, items: form.items.map((x, i) => i === idx ? { ...x, unit: e.target.value } : x) })} />
                    <input required type="number" min="0" step="0.01" placeholder="Rate" className={`${inputCls} col-span-2`} value={it.ratePerUnit} onChange={(e) => setForm({ ...form, items: form.items.map((x, i) => i === idx ? { ...x, ratePerUnit: e.target.value } : x) })} />
                    <button type="button" disabled={form.items.length <= 1} onClick={() => setForm({ ...form, items: form.items.filter((_, i) => i !== idx) })} className="col-span-1 rounded-lg text-navy-400 transition hover:bg-red-50 hover:text-red-600 disabled:opacity-30">×</button>
                  </div>
                ))}
              </div>
              <p className="mt-2 text-right text-sm font-semibold text-navy-700">Subtotal: <span className="font-mono">{money(subtotal, form.currency)}</span></p>
            </div>

            <Field label="Payment Terms"><textarea rows={2} className={inputCls} value={form.paymentTerms || ''} onChange={(e) => setForm({ ...form, paymentTerms: e.target.value })} /></Field>
            <Field label="Bank Details"><textarea rows={3} className={inputCls} value={form.bankDetails || ''} onChange={(e) => setForm({ ...form, bankDetails: e.target.value })} /></Field>

            <div className="flex justify-end gap-3 pt-2">
              <button type="button" onClick={() => setForm(null)} className={secondaryBtnCls}>Cancel</button>
              <button type="submit" disabled={saving} className={primaryBtnCls}>{saving ? 'Creating…' : 'Create Proforma Invoice'}</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
