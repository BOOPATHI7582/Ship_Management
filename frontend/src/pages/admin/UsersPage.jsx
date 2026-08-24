import { useCallback, useEffect, useState } from 'react'
import {
  fetchAdminUsers,
  updateAdminUser,
} from '../../api/admin'
import { Field, Modal, Pager, inputCls, primaryBtnCls, secondaryBtnCls } from '../../components/ui/admin'

const roles = ['CLIENT', 'SHIP_MANAGER', 'ADMIN']

const emptyEdit = { id: null, fullName: '', companyName: '', phone: '', country: '', role: 'CLIENT', active: true }

export default function UsersPage() {
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [role, setRole] = useState('')
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)
  const [edit, setEdit] = useState(null)
  const [saving, setSaving] = useState(false)

  const load = useCallback(() => {
    fetchAdminUsers({ page, size: 10, search: search || undefined, role: role || undefined })
      .then((res) => setData(res.data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load users'))
  }, [page, search, role])

  useEffect(() => {
    setError(null)
    load()
  }, [load])

  function openEdit(user) {
    setEdit({
      id: user.id,
      fullName: user.fullName || '',
      companyName: user.companyName || '',
      phone: user.phone || '',
      country: user.country || '',
      role: user.role,
      active: user.active,
    })
  }

  async function saveEdit(e) {
    e.preventDefault()
    setSaving(true)
    try {
      await updateAdminUser(edit.id, edit)
      setEdit(null)
      load()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update user')
    } finally {
      setSaving(false)
    }
  }

  async function toggleActive(user) {
    try {
      await updateAdminUser(user.id, { active: !user.active })
      load()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update status')
    }
  }

  return (
    <div className="space-y-5">
      <h1 className="font-display text-2xl font-bold text-navy-950">Users</h1>

      <div className="flex flex-wrap items-center gap-3">
        <input
          value={search}
          onChange={(e) => { setPage(0); setSearch(e.target.value) }}
          placeholder="Search name or email…"
          className={`${inputCls} max-w-xs`}
        />
        <select
          value={role}
          onChange={(e) => { setPage(0); setRole(e.target.value) }}
          className={`${inputCls} max-w-40`}
        >
          <option value="">All roles</option>
          {roles.map((r) => <option key={r}>{r}</option>)}
        </select>
      </div>

      {error && <div className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>}

      {data && (
        <>
          <div className="overflow-x-auto rounded-2xl border border-navy-100 bg-white shadow-sm">
            <table className="w-full min-w-max text-left text-sm">
              <thead className="bg-navy-50 text-xs uppercase tracking-wide text-navy-500">
                <tr>
                  <th className="px-5 py-3">User</th>
                  <th className="px-5 py-3">Role</th>
                  <th className="px-5 py-3">Status</th>
                  <th className="px-5 py-3 hidden lg:table-cell">Last Login</th>
                  <th className="px-5 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-navy-100">
                {data.content.map((user) => (
                  <tr key={user.id}>
                    <td className="px-5 py-3.5">
                      <div className="font-semibold text-navy-950">{user.fullName}</div>
                      <div className="text-xs text-navy-400">{user.email}</div>
                    </td>
                    <td className="px-5 py-3.5">
                      <span className={`rounded-full px-2.5 py-1 text-xs font-bold ${
                        user.role === 'ADMIN' ? 'bg-gold-500/20 text-navy-900' : 'bg-navy-100 text-navy-700'
                      }`}>
                        {user.role}
                      </span>
                    </td>
                    <td className="px-5 py-3.5">
                      <span className={`rounded-full px-2.5 py-1 text-xs font-bold ${
                        user.active ? 'bg-emerald-100 text-emerald-800' : 'bg-red-100 text-red-700'
                      }`}>
                        {user.active ? 'Active' : 'Disabled'}
                      </span>
                    </td>
                    <td className="hidden px-5 py-3.5 text-xs text-navy-500 lg:table-cell">
                      {user.lastLoginAt ? new Date(user.lastLoginAt).toLocaleString() : '—'}
                    </td>
                    <td className="px-5 py-3.5">
                      <div className="flex justify-end gap-2">
                        <button type="button" onClick={() => openEdit(user)} className={secondaryBtnCls}>Edit</button>
                        <button
                          type="button"
                          onClick={() => toggleActive(user)}
                          className={user.active ? secondaryBtnCls : primaryBtnCls}
                        >
                          {user.active ? 'Disable' : 'Enable'}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
                {data.content.length === 0 && (
                  <tr><td colSpan={5} className="px-5 py-10 text-center text-navy-400">No users found.</td></tr>
                )}
              </tbody>
            </table>
          </div>
          <Pager page={page} totalPages={data.totalPages} onPage={setPage} />
        </>
      )}

      {edit && (
        <Modal title={`Edit User — ${edit.id}`} onClose={() => setEdit(null)}>
          <form onSubmit={saveEdit} className="space-y-4">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <Field label="Full Name"><input className={inputCls} value={edit.fullName} onChange={(e) => setEdit({ ...edit, fullName: e.target.value })} /></Field>
              <Field label="Company"><input className={inputCls} value={edit.companyName} onChange={(e) => setEdit({ ...edit, companyName: e.target.value })} /></Field>
              <Field label="Phone"><input className={inputCls} value={edit.phone} onChange={(e) => setEdit({ ...edit, phone: e.target.value })} /></Field>
              <Field label="Country"><input className={inputCls} value={edit.country} onChange={(e) => setEdit({ ...edit, country: e.target.value })} /></Field>
              <Field label="Role">
                <select className={inputCls} value={edit.role} onChange={(e) => setEdit({ ...edit, role: e.target.value })}>
                  {roles.map((r) => <option key={r}>{r}</option>)}
                </select>
              </Field>
              <Field label="Status">
                <select className={inputCls} value={edit.active ? '1' : '0'} onChange={(e) => setEdit({ ...edit, active: e.target.value === '1' })}>
                  <option value="1">Active</option>
                  <option value="0">Disabled</option>
                </select>
              </Field>
            </div>
            <div className="flex justify-end gap-3 pt-2">
              <button type="button" onClick={() => setEdit(null)} className={secondaryBtnCls}>Cancel</button>
              <button type="submit" disabled={saving} className={primaryBtnCls}>{saving ? 'Saving…' : 'Save Changes'}</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
