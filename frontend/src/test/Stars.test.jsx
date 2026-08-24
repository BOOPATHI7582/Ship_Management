import { render, screen } from '@testing-library/react'
import Stars from '../components/Stars'

describe('Stars', () => {
  it('renders five glyphs with an accessible label', () => {
    render(<Stars rating={4} />)
    const el = screen.getByLabelText('4 out of 5 stars')
    expect(el).toBeInTheDocument()
    expect(el.textContent).toHaveLength(5)
  })

  it('clamps out-of-range ratings', () => {
    const { rerender } = render(<Stars rating={99} />)
    expect(screen.getByLabelText('5 out of 5 stars')).toBeInTheDocument()
    rerender(<Stars rating={-3} />)
    expect(screen.getByLabelText('0 out of 5 stars')).toBeInTheDocument()
  })

  it('treats missing or non-numeric ratings as zero', () => {
    render(<Stars rating={undefined} />)
    expect(screen.getByLabelText('0 out of 5 stars')).toBeInTheDocument()
  })
})
