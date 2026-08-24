import { motion } from 'framer-motion'

export function Skeleton({ className = '' }) {
  return <div aria-hidden className={`animate-pulse rounded-lg bg-navy-100 ${className}`} />
}

export function TableSkeleton({ rows = 6 }) {
  return (
    <div className="divide-y divide-navy-50 overflow-hidden rounded-xl border border-navy-100 bg-white" role="status" aria-label="Loading">
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="flex items-center gap-4 px-5 py-4">
          <Skeleton className="h-3 w-24" />
          <Skeleton className="h-3 flex-1" />
          <Skeleton className="hidden h-3 w-32 sm:block" />
          <Skeleton className="h-5 w-16 rounded-full" />
        </div>
      ))}
    </div>
  )
}

export function CardsSkeleton({ count = 4, className = 'grid grid-cols-2 gap-4 xl:grid-cols-4' }) {
  return (
    <div className={className} role="status" aria-label="Loading">
      {Array.from({ length: count }).map((_, i) => (
        <div key={i} className="rounded-xl border border-navy-100 bg-white p-4 shadow-sm">
          <Skeleton className="h-2.5 w-20" />
          <Skeleton className="mt-2 h-6 w-28" />
          <Skeleton className="mt-1 h-2.5 w-24" />
        </div>
      ))}
    </div>
  )
}

export function PageLoading() {
  return (
    <div className="space-y-6" role="status" aria-label="Loading">
      <CardsSkeleton count={4} />
      <div className="rounded-xl border border-navy-100 bg-white p-5 shadow-sm">
        <Skeleton className="h-40 w-full" />
      </div>
    </div>
  )
}

const EMPTY_ICONS = {
  inbox: 'M3 14l3-9h12l3 9v5a1 1 0 01-1 1H4a1 1 0 01-1-1v-5zm3 0h3a3 3 0 006 0h3',
  search: 'M21 21l-5.2-5.2m2.2-5.3a7.5 7.5 0 11-15 0 7.5 7.5 0 0115 0z',
  doc: 'M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.6L19 9.4V19a2 2 0 01-2 2z',
  invoice: 'M9 14h6m-6 4h6M7 3h10a1 1 0 011 1v17l-3-2-3 2-3-2-3 2V4a1 1 0 011-1z',
}

export function EmptyState({ icon = 'inbox', title, hint, children }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.25 }}
      className="flex flex-col items-center justify-center rounded-xl border border-dashed border-navy-200 bg-navy-50/40 px-6 py-12 text-center"
    >
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5"
        className="mb-3 h-10 w-10 text-navy-300">
        <path strokeLinecap="round" strokeLinejoin="round" d={EMPTY_ICONS[icon] || EMPTY_ICONS.inbox} />
      </svg>
      <p className="font-display text-sm font-semibold text-navy-700">{title}</p>
      {hint && <p className="mt-1 max-w-sm text-xs text-navy-400">{hint}</p>}
      {children && <div className="mt-4">{children}</div>}
    </motion.div>
  )
}

export function ErrorAlert({ message, onRetry }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: -6 }}
      animate={{ opacity: 1, y: 0 }}
      role="alert"
      className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-red-100 bg-red-50 px-4 py-3 text-sm text-red-700"
    >
      <span>{message}</span>
      {onRetry && (
        <button type="button" onClick={onRetry}
          className="rounded-lg bg-red-600 px-3 py-1.5 text-xs font-semibold text-white transition hover:bg-red-700">
          Retry
        </button>
      )}
    </motion.div>
  )
}
