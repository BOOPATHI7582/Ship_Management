import { useCallback, useEffect, useState } from 'react'
import { Field, Modal, inputCls, primaryBtnCls, secondaryBtnCls } from '../../components/ui/admin'
import {
  DOCUMENT_CATEGORIES,
  downloadDocument,
  fetchMyDocuments,
  formatDate,
  formatBytes,
  uploadMyDocument,
} from '../../api/documents'
import { fetchMyEnquiries } from '../../api/client'
import { fetchMyInvoices } from '../../api/invoices'
import { fetchMyShipments } from '../../api/shipments'

const CATEGORY_BADGE = {
  BILL_OF_LADING: 'bg-sky-100 text-sky-800',
  PACKING_LIST: 'bg-violet-100 text-violet-800',
  CUSTOMS_DOCUMENT: 'bg-amber-100 text-amber-800',
  CERTIFICATE: 'bg-emerald-100 text-emerald-800',
  INVOICE: 'bg-blue-100 text-blue-800',
  PROOF_OF_PAYMENT: 'bg-teal-100 text-teal-800',
}

export default function DocumentsPage() {
  const [docs, setDocs] = useState(null)
  const [error, setError] = useState(null)
  const [uploadOpen, setUploadOpen] = useState(false)
  const [targets, setTargets] = useState([])
  const [target, setTarget] = useState('')
  const [file, setFile] = useState(null)
  const [category, setCategory] = useState('OTHER')
  const [title, setTitle] = useState('')
  const [saving, setSaving] = useState(false)

  const load = useCallback(() => {
    fetchMyDocuments()
      .then((res) => setDocs(res.data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load documents'))
  }, [])

  useEffect(() => { load() }, [load])

  async function openUpload() {
    setError(null)
    try {
      const [enquiries, invoices, shipments] = await Promise.all([
        fetchMyEnquiries(0, 50),
        fetchMyInvoices({ size: 50 }),
        fetchMyShipments({ size: 50 }),
      ])
      const opts = [
        ...(enquiries.data?.content || []).map((e) => ({ value: `ENQUIRY:${e.id}`, label: `Enquiry ${e.enquiryNo || '#' + e.id}` })),
        ...(invoices.data?.content || []).map((i) => ({ value: `INVOICE:${i.id}`, label: `Invoice ${i.invoiceNo}` })),
        ...(shipments.data?.content || []).map((s) => ({ value: `SHIPMENT:${s.id}`, label: `Shipment ${s.shipmentRef}` })),
      ]
      setTargets(opts)
      setTarget(opts[0]?.value || '')
      setUploadOpen(true)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load your records')
    }
  }

  async function upload(e) {
    e.preventDefault()
    if (!file || !target) return
    setSaving(true)
    setError(null)
    try {
      const [ownerType, ownerId] = target.split(':')
      const formData = new FormData()
      formData.append('file', file)
      formData.append('ownerType', ownerType)
      formData.append('ownerId', ownerId)
      formData.append('category', category)
      if (title) formData.append('title', title)
      await uploadMyDocument(formData)
      setUploadOpen(false)
      setFile(null)
      setTitle('')
      setCategory('OTHER')
      load()
    } catch (err) {
      setError(err.response?.data?.message || 'Upload failed')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="font-display text-2xl font-bold text-navy-950">Documents</h1>
          <p className="text-sm text-navy-500">Shipping documents, certificates and paperwork for all your records.</p>
        </div>
        <button type="button" onClick={openUpload} className={primaryBtnCls}>+ Attach Document</button>
      </div>

      {error && <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>}

      {docs && (
        <div className="overflow-x-auto rounded-2xl border border-navy-100 bg-white shadow-sm">
          <table className="w-full text-left text-sm">
            <thead className="bg-navy-50 text-xs uppercase tracking-wide text-navy-500">
              <tr>
                <th className="px-5 py-3">Title</th>
                <th className="px-5 py-3 hidden md:table-cell">Category</th>
                <th className="px-5 py-3 hidden lg:table-cell">Attached to</th>
                <th className="px-5 py-3 hidden lg:table-cell">Format / Size</th>
                <th className="px-5 py-3 hidden md:table-cell">Date</th>
                <th className="px-5 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-navy-100">
              {docs.map((d) => (
                <tr key={d.id}>
                  <td className="px-5 py-3.5 font-semibold text-navy-950">{d.title}</td>
                  <td className="hidden px-5 py-3.5 md:table-cell">
                    <span className={`rounded-full px-2.5 py-1 text-xs font-bold ${CATEGORY_BADGE[d.category] || 'bg-navy-100 text-navy-600'}`}>
                      {(d.category || '').replace(/_/g, ' ')}
                    </span>
                  </td>
                  <td className="hidden px-5 py-3.5 text-navy-600 lg:table-cell">{(d.ownerType || '').replace(/_/g, ' ')} #{d.ownerId}</td>
                  <td className="hidden px-5 py-3.5 text-navy-600 lg:table-cell">{(d.fileFormat || '').toUpperCase()} · {formatBytes(d.fileSizeBytes)}</td>
                  <td className="hidden px-5 py-3.5 text-navy-600 md:table-cell">{formatDate(d.createdAt)}</td>
                  <td className="whitespace-nowrap px-5 py-3.5 text-right">
                    <button
                      type="button"
                      onClick={() => downloadDocument(`/client/documents/${d.id}/download`, d.title)}
                      className={secondaryBtnCls}
                    >
                      Download
                    </button>
                  </td>
                </tr>
              ))}
              {docs.length === 0 && (
                <tr><td colSpan={6} className="px-5 py-8 text-center text-navy-400">No documents yet.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {uploadOpen && (
        <Modal title="Attach Document" onClose={() => setUploadOpen(false)}>
          <form onSubmit={upload} className="space-y-4">
            <Field label="Attach to *">
              <select required value={target} onChange={(e) => setTarget(e.target.value)} className={inputCls}>
                {targets.map((t) => <option key={t.value} value={t.value}>{t.label}</option>)}
                {targets.length === 0 && <option value="">No records available</option>}
              </select>
            </Field>
            <Field label="File * (max 10 MB)">
              <input required type="file" accept=".pdf,.jpg,.jpeg,.png,.webp,.doc,.docx,.xls,.xlsx" onChange={(e) => setFile(e.target.files[0])} className={inputCls} />
            </Field>
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <Field label="Category">
                <select value={category} onChange={(e) => setCategory(e.target.value)} className={inputCls}>
                  {DOCUMENT_CATEGORIES.map((c) => <option key={c} value={c}>{c.replace(/_/g, ' ')}</option>)}
                </select>
              </Field>
              <Field label="Title"><input placeholder="Export licence scan" className={inputCls} value={title} onChange={(e) => setTitle(e.target.value)} /></Field>
            </div>
            <div className="flex justify-end space-x-3 pt-2">
              <button type="button" onClick={() => setUploadOpen(false)} className={secondaryBtnCls}>Cancel</button>
              <button type="submit" disabled={saving || !target} className={primaryBtnCls}>{saving ? 'Uploading…' : 'Attach'}</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
