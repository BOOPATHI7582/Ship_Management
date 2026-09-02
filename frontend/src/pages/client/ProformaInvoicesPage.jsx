import { useCallback, useEffect, useState } from 'react'
import { Pager, secondaryBtnCls } from '../../components/ui/admin'
import {
  downloadClientProformaPdf,
  fetchMyProformas,
  formatDate,
  money,
  clientProformaPdfUrl,
} from '../../api/proforma'
import { viewPdf } from '../../api/quotations'

const statusBadge = (status) => {
  const map = {
    DRAFT: 'bg-navy-100 text-navy-700',
    SENT: 'bg-blue-100 text-blue-800',
    PAYMENT_PENDING: 'bg-amber-100 text-amber-800',
    ADVANCE_PAID: 'bg-emerald-100 text-emerald-800',
    CONVERTED: 'bg-emerald-600/20 text-emerald-900',
    CANCELLED: 'bg-red-100 text-red-700',
    EXPIRED: 'bg-navy-100 text-navy-500',
  }
  return map[status] || 'bg-navy-100 text-navy-700'
}

export default function ProformaInvoicesPage() {
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)
  const [page, setPage] = useState(0)

  const load = useCallback(() => {
    fetchMyProformas({ page, size: 10 })
      .then((res) => setData(res.data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load proforma invoices'))
  }, [page])

  useEffect(() => { load() }, [load])

  return (
    <div className="space-y-5">
      <div>
        <h1 className="font-display text-2xl font-bold text-navy-950">Proforma Invoices</h1>
        <p className="text-sm text-navy-500">Advance payment documents issued against your accepted quotations.</p>
      </div>

      {error && (
        <div className="flex items-center justify-between rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700">
          <span>{error}</span>
          <button type="button" onClick={() => setError(null)} className="font-bold">×</button>
        </div>
      )}

      {data && (
        <>
          <div className="overflow-x-auto rounded-2xl border border-navy-100 bg-white shadow-sm">
            <table className="w-full text-left text-sm">
              <thead className="bg-navy-50 text-xs uppercase tracking-wide text-navy-500">
                <tr>
                  <th className="px-5 py-3">PI No</th>
                  <th className="px-5 py-3 hidden md:table-cell">Against Quote</th>
                  <th className="px-5 py-3">Issued</th>
                  <th className="px-5 py-3 hidden md:table-cell">Valid Until</th>
                  <th className="px-5 py-3 text-right">Total</th>
                  <th className="px-5 py-3">Status</th>
                  <th className="px-5 py-3 text-right">PDF</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-navy-100">
                {data.content.map((pi) => (
                  <tr key={pi.id}>
                    <td className="px-5 py-3.5 font-semibold text-navy-950">{pi.piNo}</td>
                    <td className="hidden px-5 py-3.5 text-navy-600 md:table-cell">{pi.quoteNo || '—'}</td>
                    <td className="px-5 py-3.5 text-navy-600">{formatDate(pi.issueDate)}</td>
                    <td className="hidden px-5 py-3.5 text-navy-600 md:table-cell">{formatDate(pi.validUntil)}</td>
                    <td className="px-5 py-3.5 text-right font-mono text-navy-900">{money(pi.grandTotal, pi.currency)}</td>
                    <td className="px-5 py-3.5">
                      <span className={`rounded-full px-2.5 py-1 text-xs font-bold ${statusBadge(pi.status)}`}>
                        {pi.status.replace(/_/g, ' ')}
                      </span>
                    </td>
                    <td className="px-5 py-3.5 text-right">
                      {pi.status !== 'DRAFT' ? (
                        <>
                          <button type="button" onClick={() => viewPdf(clientProformaPdfUrl(pi.id))} className={`${secondaryBtnCls} mr-2`}>
                            View
                          </button>
                          <button type="button" onClick={() => downloadClientProformaPdf(pi.id, pi.piNo)} className={secondaryBtnCls}>
                            Download
                          </button>
                        </>
                      ) : (
                        <span className="text-xs text-navy-400">Pending send</span>
                      )}
                    </td>
                  </tr>
                ))}
                {data.content.length === 0 && (
                  <tr><td colSpan={7} className="px-5 py-8 text-center text-navy-400">No proforma invoices yet.</td></tr>
                )}
              </tbody>
            </table>
          </div>
          <Pager page={data.number} totalPages={data.totalPages} onPage={setPage} />
        </>
      )}

      <p className="text-xs text-navy-400">
        Bank remittance details are printed on each document. Payments confirm automatically once verified.
      </p>
    </div>
  )
}
