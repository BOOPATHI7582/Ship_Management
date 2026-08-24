import { useEffect, useState } from 'react'
import { fetchAdminClients } from '../../api/admin'
import { Pager } from '../../components/ui/admin'

export default function ClientsPage() {
  const [page, setPage] = useState(0)
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelled = false
    setError(null)
    fetchAdminClients({ page, size: 10 })
      .then((res) => { if (!cancelled) setData(res.data) })
      .catch((err) => { if (!cancelled) setError(err.response?.data?.message || 'Failed to load clients') })
    return () => { cancelled = true }
  }, [page])

  return (
    <div className="space-y-5">
      <h1 className="font-display text-2xl font-bold text-navy-950">Clients</h1>

      {error && <div className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>}

      {data && (
        <>
          <div className="overflow-x-auto rounded-2xl border border-navy-100 bg-white shadow-sm">
            <table className="w-full min-w-max text-left text-sm">
              <thead className="bg-navy-50 text-xs uppercase tracking-wide text-navy-500">
                <tr>
                  <th className="px-5 py-3">Client</th>
                  <th className="px-5 py-3 hidden md:table-cell">Company</th>
                  <th className="px-5 py-3 hidden lg:table-cell">Contact</th>
                  <th className="px-5 py-3">Location</th>
                  <th className="px-5 py-3">Status</th>
                  <th className="px-5 py-3 hidden xl:table-cell">Registered</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-navy-100">
                {data.content.map((client) => (
                  <tr key={client.userId}>
                    <td className="px-5 py-3.5">
                      <div className="font-semibold text-navy-950">{client.fullName}</div>
                      <div className="text-xs text-navy-400">{client.email}</div>
                    </td>
                    <td className="hidden px-5 py-3.5 md:table-cell">{client.companyName || '—'}</td>
                    <td className="hidden px-5 py-3.5 lg:table-cell">
                      <div>{client.phone || '—'}</div>
                      <div className="text-xs text-navy-400">{client.gstin ? `GSTIN ${client.gstin}` : ''}</div>
                    </td>
                    <td className="px-5 py-3.5">
                      {[client.city, client.state, client.country].filter(Boolean).join(', ') || '—'}
                    </td>
                    <td className="px-5 py-3.5">
                      <span className={`rounded-full px-2.5 py-1 text-xs font-bold ${
                        client.active ? 'bg-emerald-100 text-emerald-800' : 'bg-red-100 text-red-700'
                      }`}>
                        {client.active ? 'Active' : 'Disabled'}
                      </span>
                    </td>
                    <td className="hidden px-5 py-3.5 text-xs text-navy-500 xl:table-cell">
                      {new Date(client.registeredAt).toLocaleDateString()}
                    </td>
                  </tr>
                ))}
                {data.content.length === 0 && (
                  <tr><td colSpan={6} className="px-5 py-10 text-center text-navy-400">No clients registered yet.</td></tr>
                )}
              </tbody>
            </table>
          </div>
          <Pager page={page} totalPages={data.totalPages} onPage={setPage} />
        </>
      )}
    </div>
  )
}
