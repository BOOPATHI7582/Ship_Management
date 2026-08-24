import { useCallback, useEffect, useState } from 'react'
import api from '../../api/axios'
import { Field, Modal, Pager, inputCls, primaryBtnCls, secondaryBtnCls } from '../../components/ui/admin'
import { EmptyState } from '../../components/ui/feedback'
import ShipmentTimeline from '../../components/ShipmentTimeline'
import {
  SHIPMENT_STATUSES,
  addShipmentProgress,
  createShipment,
  fetchShipments,
  fetchShipment,
  statusBadge,
  statusLabel,
  statusPercent,
} from '../../api/shipments'
import { money, formatDate } from '../../api/invoices'

const emptyForm = {
  clientId: '',
  quotationId: '',
  cargoId: '',
  vesselId: '',
  loadingPortId: '',
  destinationPortId: '',
  quantity: '',
  unit: 'MT',
  originCountry: '',
  destinationCountry: '',
  loadingDate: '',
  estimatedArrival: '',
  finalPrice: '',
  currency: 'INR',
  notes: '',
}

const emptyProgress = { status: '', locationLabel: '', latitude: '', longitude: '', notes: '' }

export default function ShipmentsPage() {
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)
  const [page, setPage] = useState(0)
  const [statusFilter, setStatusFilter] = useState('')
  const [search, setSearch] = useState('')
  const [form, setForm] = useState(null)
  const [options, setOptions] = useState(null)
  const [saving, setSaving] = useState(false)
  const [detail, setDetail] = useState(null)
  const [progressForm, setProgressForm] = useState(null)
  const [actionError, setActionError] = useState(null)

  const load = useCallback(() => {
    const params = { page, size: 10 }
    if (statusFilter) params.status = statusFilter
    if (search) params.search = search
    fetchShipments(params)
      .then((res) => setData(res.data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load shipments'))
  }, [page, statusFilter, search])

  useEffect(() => { load() }, [load])

  async function openCreate() {
    setError(null)
    try {
      const [vessels, cargo, ports, clients] = await Promise.all([
        apiGet('/manager/catalog/vessels'),
        apiGet('/manager/catalog/cargo'),
        apiGet('/manager/catalog/ports'),
        apiGet('/manager/catalog/clients'),
      ])
      setOptions({ vessels, cargo, ports, clients })
      setForm({ ...emptyForm })
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load reference data')
    }
  }

  async function save(e) {
    e.preventDefault()
    setSaving(true)
    try {
      await createShipment({
        clientId: Number(form.clientId),
        quotationId: form.quotationId ? Number(form.quotationId) : null,
        cargoId: form.cargoId ? Number(form.cargoId) : null,
        vesselId: form.vesselId ? Number(form.vesselId) : null,
        loadingPortId: form.loadingPortId ? Number(form.loadingPortId) : null,
        destinationPortId: form.destinationPortId ? Number(form.destinationPortId) : null,
        quantity: form.quantity ? Number(form.quantity) : null,
        unit: form.unit || null,
        originCountry: form.originCountry || null,
        destinationCountry: form.destinationCountry || null,
        loadingDate: form.loadingDate || null,
        estimatedArrival: form.estimatedArrival || null,
        finalPrice: form.finalPrice ? Number(form.finalPrice) : null,
        currency: form.currency || 'INR',
        notes: form.notes || null,
      })
      setForm(null)
      setPage(0)
      setStatusFilter('')
      setSearch('')
      load()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to book shipment')
    } finally {
      setSaving(false)
    }
  }

  async function openDetail(id) {
    setActionError(null)
    try {
      const res = await fetchShipment(id)
      setDetail(res.data)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load shipment')
    }
  }

  async function saveProgress(e) {
    e.preventDefault()
    setSaving(true)
    setActionError(null)
    try {
      await addShipmentProgress(detail.id, {
        status: progressForm.status || null,
        locationLabel: progressForm.locationLabel || null,
        latitude: progressForm.latitude ? Number(progressForm.latitude) : null,
        longitude: progressForm.longitude ? Number(progressForm.longitude) : null,
        notes: progressForm.notes || null,
      })
      const res = await fetchShipment(detail.id)
      setDetail(res.data)
      setProgressForm(null)
      load()
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to record update')
    } finally {
      setSaving(false)
    }
  }

  const nextStatuses = detail
    ? SHIPMENT_STATUSES.slice(SHIPMENT_STATUSES.indexOf(detail.status))
    : []

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="font-display text-2xl font-bold text-navy-950">Shipments</h1>
          <p className="text-sm text-navy-500">Operational lifecycle from booking to completion - forward-only, fully tracked.</p>
        </div>
        <button type="button" onClick={openCreate} className={primaryBtnCls}>+ Book Shipment</button>
      </div>

      {error && <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>}

      <div className="flex flex-wrap gap-3">
        <select value={statusFilter} onChange={(e) => { setPage(0); setStatusFilter(e.target.value) }} className={`${inputCls} max-w-xs`}>
          <option value="">All statuses</option>
          {SHIPMENT_STATUSES.map((s) => <option key={s} value={s}>{statusLabel(s)}</option>)}
        </select>
        <input placeholder="Search ref / client / location…" value={search} onChange={(e) => { setPage(0); setSearch(e.target.value) }} className={`${inputCls} max-w-xs`} />
      </div>

      {data && (
        <>
          <div className="overflow-hidden rounded-2xl border border-navy-100 bg-white shadow-sm">
            <table className="w-full text-left text-sm">
              <thead className="bg-navy-50 text-xs uppercase tracking-wide text-navy-500">
                <tr>
                  <th className="px-5 py-3">Ref</th>
                  <th className="px-5 py-3 hidden md:table-cell">Client</th>
                  <th className="px-5 py-3 hidden lg:table-cell">Route</th>
                  <th className="px-5 py-3 hidden lg:table-cell">Vessel</th>
                  <th className="px-5 py-3 hidden xl:table-cell">ETD → ETA</th>
                  <th className="px-5 py-3">Progress</th>
                  <th className="px-5 py-3">Status</th>
                  <th className="px-5 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-navy-100">
                {data.content.map((s) => (
                  <tr key={s.id}>
                    <td className="px-5 py-3.5 font-semibold text-navy-950">{s.shipmentRef}</td>
                    <td className="hidden px-5 py-3.5 text-navy-600 md:table-cell">{s.clientCompanyName || '—'}</td>
                    <td className="hidden px-5 py-3.5 text-navy-600 lg:table-cell">
                      {[s.originCountry || s.loadingPortName, s.destinationCountry || s.destinationPortName].filter(Boolean).join(' → ') || '—'}
                    </td>
                    <td className="hidden px-5 py-3.5 text-navy-600 lg:table-cell">{s.vesselName || '—'}</td>
                    <td className="hidden px-5 py-3.5 text-navy-600 xl:table-cell">{formatDate(s.loadingDate)} → {formatDate(s.estimatedArrival)}</td>
                    <td className="px-5 py-3.5">
                      <div className="h-2 w-24 overflow-hidden rounded-full bg-navy-100">
                        <div className="h-full rounded-full bg-sky-500 transition-all" style={{ width: `${statusPercent(s.status)}%` }} />
                      </div>
                      <span className="mt-1 block text-[11px] text-navy-400">{statusPercent(s.status)}%</span>
                    </td>
                    <td className="px-5 py-3.5">
                      <span className={`rounded-full px-2.5 py-1 text-xs font-bold ${statusBadge(s.status)}`}>{statusLabel(s.status)}</span>
                    </td>
                    <td className="px-5 py-3.5 text-right">
                      <button type="button" onClick={() => openDetail(s.id)} className={secondaryBtnCls}>Track</button>
                    </td>
                  </tr>
                ))}
                {data.content.length === 0 && (
                  <tr>
                    <td colSpan={8}>
                      <EmptyState
                        icon="search"
                        title="No shipments found"
                        hint="Book a shipment from an accepted quotation to start tracking."
                      />
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
          <Pager page={data.number} totalPages={data.totalPages} onPage={setPage} />
        </>
      )}

      {/* Create modal */}
      {form && options && (
        <Modal title="Book Shipment" onClose={() => setForm(null)} wide>
          <form onSubmit={save} className="space-y-4">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <Field label="Client *">
                <select required value={form.clientId} onChange={(e) => setForm({ ...form, clientId: e.target.value })} className={inputCls}>
                  <option value="">Select client…</option>
                  {options.clients.map((c) => <option key={c.id} value={c.id}>{c.label}{c.sublabel ? ` (${c.sublabel})` : ''}</option>)}
                </select>
              </Field>
              <Field label="Vessel">
                <select value={form.vesselId} onChange={(e) => setForm({ ...form, vesselId: e.target.value })} className={inputCls}>
                  <option value="">Unassigned</option>
                  {options.vessels.map((v) => <option key={v.id} value={v.id}>{v.label}{v.sublabel ? ` — ${v.sublabel}` : ''}</option>)}
                </select>
              </Field>
              <Field label="Cargo Lot">
                <select value={form.cargoId} onChange={(e) => setForm({ ...form, cargoId: e.target.value })} className={inputCls}>
                  <option value="">None</option>
                  {options.cargo.map((c) => <option key={c.id} value={c.id}>{c.label}{c.sublabel ? ` (${c.sublabel})` : ''}</option>)}
                </select>
              </Field>
              <div className="grid grid-cols-2 gap-3">
                <Field label="Quantity"><input type="number" min="0" step="0.0001" className={inputCls} value={form.quantity} onChange={(e) => setForm({ ...form, quantity: e.target.value })} /></Field>
                <Field label="Unit"><input className={inputCls} placeholder="MT" value={form.unit} onChange={(e) => setForm({ ...form, unit: e.target.value })} /></Field>
              </div>
            </div>

            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <Field label="Loading Port">
                <select value={form.loadingPortId} onChange={(e) => setForm({ ...form, loadingPortId: e.target.value })} className={inputCls}>
                  <option value="">Select port…</option>
                  {options.ports.map((p) => <option key={p.id} value={p.id}>{p.label}{p.sublabel ? ` (${p.sublabel})` : ''}</option>)}
                </select>
              </Field>
              <Field label="Destination Port">
                <select value={form.destinationPortId} onChange={(e) => setForm({ ...form, destinationPortId: e.target.value })} className={inputCls}>
                  <option value="">Select port…</option>
                  {options.ports.map((p) => <option key={p.id} value={p.id}>{p.label}{p.sublabel ? ` (${p.sublabel})` : ''}</option>)}
                </select>
              </Field>
            </div>

            <div className="grid grid-cols-1 gap-4 sm:grid-cols-4">
              <Field label="Origin Country"><input className={inputCls} placeholder="India" value={form.originCountry} onChange={(e) => setForm({ ...form, originCountry: e.target.value })} /></Field>
              <Field label="Destination Country"><input className={inputCls} placeholder="UAE" value={form.destinationCountry} onChange={(e) => setForm({ ...form, destinationCountry: e.target.value })} /></Field>
              <Field label="Loading Date"><input type="date" className={inputCls} value={form.loadingDate} onChange={(e) => setForm({ ...form, loadingDate: e.target.value })} /></Field>
              <Field label="Estimated Arrival"><input type="date" className={inputCls} value={form.estimatedArrival} onChange={(e) => setForm({ ...form, estimatedArrival: e.target.value })} /></Field>
            </div>

            <div className="grid grid-cols-1 gap-4 sm:grid-cols-4">
              <Field label="Final Price"><input type="number" min="0" step="0.01" className={inputCls} value={form.finalPrice} onChange={(e) => setForm({ ...form, finalPrice: e.target.value })} /></Field>
              <Field label="Currency"><input maxLength={3} className={inputCls} value={form.currency} onChange={(e) => setForm({ ...form, currency: e.target.value.toUpperCase() })} /></Field>
              <div className="sm:col-span-2"><Field label="Notes"><input className={inputCls} value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })} /></Field></div>
            </div>

            <div className="flex justify-end space-x-3 pt-2">
              <button type="button" onClick={() => setForm(null)} className={secondaryBtnCls}>Cancel</button>
              <button type="submit" disabled={saving} className={primaryBtnCls}>{saving ? 'Booking…' : 'Book Shipment'}</button>
            </div>
          </form>
        </Modal>
      )}

      {/* Track modal */}
      {detail && (
        <Modal title={`${detail.shipmentRef} — ${detail.clientCompanyName || 'Client'}`} onClose={() => { setDetail(null); setProgressForm(null) }} wide>
          <div className="mb-4 flex items-center gap-3">
            <span className={`rounded-full px-3 py-1 text-xs font-bold ${statusBadge(detail.status)}`}>{statusLabel(detail.status)}</span>
            <div className="h-2 flex-1 overflow-hidden rounded-full bg-navy-100">
              <div className="h-full rounded-full bg-sky-500" style={{ width: `${statusPercent(detail.status)}%` }} />
            </div>
            <span className="text-xs font-semibold text-navy-500">{statusPercent(detail.status)}%</span>
          </div>

          <dl className="grid grid-cols-1 gap-x-6 gap-y-2 text-sm sm:grid-cols-2">
            <dt className="text-navy-500">Route</dt><dd>{[detail.originCountry || detail.loadingPortName, detail.destinationCountry || detail.destinationPortName].filter(Boolean).join(' → ') || '—'}</dd>
            <dt className="text-navy-500">Vessel</dt><dd>{detail.vesselName || '—'}{detail.vesselImoNumber ? ` (IMO ${detail.vesselImoNumber})` : ''}</dd>
            <dt className="text-navy-500">Cargo</dt><dd>{detail.cargoName || '—'}{detail.quantity ? ` — ${Number(detail.quantity)} ${detail.unit || ''}` : ''}</dd>
            <dt className="text-navy-500">Current position</dt><dd>{detail.currentLocation || '—'}{detail.currentLatitude != null ? ` (${Number(detail.currentLatitude).toFixed(3)}, ${Number(detail.currentLongitude).toFixed(3)})` : ''}</dd>
            <dt className="text-navy-500">ETD → ETA</dt><dd>{formatDate(detail.loadingDate)} → {formatDate(detail.estimatedArrival)}{detail.actualArrival ? ` (arrived ${formatDate(detail.actualArrival)})` : ''}</dd>
            <dt className="text-navy-500">Final price</dt><dd>{money(detail.finalPrice, detail.currency)}</dd>
            <dt className="text-navy-500">Public reference</dt><dd className="font-mono text-xs">{detail.shipmentRef}</dd>
          </dl>

          {!progressForm && detail.status !== 'COMPLETED' && (
            <button type="button" onClick={() => setProgressForm({ ...emptyProgress })} className={`${primaryBtnCls} mt-4`}>+ Add Progress Update</button>
          )}

          {progressForm && (
            <form onSubmit={saveProgress} className="mt-4 space-y-3 rounded-xl border border-navy-100 bg-navy-50/50 p-4">
              <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                <Field label="New Status">
                  <select value={progressForm.status} onChange={(e) => setProgressForm({ ...progressForm, status: e.target.value })} className={inputCls}>
                    <option value="">Position update only (no state change)</option>
                    {nextStatuses.map((s) => <option key={s} value={s}>{statusLabel(s)}{s === detail.status ? ' (current)' : ''}</option>)}
                  </select>
                </Field>
                <Field label="Location Label"><input placeholder="JNPT Terminal, Mumbai" className={inputCls} value={progressForm.locationLabel} onChange={(e) => setProgressForm({ ...progressForm, locationLabel: e.target.value })} /></Field>
                <div className="grid grid-cols-2 gap-3">
                  <Field label="Latitude"><input type="number" step="any" min="-90" max="90" placeholder="18.9517" className={inputCls} value={progressForm.latitude} onChange={(e) => setProgressForm({ ...progressForm, latitude: e.target.value })} /></Field>
                  <Field label="Longitude"><input type="number" step="any" min="-180" max="180" placeholder="72.9457" className={inputCls} value={progressForm.longitude} onChange={(e) => setProgressForm({ ...progressForm, longitude: e.target.value })} /></Field>
                </div>
                <Field label="Notes"><input className={inputCls} value={progressForm.notes} onChange={(e) => setProgressForm({ ...progressForm, notes: e.target.value })} /></Field>
              </div>
              {actionError && <p className="text-sm text-red-600">{actionError}</p>}
              <div className="flex justify-end space-x-3">
                <button type="button" onClick={() => setProgressForm(null)} className={secondaryBtnCls}>Cancel</button>
                <button type="submit" disabled={saving} className={primaryBtnCls}>{saving ? 'Recording…' : 'Record Update'}</button>
              </div>
            </form>
          )}

          <h3 className="mb-3 mt-6 text-xs font-bold uppercase tracking-wide text-navy-500">Tracking Timeline</h3>
          <ShipmentTimeline timeline={detail.timeline} />
        </Modal>
      )}
    </div>
  )
}

async function apiGet(path) {
  const res = await api.get(path)
  return res.data.data
}
