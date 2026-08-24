import { render, screen, fireEvent } from '@testing-library/react'
import { EmptyState, ErrorAlert, Skeleton } from '../components/ui/feedback'

describe('EmptyState', () => {
  it('shows title and hint', () => {
    render(<EmptyState title="Nothing here" hint="Try again soon." />)
    expect(screen.getByText('Nothing here')).toBeInTheDocument()
    expect(screen.getByText('Try again soon.')).toBeInTheDocument()
  })

  it('renders action children', () => {
    render(
      <EmptyState title="No invoices">
        <button>Create one</button>
      </EmptyState>,
    )
    expect(screen.getByRole('button', { name: 'Create one' })).toBeInTheDocument()
  })
})

describe('ErrorAlert', () => {
  it('shows the message and retries on click', () => {
    const onRetry = vi.fn()
    render(<ErrorAlert message="Boom" onRetry={onRetry} />)
    expect(screen.getByText('Boom')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Retry' }))
    expect(onRetry).toHaveBeenCalledTimes(1)
  })

  it('hides the retry button when no handler is given', () => {
    render(<ErrorAlert message="Only message" />)
    expect(screen.queryByRole('button')).not.toBeInTheDocument()
  })
})

describe('Skeleton', () => {
  it('renders a decorative pulse block', () => {
    const { container } = render(<Skeleton className="h-4 w-8" />)
    expect(container.firstChild).toHaveClass('animate-pulse')
    expect(container.firstChild).toHaveAttribute('aria-hidden')
  })
})
