import { useCallback, useEffect, useState } from 'react'
import { createVessel, fetchVessels, updateVessel } from '../../api/admin'
import { Field, Modal, Pager, inputCls, primaryBtnCls, secondaryBtnCls } from '../../components/ui/admin'

const statuses = ['AVAILABLE', 'LOADING', 'LOADING_COMPLETED', 'IN_TRANSIT', 'ARRIVED', 'MAINTENANCE']

const emptyForm = {
  name: '', imoNumber: '', vesselType: 'Bulk Carrier', capacity: '', capacityUnit: 'MT',
  flag: '', currentLocation: '', status: 'AVAILABLE', managementCompany: '',
  managementContact: '', description: '',
}

export default function VesselsPage() {
  const [page, setPage] = useState(0)
  const [statusFilter, setStatusFilter] = useState('')
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)
  const [form, setForm] = useState(null)
  const [saving, setSaving] = useState(false)

  const load = useCallback(() => {
    fetchVessels({ page, size: 10, status: statusFilter || undefined })
      .then((res) => setData(res.data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load vessels'))
  }, [page, statusFilter])

  useEffect(() => {
    setError(null)
    load()
  }, [load])

  async function save(e) {
    e.preventDefault()
    setSaving(true)
    try {
      if (form.id) await updateVessel(form.id, form)
      else await createVessel(form)
      setForm(null)
      load()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save vessel')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="font-display text-2xl font-bold text-navy-950">Vessels</h1>
        <button type="button" onClick={() => setForm({ ...emptyForm })} className={primaryBtnCls}>
          + Add Vessel
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
                  <th className="px-5 py-3">Vessel</th>
                  <th className="px-5 py-3 hidden md:table-cell">IMO</th>
                  <th className="px-5 py-3">Capacity</th>
                  <th className="px-5 py-3 hidden lg:table-cell">Flag</th>
                  <th className="px-5 py-3">Status</th>
                  <th className="px-5 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-navy-100">
                {data.content.map((vessel) => (
                  <tr key={vessel.id}>
                    <td className="px-5 py-3.5">
                      <div className="font-semibold text-navy-950">{vessel.name}</div>
                      <div className="text-xs text-navy-400">{vessel.vesselType}</div>
                    </td>
                    <td className="hidden px-5 py-3.5 md:table-cell">{vessel.imoNumber || '—'}</td>
                    <td className="px-5 py-3.5">{vessel.capacity ? `${vessel.capacity} ${vessel.capacityUnit}` : '—'}</td>
                    <td className="hidden px-5 py-3.5 lg:table-cell">{vessel.flag || '—'}</td>
                    <td className="px-5 py-3.5">
                      <span className={`rounded-full px-2.5 py-1 text-xs font-bold ${
                        vessel.status === 'AVAILABLE' ? 'bg-emerald-100 text-emerald-800' : 'bg-navy-100 text-navy-700'
                      }`}>
                        {vessel.status.replace(/_/g, ' ')}
                      </span>
                    </td>
                    <td className="px-5 py-3.5 text-right">
                      <button type="button" onClick={() => setForm({ ...emptyForm, ...vessel })} className={secondaryBtnCls}>
                        Edit
                      </button>
                    </td>
                  </tr>
                ))}
                {data.content.length === 0 && (
                  <tr><td colSpan={6} className="px-5 py-10 text-center text-navy-400">No vessels yet.</td></tr>
                )}
              </tbody>
            </table>
          </div>
          <Pager page={page} totalPages={data.totalPages} onPage={setPage} />
        </>
      )}

      {form && (
        <Modal title={form.id ? `Edit Vessel — ${form.name}` : 'Add Vessel'} onClose={() => setForm(null)} wide>
          <form onSubmit={save} className="space-y-4">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <Field label="Name *"><input required className={inputCls} value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} /></Field>
              <Field label="IMO Number"><input className={inputCls} value={form.imoNumber} onChange={(e) => setForm({ ...form, imoNumber: e.target.value })} /></Field>
              <Field label="Vessel Type *">
                <select className={inputCls} value={form.vesselType} onChange={(e) => setForm({ ...form, vesselType: e.target.value })}>
                  {['Bulk Carrier', 'Container Ship', 'Tanker', 'General Cargo', 'Ro-Ro', 'Other'].map((t) => <option key={t}>{t}</option>)}
                </select>
              </Field>
              <Field label="Capacity"><input type="number" step="any" min="0" className={inputCls} value={form.capacity} onChange={(e) => setForm({ ...form, capacity: e.target.value })} /></Field>
              <Field label="Capacity Unit">
                <select className={inputCls} value={form.capacityUnit} onChange={(e) => setForm({ ...form, capacityUnit: e.target.value })}>
                  {['MT', 'DWT', 'TEU', 'CBM'].map((u) => <option key={u}>{u}</option>)}
                </select>
              </Field>
              <Field label="Status">
                <select className={inputCls} value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
                  {statuses.map((s) => <option key={s}>{s}</option>)}
                </select>
              </Field>
              <Field label="Flag"><input className={inputCls} value={form.flag} onChange={(e) => setForm({ ...form, flag: e.target.value })} placeholder="e.g. Panama" /></Field>
              <Field label="Current Location"><input className={inputCls} value={form.currentLocation} onChange={(e) => setForm({ ...form, currentLocation: e.target.value })} /></Field>
              <Field label="Management Company"><input className={inputCls} value={form.managementCompany} onChange={(e) => setForm({ ...form, managementCompany: e.target.value })} /></Field>
              <Field label="Management Contact"><input className={inputCls} value={form.managementContact} onChange={(e) => setForm({ ...form, managementContact: e.target.value })} /></Field>
            </div>
            <Field label="Description"><textarea rows={3} className={inputCls} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></Field>
            <div className="flex justify-end gap-3 pt-2">
              <button type="button" onClick={() => setForm(null)} className={secondaryBtnCls}>Cancel</button>
              <button type="submit" disabled={saving} className={primaryBtnCls}>{saving ? 'Saving…' : 'Save Vessel'}</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
