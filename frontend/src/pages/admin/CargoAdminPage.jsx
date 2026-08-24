import { useCallback, useEffect, useState } from 'react'
import { createCargoLot, fetchAdminCargo, updateCargoLot } from '../../api/admin'
import { fetchAllCategories, fetchAllPorts } from '../../api/admin'
import { Field, Modal, Pager, inputCls, primaryBtnCls, secondaryBtnCls } from '../../components/ui/admin'

const statuses = ['AVAILABLE', 'RESERVED', 'LOADING', 'IN_TRANSIT', 'DELIVERED']

const emptyForm = {
  name: '', categoryId: '', description: '', quantity: '', unit: 'MT',
  originCountry: '', destinationCountry: '', loadingPortId: '', destinationPortId: '',
  loadingDate: '', estimatedArrival: '', indicativePrice: '', currency: 'INR', status: 'AVAILABLE',
}

export default function CargoAdminPage() {
  const [page, setPage] = useState(0)
  const [statusFilter, setStatusFilter] = useState('')
  const [data, setData] = useState(null)
  const [categories, setCategories] = useState([])
  const [ports, setPorts] = useState([])
  const [error, setError] = useState(null)
  const [form, setForm] = useState(null)
  const [saving, setSaving] = useState(false)

  const load = useCallback(() => {
    fetchAdminCargo({ page, size: 10, status: statusFilter || undefined })
      .then((res) => setData(res.data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load cargo lots'))
  }, [page, statusFilter])

  useEffect(() => {
    setError(null)
    load()
  }, [load])

  useEffect(() => {
    fetchAllCategories().then((res) => setCategories(res.data)).catch(() => {})
    fetchAllPorts().then((res) => setPorts(res.data)).catch(() => {})
  }, [])

  async function save(e) {
    e.preventDefault()
    setSaving(true)
    try {
      const payload = {
        ...form,
        categoryId: form.categoryId ? Number(form.categoryId) : null,
        loadingPortId: form.loadingPortId ? Number(form.loadingPortId) : null,
        destinationPortId: form.destinationPortId ? Number(form.destinationPortId) : null,
        quantity: Number(form.quantity),
        indicativePrice: form.indicativePrice ? Number(form.indicativePrice) : null,
        loadingDate: form.loadingDate || null,
        estimatedArrival: form.estimatedArrival || null,
      }
      if (form.id) await updateCargoLot(form.id, payload)
      else await createCargoLot(payload)
      setForm(null)
      load()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save cargo lot')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="font-display text-2xl font-bold text-navy-950">Cargo Lots</h1>
        <button type="button" onClick={() => setForm({ ...emptyForm })} className={primaryBtnCls}>
          + Add Cargo Lot
        </button>
      </div>

      <select
        value={statusFilter}
        onChange={(e) => { setPage(0); setStatusFilter(e.target.value) }}
        className={`${inputCls} max-w-48`}
      >
        <option value="">All statuses</option>
        {statuses.map((s) => <option key={s}>{s}</option>)}
      </select>

      {error && <div className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>}

      {data && (
        <>
          <div className="overflow-x-auto rounded-2xl border border-navy-100 bg-white shadow-sm">
            <table className="w-full min-w-max text-left text-sm">
              <thead className="bg-navy-50 text-xs uppercase tracking-wide text-navy-500">
                <tr>
                  <th className="px-5 py-3">Lot</th>
                  <th className="px-5 py-3">Quantity</th>
                  <th className="px-5 py-3 hidden md:table-cell">Route</th>
                  <th className="px-5 py-3 hidden lg:table-cell">Price</th>
                  <th className="px-5 py-3">Status</th>
                  <th className="px-5 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-navy-100">
                {data.content.map((lot) => (
                  <tr key={lot.id}>
                    <td className="px-5 py-3.5">
                      <div className="font-semibold text-navy-950">{lot.name}</div>
                      <div className="text-xs text-navy-400">{lot.categoryName || 'Uncategorised'}</div>
                    </td>
                    <td className="px-5 py-3.5">{lot.quantity} {lot.unit}</td>
                    <td className="hidden px-5 py-3.5 md:table-cell">{lot.originCountry} → {lot.destinationCountry}</td>
                    <td className="hidden px-5 py-3.5 lg:table-cell">
                      {lot.indicativePrice ? `${lot.currency} ${lot.indicativePrice}` : '—'}
                    </td>
                    <td className="px-5 py-3.5">
                      <span className={`rounded-full px-2.5 py-1 text-xs font-bold ${
                        lot.status === 'AVAILABLE' ? 'bg-emerald-100 text-emerald-800' : 'bg-navy-100 text-navy-700'
                      }`}>
                        {lot.status.replace(/_/g, ' ')}
                      </span>
                    </td>
                    <td className="px-5 py-3.5 text-right">
                      <button
                        type="button"
                        onClick={() => setForm({
                          ...emptyForm, ...lot,
                          categoryId: lot.categoryId ?? '',
                          loadingPortId: lot.loadingPortId ?? '',
                          destinationPortId: lot.destinationPortId ?? '',
                          indicativePrice: lot.indicativePrice ?? '',
                        })}
                        className={secondaryBtnCls}
                      >
                        Edit
                      </button>
                    </td>
                  </tr>
                ))}
                {data.content.length === 0 && (
                  <tr><td colSpan={6} className="px-5 py-10 text-center text-navy-400">No cargo lots yet.</td></tr>
                )}
              </tbody>
            </table>
          </div>
          <Pager page={page} totalPages={data.totalPages} onPage={setPage} />
        </>
      )}

      {form && (
        <Modal title={form.id ? `Edit Lot — ${form.name}` : 'Add Cargo Lot'} onClose={() => setForm(null)} wide>
          <form onSubmit={save} className="space-y-4">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <Field label="Name *"><input required className={inputCls} value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} /></Field>
              <Field label="Category">
                <select className={inputCls} value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })}>
                  <option value="">None</option>
                  {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
              </Field>
              <Field label="Status">
                <select className={inputCls} value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
                  {statuses.map((s) => <option key={s}>{s}</option>)}
                </select>
              </Field>
              <Field label="Quantity *"><input required type="number" step="any" min="0" className={inputCls} value={form.quantity} onChange={(e) => setForm({ ...form, quantity: e.target.value })} /></Field>
              <Field label="Unit">
                <select className={inputCls} value={form.unit} onChange={(e) => setForm({ ...form, unit: e.target.value })}>
                  {['MT', 'KG', 'CBM', 'TEU', 'FEU'].map((u) => <option key={u}>{u}</option>)}
                </select>
              </Field>
              <Field label="Indicative Price"><input type="number" step="0.01" min="0" className={inputCls} value={form.indicativePrice} onChange={(e) => setForm({ ...form, indicativePrice: e.target.value })} /></Field>
              <Field label="Currency">
                <select className={inputCls} value={form.currency} onChange={(e) => setForm({ ...form, currency: e.target.value })}>
                  {['INR', 'USD', 'EUR', 'AED'].map((c) => <option key={c}>{c}</option>)}
                </select>
              </Field>
              <Field label="Origin Country *"><input required className={inputCls} value={form.originCountry} onChange={(e) => setForm({ ...form, originCountry: e.target.value })} /></Field>
              <Field label="Destination Country *"><input required className={inputCls} value={form.destinationCountry} onChange={(e) => setForm({ ...form, destinationCountry: e.target.value })} /></Field>
              <Field label="Loading Port">
                <select className={inputCls} value={form.loadingPortId} onChange={(e) => setForm({ ...form, loadingPortId: e.target.value })}>
                  <option value="">None</option>
                  {ports.map((p) => <option key={p.id} value={p.id}>{p.name} ({p.code})</option>)}
                </select>
              </Field>
              <Field label="Destination Port">
                <select className={inputCls} value={form.destinationPortId} onChange={(e) => setForm({ ...form, destinationPortId: e.target.value })}>
                  <option value="">None</option>
                  {ports.map((p) => <option key={p.id} value={p.id}>{p.name} ({p.code})</option>)}
                </select>
              </Field>
              <Field label="Loading Date"><input type="date" className={inputCls} value={form.loadingDate} onChange={(e) => setForm({ ...form, loadingDate: e.target.value })} /></Field>
              <Field label="Estimated Arrival"><input type="date" className={inputCls} value={form.estimatedArrival} onChange={(e) => setForm({ ...form, estimatedArrival: e.target.value })} /></Field>
            </div>
            <Field label="Description"><textarea rows={3} className={inputCls} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></Field>
            <div className="flex justify-end gap-3 pt-2">
              <button type="button" onClick={() => setForm(null)} className={secondaryBtnCls}>Cancel</button>
              <button type="submit" disabled={saving} className={primaryBtnCls}>{saving ? 'Saving…' : 'Save Cargo Lot'}</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
