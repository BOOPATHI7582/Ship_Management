import { useEffect, useRef } from 'react'

export default function OtpInput({ length = 6, value, onChange, disabled = false }) {
  const refs = useRef([])
  const digits = Array.from({ length }, (_, i) => value[i] || '')

  useEffect(() => {
    if (value.length === length) refs.current[length - 1]?.blur()
  }, [value, length])

  function handleChange(i, e) {
    const val = e.target.value.replace(/\D/g, '')
    const next = value.slice(0, i) + val + value.slice(i + val.length)
    onChange(next.slice(0, length))
    const focusIndex = Math.min(i + Math.max(val.length, 1), length - 1)
    refs.current[focusIndex]?.focus()
  }

  function handleKeyDown(i, e) {
    if (e.key === 'Backspace' && !value[i] && i > 0) {
      e.preventDefault()
      refs.current[i - 1]?.focus()
    }
  }

  function handlePaste(e) {
    const pasted = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, length)
    if (pasted) {
      e.preventDefault()
      onChange(pasted)
      refs.current[Math.min(pasted.length, length) - 1]?.focus()
    }
  }

  return (
    <div className="flex justify-center gap-2" onPaste={handlePaste}>
      {digits.map((d, i) => (
        <input
          key={i}
          ref={(el) => {
            refs.current[i] = el
          }}
          data-index={i}
          type="text"
          inputMode="numeric"
          autoComplete="one-time-code"
          maxLength={length}
          value={d}
          disabled={disabled}
          onChange={(e) => handleChange(i, e)}
          onKeyDown={(e) => handleKeyDown(i, e)}
          aria-label={`Code digit ${i + 1}`}
          className="otp-box"
        />
      ))}
    </div>
  )
}