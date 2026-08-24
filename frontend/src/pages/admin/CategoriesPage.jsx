import { useCallback, useEffect, useState } from 'react'
import { createCategory, fetchAllCategories, updateCategory } from '../../api/admin'
import { Field, Modal, inputCls, primaryBtnCls, secondaryBtnCls } from '../../components/ui/admin'

const emptyForm = { id: null, name: '', description: '', active: true }

export default function CategoriesPage() {
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)
  const [form, setForm] = useState(null)
  const [saving, setSaving] = useState(false)

  const load = useCallback(() => {
    fetchAllCategories()
      .then((res) => setData(res.data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load categories'))
  }, [])

  useEffect(() => { load() }, [load])

  async function save(e) {
    e.preventDefault()
    setSaving(true)
    try {
      if (form.id) await updateCategory(form.id, form)
      else await createCategory(form)
      setForm(null)
      load()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save category')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="font-display text-2xl font-bold text-navy-950">Cargo Categories</h1>
        <button type="button" onClick={() => setForm({ ...emptyForm })} className={primaryBtnCls}>
          + Add Category
        </button>
      </div>

      {error && <div className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>}

      {data && (
        <div className="overflow-hidden rounded-2xl border border-navy-100 bg-white shadow-sm">
          <table className="w-full text-left text-sm">
            <thead className="bg-navy-50 text-xs uppercase tracking-wide text-navy-500">
              <tr>
                <th className="px-5 py-3">Name</th>
                <th className="px-5 py-3 hidden md:table-cell">Description</th>
                <th className="px-5 py-3">Visibility</th>
                <th className="px-5 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-navy-100">
              {data.map((category) => (
                <tr key={category.id}>
                  <td className="px-5 py-3.5 font-semibold text-navy-950">{category.name}</td>
                  <td className="hidden max-w-md px-5 py-3.5 text-navy-600 md:table-cell">{category.description || '—'}</td>
                  <td className="px-5 py-3.5">
                    <span className={`rounded-full px-2.5 py-1 text-xs font-bold ${
                      category.active ? 'bg-emerald-100 text-emerald-800' : 'bg-red-100 text-red-700'
                    }`}>
                      {category.active ? 'Visible' : 'Hidden'}
                    </span>
                  </td>
                  <td className="px-5 py-3.5 text-right">
                    <button type="button" onClick={() => setForm({ ...emptyForm, ...category })} className={secondaryBtnCls}>
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
        <Modal title={form.id ? `Edit Category — ${form.name}` : 'Add Category'} onClose={() => setForm(null)}>
          <form onSubmit={save} className="space-y-4">
            <Field label="Name *"><input required className={inputCls} value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} /></Field>
            <Field label="Description"><textarea rows={3} className={inputCls} value={form.description || ''} onChange={(e) => setForm({ ...form, description: e.target.value })} /></Field>
            <Field label="Visibility">
              <select className={inputCls} value={form.active ? '1' : '0'} onChange={(e) => setForm({ ...form, active: e.target.value === '1' })}>
                <option value="1">Visible on website</option>
                <option value="0">Hidden</option>
              </select>
            </Field>
            <div className="flex justify-end gap-3 pt-2">
              <button type="button" onClick={() => setForm(null)} className={secondaryBtnCls}>Cancel</button>
              <button type="submit" disabled={saving} className={primaryBtnCls}>{saving ? 'Saving…' : 'Save Category'}</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
