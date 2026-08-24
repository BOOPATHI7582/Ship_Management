import { useEffect } from 'react'
import { motion } from 'framer-motion'

export const inputCls =
  'w-full rounded-lg border border-navy-200 bg-white px-3.5 py-2.5 text-sm text-navy-900 outline-none transition focus:border-gold-500 focus:ring-2 focus:ring-gold-500/30'

export const labelCls = 'mb-1.5 block text-xs font-semibold uppercase tracking-wide text-navy-500'

export const primaryBtnCls =
  'rounded-lg bg-navy-950 px-5 py-2.5 text-sm font-bold text-white transition hover:bg-navy-900 disabled:opacity-50'

export const secondaryBtnCls =
  'rounded-lg border border-navy-200 px-4 py-2 text-sm font-semibold text-navy-700 transition hover:bg-navy-50 disabled:opacity-40'

export const dangerBtnCls =
  'rounded-lg border border-red-200 bg-red-50 px-4 py-2 text-sm font-semibold text-red-700 transition hover:bg-red-100 disabled:opacity-40'

export function Field({ label, error, children }) {
  return (
    <label className="block">
      <span className={labelCls}>{label}</span>
      {children}
      {error && <span className="mt-1 block text-xs font-medium text-red-600">{error}</span>}
    </label>
  )
}

export function Modal({ title, onClose, children, wide = false }) {
  useEffect(() => {
    function onKey(e) {
      if (e.key === 'Escape') onClose()
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [onClose])

  return (
    <div
      className="fixed inset-0 z-50 flex items-start justify-center overflow-y-auto bg-navy-950/50 p-4 py-10 backdrop-blur-sm"
      onMouseDown={(e) => e.target === e.currentTarget && onClose()}
    >
      <motion.div
        initial={{ opacity: 0, y: 14, scale: 0.98 }}
        animate={{ opacity: 1, y: 0, scale: 1 }}
        transition={{ duration: 0.18, ease: 'easeOut' }}
        className={`w-full ${wide ? 'max-w-3xl' : 'max-w-xl'} rounded-2xl bg-white p-6 shadow-xl`}
      >
        <div className="mb-4 flex items-center justify-between">
          <h2 className="font-display text-lg font-bold text-navy-950">{title}</h2>
          <button
            type="button"
            onClick={onClose}
            className="rounded-lg px-2.5 py-1 text-lg leading-none text-navy-400 transition hover:bg-navy-50 hover:text-navy-700"
            aria-label="Close"
          >
            ×
          </button>
        </div>
        {children}
      </motion.div>
    </div>
  )
}

export function Pager({ page, totalPages, onPage }) {
  if (!totalPages || totalPages <= 1) return null
  return (
    <div className="flex items-center justify-between pt-2 text-sm">
      <button type="button" disabled={page === 0} onClick={() => onPage(page - 1)} className={secondaryBtnCls}>
        Previous
      </button>
      <span className="text-navy-500">Page {page + 1} of {totalPages}</span>
      <button
        type="button"
        disabled={page + 1 >= totalPages}
        onClick={() => onPage(page + 1)}
        className={secondaryBtnCls}
      >
        Next
      </button>
    </div>
  )
}
