import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchManagerEnquiries } from '../../api/manager'
import { Pager, inputCls, secondaryBtnCls } from '../../components/ui/admin'

const statuses = ['NEW', 'REVIEWING', 'CONTACTED', 'NEGOTIATING', 'QUOTATION_SENT', 'APPROVED', 'REJECTED', 'CONVERTED', 'CLOSED']

const statusStyles = {
  NEW: 'bg-blue-100 text-blue-800',
  REVIEWING: 'bg-amber-100 text-amber-800',
  CONTACTED: 'bg-cyan-100 text-cyan-800',
  NEGOTIATING: 'bg-violet-100 text-violet-800',
  QUOTATION_SENT: 'bg-indigo-100 text-indigo-800',
  APPROVED: 'bg-emerald-100 text-emerald-800',
  REJECTED: 'bg-red-100 text-red-700',
  CONVERTED: 'bg-green-100 text-green-800',
  CLOSED: 'bg-navy-100 text-navy-600',
}

export default function EnquiryQueuePage() {
  const [page, setPage] = useState(0)
  const [status, setStatus] = useState('')
  const [search, setSearch] = useState('')
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelled = false
    setError(null)
    fetchManagerEnquiries({ page, size: 10, status: status || undefined, search: search || undefined })
      .then((res) => { if (!cancelled) setData(res.data) })
      .catch((err) => { if (!cancelled) setError(err.response?.data?.message || 'Failed to load enquiries') })
    return () => { cancelled = true }
  }, [page, status, search])

  return (
    <div className="space-y-5">
      <h1 className="font-display text-2xl font-bold text-navy-950">Enquiry Queue</h1>

      <div className="flex flex-wrap items-center gap-3">
        <input
          value={search}
          onChange={(e) => { setPage(0); setSearch(e.target.value) }}
          placeholder="Search reference, cargo or client…"
          className={`${inputCls} max-w-xs`}
        />
        <select value={status} onChange={(e) => { setPage(0); setStatus(e.target.value) }} className={`${inputCls} max-w-52`}>
          <option value="">All statuses</option>
          {statuses.map((s) => <option key={s}>{s}</option>)}
        </select>
      </div>

      {error && <div className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>}

      {data && (
        <>
          <div className="overflow-x-auto rounded-2xl border border-navy-100 bg-white shadow-sm">
            <table className="w-full min-w-max text-left text-sm">
              <thead className="bg-navy-50 text-xs uppercase tracking-wide text-navy-500">
                <tr>
                  <th className="px-5 py-3">Reference</th>
                  <th className="px-5 py-3">Client</th>
                  <th className="px-5 py-3 hidden md:table-cell">Cargo</th>
                  <th className="px-5 py-3 hidden lg:table-cell">Route</th>
                  <th className="px-5 py-3">Status</th>
                  <th className="px-5 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-navy-100">
                {data.content.map((enquiry) => (
                  <tr key={enquiry.id}>
                    <td className="px-5 py-3.5">
                      <Link to={`/manager/enquiries/${enquiry.id}`} className="font-semibold text-navy-950 underline-offset-2 hover:underline">
                        {enquiry.referenceNo}
                      </Link>
                      <div className="text-xs text-navy-400">{new Date(enquiry.createdAt).toLocaleDateString()}</div>
                    </td>
                    <td className="px-5 py-3.5">
                      <div>{enquiry.clientName}</div>
                      <div className="text-xs text-navy-400">{enquiry.companyName || enquiry.clientEmail}</div>
                    </td>
                    <td className="hidden px-5 py-3.5 md:table-cell">
                      <div>{enquiry.cargoType}</div>
                      <div className="text-xs text-navy-400">{enquiry.quantity ? `${enquiry.quantity} ${enquiry.unit}` : ''}</div>
                    </td>
                    <td className="hidden px-5 py-3.5 lg:table-cell">{enquiry.originCountry} → {enquiry.destinationCountry}</td>
                    <td className="px-5 py-3.5">
                      <span className={`rounded-full px-2.5 py-1 text-xs font-bold ${statusStyles[enquiry.status] || 'bg-navy-100 text-navy-700'}`}>
                        {enquiry.status.replace(/_/g, ' ')}
                      </span>
                    </td>
                    <td className="px-5 py-3.5 text-right">
                      <Link to={`/manager/enquiries/${enquiry.id}`} className={secondaryBtnCls + ' inline-block'}>
                        Open
                      </Link>
                    </td>
                  </tr>
                ))}
                {data.content.length === 0 && (
                  <tr><td colSpan={6} className="px-5 py-10 text-center text-navy-400">No enquiries found.</td></tr>
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
