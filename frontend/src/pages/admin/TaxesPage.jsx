import { useCallback, useEffect, useState } from 'react'
import {
  TAX_TYPES,
  createTaxRate,
  emptyTaxRateForm,
  fetchTaxRates,
  toggleTaxRate,
  deleteTaxRate,
  updateTaxRate,
} from '../../api/taxRates'
import { Field, Modal, inputCls, primaryBtnCls, secondaryBtnCls, dangerBtnCls } from '../../components/ui/admin'

export default function TaxesPage() {
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)
  const [form, setForm] = useState(null)
  const [saving, setSaving] = useState(false)
  const [filterType, setFilterType] = useState('')

  const load = useCallback(() => {
    fetchTaxRates(filterType ? { taxType: filterType, size: 100 } : { size: 100 })
      .then((res) => setData(res.data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load tax rates'))
  }, [filterType])

  useEffect(() => { load() }, [load])

  async function save(e) {
    e.preventDefault()
    setSaving(true)
    try {
      const payload = {
        ...form,
        rate: Number(form.rate),
        jurisdiction: form.jurisdiction || null,
        description: form.description || null,
      }
      if (form.id) await updateTaxRate(form.id, payload)
      else await createTaxRate(payload)
      setForm(null)
      load()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save tax rate')
    } finally {
      setSaving(false)
    }
  }

  async function toggle(row) {
    try {
      await toggleTaxRate(row.id, !row.active)
      load()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update tax rate')
    }
  }

  async function remove(row) {
    if (!window.confirm(`Delete tax rate "${row.name}"? Documents already issued keep their snapshot.`)) return
    try {
      await deleteTaxRate(row.id)
      load()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete tax rate')
    }
  }

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="font-display text-2xl font-bold text-navy-950">Taxes</h1>
          <p className="text-sm text-navy-500">Configurable rates used by quotations and invoices. Issued documents keep their own snapshot.</p>
        </div>
        <button type="button" onClick={() => setForm({ ...emptyTaxRateForm })} className={primaryBtnCls}>
          + Add Tax Rate
        </button>
      </div>

      {error && (
        <div className="flex items-center justify-between rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700">
          <span>{error}</span>
          <button type="button" onClick={() => setError(null)} className="font-bold">×</button>
        </div>
      )}

      <select
        value={filterType}
        onChange={(e) => setFilterType(e.target.value)}
        className={`${inputCls} max-w-xs`}
      >
        <option value="">All tax types</option>
        {TAX_TYPES.map((t) => (
          <option key={t} value={t}>{t.replace('_', ' ')}</option>
        ))}
      </select>

      {data && (
        <div className="overflow-hidden rounded-2xl border border-navy-100 bg-white shadow-sm">
          <table className="w-full text-left text-sm">
            <thead className="bg-navy-50 text-xs uppercase tracking-wide text-navy-500">
              <tr>
                <th className="px-5 py-3">Name</th>
                <th className="px-5 py-3">Type</th>
                <th className="px-5 py-3">Rate %</th>
                <th className="px-5 py-3 hidden md:table-cell">Country</th>
                <th className="px-5 py-3 hidden lg:table-cell">Effective From</th>
                <th className="px-5 py-3">Status</th>
                <th className="px-5 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-navy-100">
              {data.content.map((rate) => (
                <tr key={rate.id}>
                  <td className="px-5 py-3.5 font-semibold text-navy-950">{rate.name}</td>
                  <td className="px-5 py-3.5 text-navy-600">{rate.taxType}</td>
                  <td className="px-5 py-3.5 font-mono text-navy-800">{Number(rate.rate)}%</td>
                  <td className="hidden px-5 py-3.5 text-navy-600 md:table-cell">{rate.country}</td>
                  <td className="hidden px-5 py-3.5 text-navy-600 lg:table-cell">{rate.effectiveFrom}</td>
                  <td className="px-5 py-3.5">
                    <span className={`rounded-full px-2.5 py-1 text-xs font-bold ${
                      rate.active ? 'bg-emerald-100 text-emerald-800' : 'bg-red-100 text-red-700'
                    }`}>
                      {rate.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="space-x-2 px-5 py-3.5 text-right whitespace-nowrap">
                    <button
                      type="button"
                      onClick={() => setForm({
                        ...emptyTaxRateForm,
                        ...rate,
                        rate: String(Number(rate.rate)),
                        effectiveFrom: rate.effectiveFrom,
                      })}
                      className={secondaryBtnCls}
                    >
                      Edit
                    </button>
                    <button type="button" onClick={() => toggle(rate)} className={secondaryBtnCls}>
                      {rate.active ? 'Disable' : 'Enable'}
                    </button>
                    <button type="button" onClick={() => remove(rate)} className={dangerBtnCls}>Delete</button>
                  </td>
                </tr>
              ))}
              {data.content.length === 0 && (
                <tr><td colSpan={7} className="px-5 py-8 text-center text-navy-400">No tax rates configured yet.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {form && (
        <Modal title={form.id ? `Edit Tax Rate — ${form.name}` : 'Add Tax Rate'} onClose={() => setForm(null)}>
          <form onSubmit={save} className="space-y-4">
            <Field label="Name *"><input required className={inputCls} placeholder="IGST Export 18%" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} /></Field>
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <Field label="Tax Type *">
                <select required className={inputCls} value={form.taxType} onChange={(e) => setForm({ ...form, taxType: e.target.value })}>
                  {TAX_TYPES.map((t) => <option key={t} value={t}>{t.replace('_', ' ')}</option>)}
                </select>
              </Field>
              <Field label="Rate % *"><input required type="number" min="0" max="100" step="0.01" className={inputCls} value={form.rate} onChange={(e) => setForm({ ...form, rate: e.target.value })} /></Field>
            </div>
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <Field label="Country *"><input required className={inputCls} value={form.country} onChange={(e) => setForm({ ...form, country: e.target.value })} /></Field>
              <Field label="Jurisdiction"><input className={inputCls} placeholder="Maharashtra (optional)" value={form.jurisdiction || ''} onChange={(e) => setForm({ ...form, jurisdiction: e.target.value })} /></Field>
            </div>
            <Field label="Effective From *">
              <input required type="date" className={inputCls} value={form.effectiveFrom || ''} onChange={(e) => setForm({ ...form, effectiveFrom: e.target.value })} />
            </Field>
            <Field label="Status">
              <select className={inputCls} value={form.active ? '1' : '0'} onChange={(e) => setForm({ ...form, active: e.target.value === '1' })}>
                <option value="1">Active</option>
                <option value="0">Inactive</option>
              </select>
            </Field>
            <Field label="Description"><textarea rows={2} className={inputCls} value={form.description || ''} onChange={(e) => setForm({ ...form, description: e.target.value })} /></Field>
            <div className="flex justify-end gap-3 pt-2">
              <button type="button" onClick={() => setForm(null)} className={secondaryBtnCls}>Cancel</button>
              <button type="submit" disabled={saving} className={primaryBtnCls}>{saving ? 'Saving…' : 'Save Tax Rate'}</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
