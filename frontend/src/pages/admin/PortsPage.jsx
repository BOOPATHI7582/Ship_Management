import { useCallback, useEffect, useState } from 'react'
import { createPort, fetchAllPorts, updatePort } from '../../api/admin'
import { Field, Modal, inputCls, primaryBtnCls, secondaryBtnCls } from '../../components/ui/admin'

const emptyForm = { id: null, name: '', code: '', country: '', city: '', latitude: '', longitude: '', active: true }

export default function PortsPage() {
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)
  const [form, setForm] = useState(null)
  const [saving, setSaving] = useState(false)

  const load = useCallback(() => {
    fetchAllPorts()
      .then((res) => setData(res.data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load ports'))
  }, [])

  useEffect(() => { load() }, [load])

  async function save(e) {
    e.preventDefault()
    setSaving(true)
    try {
      if (form.id) await updatePort(form.id, form)
      else await createPort(form)
      setForm(null)
      load()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save port')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="font-display text-2xl font-bold text-navy-950">Ports</h1>
        <button type="button" onClick={() => setForm({ ...emptyForm })} className={primaryBtnCls}>
          + Add Port
        </button>
      </div>

      {error && <div className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>}

      {data && (
        <div className="overflow-x-auto rounded-2xl border border-navy-100 bg-white shadow-sm">
          <table className="w-full min-w-max text-left text-sm">
            <thead className="bg-navy-50 text-xs uppercase tracking-wide text-navy-500">
              <tr>
                <th className="px-5 py-3">Port</th>
                <th className="px-5 py-3">Code</th>
                <th className="px-5 py-3 hidden md:table-cell">Country</th>
                <th className="px-5 py-3 hidden lg:table-cell">Coordinates</th>
                <th className="px-5 py-3">Status</th>
                <th className="px-5 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-navy-100">
              {data.map((port) => (
                <tr key={port.id}>
                  <td className="px-5 py-3.5 font-semibold text-navy-950">{port.name}</td>
                  <td className="px-5 py-3.5">{port.code}</td>
                  <td className="hidden px-5 py-3.5 md:table-cell">{[port.city, port.country].filter(Boolean).join(', ')}</td>
                  <td className="hidden px-5 py-3.5 text-xs text-navy-500 lg:table-cell">
                    {port.latitude != null ? `${port.latitude}, ${port.longitude}` : '—'}
                  </td>
                  <td className="px-5 py-3.5">
                    <span className={`rounded-full px-2.5 py-1 text-xs font-bold ${
                      port.active ? 'bg-emerald-100 text-emerald-800' : 'bg-red-100 text-red-700'
                    }`}>
                      {port.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="px-5 py-3.5 text-right">
                    <button
                      type="button"
                      onClick={() => setForm({
                        ...emptyForm, ...port,
                        latitude: port.latitude ?? '',
                        longitude: port.longitude ?? '',
                      })}
                      className={secondaryBtnCls}
                    >
                      Edit
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {form && (
        <Modal title={form.id ? `Edit Port — ${form.name}` : 'Add Port'} onClose={() => setForm(null)}>
          <form onSubmit={save} className="space-y-4">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <Field label="Port Name *"><input required className={inputCls} value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} /></Field>
              <Field label="UN/LOCODE *"><input required maxLength={10} className={inputCls} value={form.code} onChange={(e) => setForm({ ...form, code: e.target.value.toUpperCase() })} placeholder="e.g. INNSA" /></Field>
              <Field label="Country *"><input required className={inputCls} value={form.country} onChange={(e) => setForm({ ...form, country: e.target.value })} /></Field>
              <Field label="City"><input className={inputCls} value={form.city} onChange={(e) => setForm({ ...form, city: e.target.value })} /></Field>
              <Field label="Latitude"><input type="number" step="any" min="-90" max="90" className={inputCls} value={form.latitude} onChange={(e) => setForm({ ...form, latitude: e.target.value })} /></Field>
              <Field label="Longitude"><input type="number" step="any" min="-180" max="180" className={inputCls} value={form.longitude} onChange={(e) => setForm({ ...form, longitude: e.target.value })} /></Field>
            </div>
            <Field label="Status">
              <select className={inputCls} value={form.active ? '1' : '0'} onChange={(e) => setForm({ ...form, active: e.target.value === '1' })}>
                <option value="1">Active</option>
                <option value="0">Inactive</option>
              </select>
            </Field>
            <div className="flex justify-end gap-3 pt-2">
              <button type="button" onClick={() => setForm(null)} className={secondaryBtnCls}>Cancel</button>
              <button type="submit" disabled={saving} className={primaryBtnCls}>{saving ? 'Saving…' : 'Save Port'}</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
