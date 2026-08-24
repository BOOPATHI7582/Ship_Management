import { describe, it, expect, vi } from 'vitest'
import { money } from '../api/invoices'

vi.mock('../api/axios', () => ({ default: { get: vi.fn(), post: vi.fn() } }))
vi.mock('../api/quotations', () => ({ downloadPdf: vi.fn() }))

describe('money', () => {
  it('formats amounts as INR with two decimals', () => {
    expect(money('1234.5')).toBe('INR 1,234.50')
  })

  it('defaults null or undefined to zero', () => {
    expect(money(null)).toBe('INR 0.00')
    expect(money(undefined)).toBe('INR 0.00')
  })

  it('honours an explicit currency', () => {
    expect(money(10, 'USD')).toContain('USD')
  })
})
