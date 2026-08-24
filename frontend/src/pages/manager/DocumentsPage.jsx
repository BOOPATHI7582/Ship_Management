import { useCallback, useEffect, useState } from 'react'
import { Field, Modal, inputCls, primaryBtnCls, secondaryBtnCls, dangerBtnCls } from '../../components/ui/admin'
import {
  DOCUMENT_CATEGORIES,
  deleteDocument,
  downloadDocument,
  fetchStaffDocuments,
  formatDate,
  formatBytes,
  uploadStaffDocument,
} from '../../api/documents'

const OWNER_TYPES = ['ENQUIRY', 'QUOTATION', 'PROFORMA_INVOICE', 'INVOICE', 'RECEIPT', 'SHIPMENT', 'VESSEL', 'CARGO', 'USER']

export default function DocumentsPage() {
  const [ownerType, setOwnerType] = useState('ENQUIRY')
  const [ownerId, setOwnerId] = useState('')
  const [docs, setDocs] = useState(null)
  const [error, setError] = useState(null)
  const [uploadOpen, setUploadOpen] = useState(false)
  const [file, setFile] = useState(null)
  const [category, setCategory] = useState('OTHER')
  const [title, setTitle] = useState('')
  const [saving, setSaving] = useState(false)

  const load = useCallback(() => {
    if (!ownerId) { setDocs(null); return }
    fetchStaffDocuments({ ownerType, ownerId })
      .then((res) => setDocs(res.data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load documents'))
  }, [ownerType, ownerId])

  useEffect(() => { load() }, [load])

  async function upload(e) {
    e.preventDefault()
    if (!file) return
    setSaving(true)
    setError(null)
    try {
      const formData = new FormData()
      formData.append('file', file)
      formData.append('ownerType', ownerType)
      formData.append('ownerId', ownerId)
      formData.append('category', category)
      if (title) formData.append('title', title)
      await uploadStaffDocument(formData)
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

  async function remove(id) {
    if (!window.confirm('Delete this document permanently?')) return
    try {
      await deleteDocument(id)
      load()
    } catch (err) {
      setError(err.response?.data?.message || 'Delete failed')
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="font-display text-2xl font-bold text-navy-950">Compliance Documents</h1>
          <p className="text-sm text-navy-500">Packing lists, bills of lading, certificates and contracts per record.</p>
        </div>
        <button type="button" onClick={() => setUploadOpen(true)} disabled={!ownerId} className={primaryBtnCls}>+ Upload</button>
      </div>

      {error && <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>}

      <div className="flex flex-wrap gap-3">
        <select value={ownerType} onChange={(e) => setOwnerType(e.target.value)} className={`${inputCls} max-w-xs`}>
          {OWNER_TYPES.map((t) => <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>)}
        </select>
        <input placeholder="Record ID…" value={ownerId} onChange={(e) => { setOwnerId(e.target.value.replace(/\D/g, '')) }} className={`${inputCls} max-w-[140px]`} />
      </div>

      {!ownerId && (
        <div className="rounded-2xl border border-dashed border-navy-200 bg-white p-8 text-center text-navy-400">
          Pick an owner type and record ID (e.g. ENQUIRY + 3) to view its documents.
        </div>
      )}

      {docs && (
        <div className="overflow-hidden rounded-2xl border border-navy-100 bg-white shadow-sm">
          <table className="w-full text-left text-sm">
            <thead className="bg-navy-50 text-xs uppercase tracking-wide text-navy-500">
              <tr>
                <th className="px-5 py-3">Title</th>
                <th className="px-5 py-3 hidden md:table-cell">Category</th>
                <th className="px-5 py-3 hidden lg:table-cell">Format / Size</th>
                <th className="px-5 py-3 hidden xl:table-cell">Uploaded by</th>
                <th className="px-5 py-3 hidden md:table-cell">Date</th>
                <th className="px-5 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-navy-100">
              {docs.map((d) => (
                <tr key={d.id}>
                  <td className="px-5 py-3.5 font-semibold text-navy-950">{d.title}</td>
                  <td className="hidden px-5 py-3.5 text-navy-600 md:table-cell">{(d.category || '').replace(/_/g, ' ')}</td>
                  <td className="hidden px-5 py-3.5 text-navy-600 lg:table-cell">{(d.fileFormat || '').toUpperCase()} · {formatBytes(d.fileSizeBytes)}</td>
                  <td className="hidden px-5 py-3.5 text-navy-600 xl:table-cell">{d.uploadedByEmail || '—'}</td>
                  <td className="hidden px-5 py-3.5 text-navy-600 md:table-cell">{formatDate(d.createdAt)}</td>
                  <td className="space-x-2 whitespace-nowrap px-5 py-3.5 text-right">
                    <button
                      type="button"
                      onClick={() => downloadDocument(`/manager/documents/${d.id}/download`, d.title)}
                      className={secondaryBtnCls}
                    >
                      Download
                    </button>
                    <button type="button" onClick={() => remove(d.id)} className={dangerBtnCls}>Delete</button>
                  </td>
                </tr>
              ))}
              {docs.length === 0 && (
                <tr><td colSpan={6} className="px-5 py-8 text-center text-navy-400">No documents attached to this record yet.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {uploadOpen && (
        <Modal title={`Attach Document — ${ownerType} #${ownerId}`} onClose={() => setUploadOpen(false)}>
          <form onSubmit={upload} className="space-y-4">
            <Field label="File * (max 10 MB)">
              <input required type="file" accept=".pdf,.jpg,.jpeg,.png,.webp,.doc,.docx,.xls,.xlsx" onChange={(e) => setFile(e.target.files[0])} className={inputCls} />
            </Field>
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <Field label="Category">
                <select value={category} onChange={(e) => setCategory(e.target.value)} className={inputCls}>
                  {DOCUMENT_CATEGORIES.map((c) => <option key={c} value={c}>{c.replace(/_/g, ' ')}</option>)}
                </select>
              </Field>
              <Field label="Title"><input placeholder="Bill of lading scan" className={inputCls} value={title} onChange={(e) => setTitle(e.target.value)} /></Field>
            </div>
            <div className="flex justify-end space-x-3 pt-2">
              <button type="button" onClick={() => setUploadOpen(false)} className={secondaryBtnCls}>Cancel</button>
              <button type="submit" disabled={saving} className={primaryBtnCls}>{saving ? 'Uploading…' : 'Upload'}</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
