import { useEffect, useRef, useState } from 'react'
import { useInView } from 'framer-motion'

function formatValue(value, suffix) {
  return `${value.toLocaleString()}${suffix}`
}

export default function StatCounter({ value, suffix = '', label, durationMs = 1600 }) {
  const target = Number(value)
  const ref = useRef(null)
  const inView = useInView(ref, { once: true, margin: '-40px' })
  const [displayed, setDisplayed] = useState(0)

  useEffect(() => {
    if (!inView || !Number.isFinite(target)) return undefined
    const startedAt = performance.now()
    let frame
    function tick(now) {
      const progress = Math.min((now - startedAt) / durationMs, 1)
      const eased = 1 - Math.pow(1 - progress, 3)
      setDisplayed(Math.round(target * eased))
      if (progress < 1) frame = requestAnimationFrame(tick)
    }
    frame = requestAnimationFrame(tick)
    return () => cancelAnimationFrame(frame)
  }, [inView, target, durationMs])

  return (
    <div ref={ref} className="text-center">
      <p className="font-display text-3xl font-extrabold text-gold-400 sm:text-4xl">
        {Number.isFinite(target) ? formatValue(displayed, suffix) : value}
      </p>
      {label && <p className="mt-1.5 text-sm font-medium text-white/70">{label}</p>}
    </div>
  )
}
