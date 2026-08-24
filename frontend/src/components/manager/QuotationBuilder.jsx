import { useState } from 'react'
import { createQuotation, updateQuotation } from '../../api/quotations'
import { Field, inputCls, primaryBtnCls } from '../ui/admin'

const emptyItem = () => ({ description: '', quantity: '', unit: 'MT', ratePerUnit: '' })
const TREATMENTS = [
  { value: '', label: 'No tax (exempt export)' },
  { value: 'IGST', label: 'IGST (auto-resolve by country)' },
  { value: 'CGST_SGST', label: 'CGST + SGST (auto-resolve)' },
  { value: 'ZERO_RATED', label: 'Zero-rated export' },
]

export default function QuotationBuilder({ enquiryId, currency = 'USD', quotation, onClose, onSaved }) {
  const editing = Boolean(quotation?.id)
  const [form, setForm] = useState(() => ({
    items: quotation?.items?.length
      ? quotation.items.map((i) => ({
          description: i.description,
          quantity: String(i.quantity),
          unit: i.unit || '',
          ratePerUnit: String(i.ratePerUnit),
        }))
      : [emptyItem()],
    validUntil: quotation?.validUntil ?? '',
    incoterms: quotation?.incoterms ?? '',
    paymentTerms: quotation?.paymentTerms ?? '',
    deliveryTerms: quotation?.deliveryTerms ?? '',
    notes: quotation?.notes ?? '',
    termsConditions: quotation?.termsConditions ?? '',
    discount: quotation?.discount ? String(quotation.discount) : '',
    freightCharges: quotation?.freightCharges ? String(quotation.freightCharges) : '',
    loadingCharges: quotation?.loadingCharges ? String(quotation.loadingCharges) : '',
    documentationCharges: quotation?.documentationCharges ? String(quotation.documentationCharges) : '',
    insuranceCharges: quotation?.insuranceCharges ? String(quotation.insuranceCharges) : '',
    otherCharges: quotation?.otherCharges ? String(quotation.otherCharges) : '',
    taxTreatment: quotation?.taxTreatment ?? '',
    taxRateId: '',
  }))
  const [error, setError] = useState(null)
  const [saving, setSaving] = useState(false)

  const num = (v) => (v === '' || v == null ? null : Number(v))
  const subtotal = form.items.reduce(
    (sum, item) => sum + (Number(item.quantity) || 0) * (Number(item.ratePerUnit) || 0),
    0,
  )

  function setItem(index, key, value) {
    setForm((prev) => ({
      ...prev,
      items: prev.items.map((item, i) => (i === index ? { ...item, [key]: value } : item)),
    }))
  }

  async function submit(e) {
    e.preventDefault()
    setSaving(true)
    setError(null)
    const payload = {
      enquiryId: Number(enquiryId),
      currency,
      validUntil: form.validUntil || null,
      incoterms: form.incoterms || null,
      paymentTerms: form.paymentTerms || null,
      deliveryTerms: form.deliveryTerms || null,
      notes: form.notes || null,
      termsConditions: form.termsConditions || null,
      discount: num(form.discount),
      freightCharges: num(form.freightCharges),
      loadingCharges: num(form.loadingCharges),
      documentationCharges: num(form.documentationCharges),
      insuranceCharges: num(form.insuranceCharges),
      otherCharges: num(form.otherCharges),
      taxTreatment: form.taxTreatment || null,
      taxRateId: form.taxRateId ? Number(form.taxRateId) : null,
      items: form.items.map((item) => ({
        description: item.description,
        quantity: Number(item.quantity),
        unit: item.unit || null,
        ratePerUnit: Number(item.ratePerUnit),
      })),
    }
    try {
      const res = editing ? await updateQuotation(quotation.id, payload) : await createQuotation(payload)
      onSaved(res.data)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save quotation')
    } finally {
      setSaving(false)
    }
  }

  return (
    <form onSubmit={submit} className="space-y-5">
      <div className="rounded-xl border border-navy-100">
        <table className="w-full text-sm">
          <thead className="bg-navy-50 text-xs uppercase tracking-wide text-navy-500">
            <tr>
              <th className="px-2 py-2 text-left">Description</th>
              <th className="w-24 px-2 py-2">Qty</th>
              <th className="w-20 px-2 py-2">Unit</th>
              <th className="w-28 px-2 py-2">Rate</th>
              <th className="w-24 px-2 py-2" />
            </tr>
          </thead>
          <tbody className="divide-y divide-navy-100">
            {form.items.map((item, index) => (
              <tr key={index}>
                <td className="px-1 py-1.5">
                  <input required maxLength={500} className={inputCls} value={item.description}
                    onChange={(e) => setItem(index, 'description', e.target.value)} placeholder="Cargo / service line" />
                </td>
                <td className="px-1 py-1.5">
                  <input required type="number" step="0.0001" min="0" className={inputCls} value={item.quantity}
                    onChange={(e) => setItem(index, 'quantity', e.target.value)} />
                </td>
                <td className="px-1 py-1.5">
                  <input className={inputCls} value={item.unit}
                    onChange={(e) => setItem(index, 'unit', e.target.value)} />
                </td>
                <td className="px-1 py-1.5">
                  <input required type="number" step="0.01" min="0" className={inputCls} value={item.ratePerUnit}
                    onChange={(e) => setItem(index, 'ratePerUnit', e.target.value)} />
                </td>
                <td className="px-1 py-1.5 text-center">
                  {form.items.length > 1 && (
                    <button type="button" onClick={() => setForm((p) => ({ ...p, items: p.items.filter((_, i) => i !== index) }))}
                      className="text-xs font-bold text-red-600 hover:underline">Remove</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        <div className="flex items-center justify-between border-t border-navy-100 px-3 py-2">
          <button type="button" onClick={() => setForm((p) => ({ ...p, items: [...p.items, emptyItem()] }))}
            className="text-xs font-bold text-navy-600 hover:underline">+ Add line item</button>
          <span className="text-sm font-semibold text-navy-700">
            Subtotal: {currency} {subtotal.toLocaleString('en-US', { minimumFractionDigits: 2 })}
          </span>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
        <Field label="Valid Until">
          <input type="date" className={inputCls} value={form.validUntil}
            onChange={(e) => setForm({ ...form, validUntil: e.target.value })} />
        </Field>
        <Field label="Incoterms">
          <input className={inputCls} value={form.incoterms} maxLength={20}
            onChange={(e) => setForm({ ...form, incoterms: e.target.value })} placeholder="CIF / FOB / EXW" />
        </Field>
        <Field label={`Discount (${currency})`}>
          <input type="number" step="0.01" min="0" className={inputCls} value={form.discount}
            onChange={(e) => setForm({ ...form, discount: e.target.value })} />
        </Field>
        <Field label={`Freight (${currency})`}>
          <input type="number" step="0.01" min="0" className={inputCls} value={form.freightCharges}
            onChange={(e) => setForm({ ...form, freightCharges: e.target.value })} />
        </Field>
        <Field label={`Loading (${currency})`}>
          <input type="number" step="0.01" min="0" className={inputCls} value={form.loadingCharges}
            onChange={(e) => setForm({ ...form, loadingCharges: e.target.value })} />
        </Field>
        <Field label={`Documentation (${currency})`}>
          <input type="number" step="0.01" min="0" className={inputCls} value={form.documentationCharges}
            onChange={(e) => setForm({ ...form, documentationCharges: e.target.value })} />
        </Field>
        <Field label={`Insurance (${currency})`}>
          <input type="number" step="0.01" min="0" className={inputCls} value={form.insuranceCharges}
            onChange={(e) => setForm({ ...form, insuranceCharges: e.target.value })} />
        </Field>
        <Field label={`Other charges (${currency})`}>
          <input type="number" step="0.01" min="0" className={inputCls} value={form.otherCharges}
            onChange={(e) => setForm({ ...form, otherCharges: e.target.value })} />
        </Field>
        <Field label="Tax Treatment">
          <select className={inputCls} value={form.taxTreatment}
            onChange={(e) => setForm({ ...form, taxTreatment: e.target.value })}>
            {TREATMENTS.map((t) => <option key={t.value} value={t.value}>{t.label}</option>)}
          </select>
        </Field>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Field label="Payment Terms">
          <textarea rows={2} className={inputCls} value={form.paymentTerms}
            onChange={(e) => setForm({ ...form, paymentTerms: e.target.value })} placeholder="30% advance, balance against documents…" />
        </Field>
        <Field label="Delivery Terms">
          <textarea rows={2} className={inputCls} value={form.deliveryTerms}
            onChange={(e) => setForm({ ...form, deliveryTerms: e.target.value })} placeholder="Shipment within 3 weeks of PO…" />
        </Field>
        <Field label="Notes">
          <textarea rows={2} className={inputCls} value={form.notes}
            onChange={(e) => setForm({ ...form, notes: e.target.value })} />
        </Field>
        <Field label="Terms & Conditions">
          <textarea rows={2} className={inputCls} value={form.termsConditions}
            onChange={(e) => setForm({ ...form, termsConditions: e.target.value })} />
        </Field>
      </div>

      {error && <p className="rounded-lg bg-red-50 px-4 py-2.5 text-sm text-red-700">{error}</p>}

      <div className="flex justify-end gap-2">
        <button type="button" onClick={onClose} className="rounded-lg border border-navy-200 px-4 py-2 text-sm font-semibold text-navy-700 hover:bg-navy-50">
          Cancel
        </button>
        <button type="submit" disabled={saving} className={primaryBtnCls}>
          {saving ? 'Saving…' : editing ? 'Save Changes' : 'Create Draft'}
        </button>
      </div>
    </form>
  )
}
