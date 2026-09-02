import { useCallback, useEffect, useState } from 'react'
import { Pager, secondaryBtnCls } from '../../components/ui/admin'
import { downloadClientReceiptPdf, fetchMyReceipts, methodBadge, clientReceiptPdfUrl } from '../../api/receipts'
import { viewPdf } from '../../api/quotations'
import { formatDate, money } from '../../api/invoices'

export default function ReceiptsPage() {
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)
  const [page, setPage] = useState(0)

  const load = useCallback(() => {
    setError(null)
    fetchMyReceipts({ page, size: 10 })
      .then((res) => setData(res.data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load receipts'))
  }, [page])

  useEffect(() => { load() }, [load])

  return (
    <div className="space-y-5">
      <div>
        <h1 className="font-display text-2xl font-bold text-navy-950">Payment Receipts</h1>
        <p className="text-sm text-navy-500">Official confirmation for every payment you have made.</p>
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
                  <th className="px-5 py-3">Receipt No</th>
                  <th className="px-5 py-3 hidden md:table-cell">Against Invoice</th>
                  <th className="px-5 py-3 hidden lg:table-cell">Issued</th>
                  <th className="px-5 py-3 hidden sm:table-cell">Method</th>
                  <th className="px-5 py-3 text-right">Amount</th>
                  <th className="px-5 py-3 text-right hidden md:table-cell">Balance Left</th>
                  <th className="px-5 py-3 text-right">PDF</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-navy-100">
                {data.content.map((r) => (
                  <tr key={r.id}>
                    <td className="px-5 py-3.5 font-semibold text-navy-950">{r.receiptNo}</td>
                    <td className="hidden px-5 py-3.5 text-navy-600 md:table-cell">{r.invoiceNo || '—'}</td>
                    <td className="hidden px-5 py-3.5 text-navy-600 lg:table-cell">{formatDate(r.issuedOn)}</td>
                    <td className="hidden px-5 py-3.5 sm:table-cell">
                      <span className={`rounded-full px-2.5 py-1 text-xs font-bold ${methodBadge(r.method)}`}>
                        {(r.method || '—').replace(/_/g, ' ')}
                      </span>
                    </td>
                    <td className="px-5 py-3.5 text-right font-mono font-semibold text-emerald-700">
                      {money(r.amount, r.currency)}
                    </td>
                    <td className="hidden px-5 py-3.5 text-right font-mono text-navy-700 md:table-cell">
                      {money(r.remainingBalance, r.currency)}
                    </td>
                    <td className="px-5 py-3.5 text-right">
                      <button type="button" onClick={() => viewPdf(clientReceiptPdfUrl(r.id))} className={`${secondaryBtnCls} mr-2`}>
                        View
                      </button>
                      <button type="button" onClick={() => downloadClientReceiptPdf(r.id, r.receiptNo)} className={secondaryBtnCls}>
                        Download
                      </button>
                    </td>
                  </tr>
                ))}
                {data.content.length === 0 && (
                  <tr><td colSpan={7} className="px-5 py-8 text-center text-navy-400">
                    No receipts yet — they appear here automatically once a payment is verified.
                  </td></tr>
                )}
              </tbody>
            </table>
          </div>
          <Pager page={data.number} totalPages={data.totalPages} onPage={setPage} />
        </>
      )}
    </div>
  )
}
