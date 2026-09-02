import { useCallback, useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { fetchMyEnquiries } from '../../api/client'
import { Pager } from '../../components/ui/admin'

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

export default function MyEnquiriesPage() {
  const location = useLocation()
  const [page, setPage] = useState(0)
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)

  const load = useCallback(() => {
    setError(null)
    fetchMyEnquiries(page, 10)
      .then((res) => setData(res.data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load enquiries'))
  }, [page])

  useEffect(() => { load() }, [load])

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <h1 className="font-display text-2xl font-bold text-navy-950">My Enquiries</h1>
        <Link
          to="/client/enquiries/new"
          className="rounded-lg bg-gold-500 px-5 py-2.5 text-sm font-bold text-navy-950 transition hover:bg-gold-400"
        >
          New Enquiry
        </Link>
      </div>

      {location.state?.created && (
        <p className="rounded-lg bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-800">
          Enquiry submitted. Our operations team will review it shortly.
        </p>
      )}

      {error && <div className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>}

      {data && data.content.length === 0 && (
        <div className="rounded-2xl border border-dashed border-navy-200 bg-white p-12 text-center">
          <p className="font-display text-lg font-bold text-navy-950">No enquiries yet</p>
          <p className="mt-2 text-sm text-navy-500">
            Submit your first export requirement and we will get back with a quotation.
          </p>
          <Link
            to="/client/enquiries/new"
            className="mt-6 inline-block rounded-lg bg-navy-950 px-6 py-2.5 text-sm font-bold text-white transition hover:bg-navy-900"
          >
            Submit Enquiry
          </Link>
        </div>
      )}

      {data && data.content.length > 0 && (
        <>
          <div className="overflow-x-auto rounded-2xl border border-navy-100 bg-white shadow-sm">
            <table className="w-full text-left text-sm">
              <thead className="bg-navy-50 text-xs uppercase tracking-wide text-navy-500">
                <tr>
                  <th className="px-5 py-3">Reference</th>
                  <th className="px-5 py-3">Cargo</th>
                  <th className="px-5 py-3 hidden md:table-cell">Route</th>
                  <th className="px-5 py-3">Qty</th>
                  <th className="px-5 py-3">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-navy-100">
                {data.content.map((enquiry) => (
                  <tr key={enquiry.id} className="transition hover:bg-navy-50/60">
                    <td className="px-5 py-3.5">
                      <Link
                        to={`/client/enquiries/${enquiry.id}`}
                        className="font-semibold text-navy-950 underline-offset-2 hover:underline"
                      >
                        {enquiry.referenceNo}
                      </Link>
                      <div className="text-xs text-navy-400">
                        {new Date(enquiry.createdAt).toLocaleDateString()}
                      </div>
                    </td>
                    <td className="px-5 py-3.5">{enquiry.cargoType}</td>
                    <td className="hidden px-5 py-3.5 text-navy-600 md:table-cell">
                      {enquiry.originCountry} → {enquiry.destinationCountry}
                    </td>
                    <td className="px-5 py-3.5">{enquiry.quantity} {enquiry.unit}</td>
                    <td className="px-5 py-3.5">
                      <span
                        className={`rounded-full px-2.5 py-1 text-xs font-bold ${
                          statusStyles[enquiry.status] || 'bg-navy-100 text-navy-700'
                        }`}
                      >
                        {(enquiry.status || '').replace(/_/g, ' ')}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <Pager page={page} totalPages={data.totalPages} onPage={setPage} />
        </>
      )}
    </div>
  )
}
