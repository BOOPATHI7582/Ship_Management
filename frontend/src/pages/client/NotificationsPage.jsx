import { useEffect, useState } from 'react'
import { markNotificationRead, fetchNotifications } from '../../api/client'

export default function NotificationsPage() {
  const [page, setPage] = useState(0)
  const [data, setData] = useState(null)
  const [error, setError] = useState(null)

  function load(targetPage) {
    fetchNotifications(targetPage, 10)
      .then((res) => setData(res.data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load notifications'))
  }

  useEffect(() => {
    setError(null)
    load(page)
  }, [page])

  async function handleMarkRead(id) {
    try {
      await markNotificationRead(id)
      load(page)
    } catch {
      /* keep silent, list refreshes next visit */
    }
  }

  return (
    <div className="space-y-6">
      <h1 className="font-display text-2xl font-bold text-navy-950">Notifications</h1>

      {error && <div className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>}

      {data && data.content.length === 0 && (
        <div className="rounded-2xl border border-dashed border-navy-200 bg-white p-12 text-center">
          <p className="font-display text-lg font-bold text-navy-950">Nothing here yet</p>
          <p className="mt-2 text-sm text-navy-500">
            Updates about your enquiries and shipments will appear here.
          </p>
        </div>
      )}

      {data && data.content.length > 0 && (
        <ul className="space-y-3">
          {data.content.map((notification) => (
            <li
              key={notification.id}
              className={`rounded-2xl border bg-white p-5 shadow-sm ${
                notification.read ? 'border-navy-100' : 'border-gold-300'
              }`}
            >
              <div className="flex items-start justify-between gap-4">
                <div>
                  <p className="text-sm font-bold text-navy-950">{notification.title}</p>
                  <p className="mt-1 text-sm text-navy-600">{notification.message}</p>
                  <p className="mt-2 text-xs text-navy-400">
                    {new Date(notification.createdAt).toLocaleString()}
                    {!notification.read && (
                      <span className="ml-2 rounded-full bg-gold-500/20 px-2 py-0.5 font-bold text-navy-800">
                        NEW
                      </span>
                    )}
                  </p>
                </div>
                {!notification.read && (
                  <button
                    type="button"
                    onClick={() => handleMarkRead(notification.id)}
                    className="shrink-0 rounded-lg border border-navy-200 px-3 py-1.5 text-xs font-semibold text-navy-700 transition hover:bg-navy-50"
                  >
                    Mark read
                  </button>
                )}
              </div>
            </li>
          ))}
        </ul>
      )}

      {data && data.totalPages > 1 && (
        <div className="flex items-center justify-between text-sm">
          <button
            type="button"
            disabled={page === 0}
            onClick={() => setPage((p) => p - 1)}
            className="rounded-lg border border-navy-200 px-4 py-2 font-semibold disabled:opacity-40"
          >
            Previous
          </button>
          <span className="text-navy-500">Page {page + 1} of {data.totalPages}</span>
          <button
            type="button"
            disabled={page + 1 >= data.totalPages}
            onClick={() => setPage((p) => p + 1)}
            className="rounded-lg border border-navy-200 px-4 py-2 font-semibold disabled:opacity-40"
          >
            Next
          </button>
        </div>
      )}
    </div>
  )
}
