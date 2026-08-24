import { Component } from 'react'
import { Link } from 'react-router-dom'

/** Full-app crash guard: renders a readable message instead of a blank page. */
export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props)
    this.state = { error: null }
  }

  static getDerivedStateFromError(error) {
    return { error }
  }

  componentDidCatch(error, info) {
    console.error('Unhandled UI error:', error, info?.componentStack)
  }

  render() {
    if (this.state.error) {
      return (
        <div className="flex min-h-screen items-center justify-center bg-navy-50 p-6">
          <div className="w-full max-w-lg rounded-2xl border border-navy-100 bg-white p-8 text-center shadow-sm">
            <h1 className="font-display text-xl font-bold text-navy-950">Something went wrong</h1>
            <p className="mt-2 text-sm text-navy-500">
              An unexpected error occurred while rendering this page. Please try again.
            </p>
            {this.state.error?.message && (
              <pre className="mt-4 overflow-x-auto rounded-lg bg-navy-50 p-3 text-left text-xs text-red-700">
                {String(this.state.error.message)}
              </pre>
            )}
            <button
              type="button"
              onClick={() => window.location.reload()}
              className="mt-5 rounded-lg bg-navy-950 px-6 py-2.5 text-sm font-bold text-white transition hover:bg-navy-900"
            >
              Reload
            </button>
            <Link to="/" className="ml-3 text-sm font-semibold text-navy-500 underline-offset-2 hover:underline">
              Back to home
            </Link>
          </div>
        </div>
      )
    }
    return this.props.children
  }
}
