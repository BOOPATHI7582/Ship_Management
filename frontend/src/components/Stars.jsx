export default function Stars({ rating, size = 'text-base' }) {
  const safe = Math.max(0, Math.min(5, Number(rating) || 0))
  return (
    <span className={`${size} tracking-wide text-amber-500`} aria-label={`${safe} out of 5 stars`}>
      {'★'.repeat(safe)}{'☆'.repeat(5 - safe)}
    </span>
  )
}
