import { Link } from 'react-router-dom'

export default function NotFoundPage() {
  return (
    <section className="flex min-h-[60vh] flex-col items-center justify-center bg-navy-950 px-4 text-center text-white">
      <p className="font-display text-7xl font-extrabold text-gold-400">404</p>
      <h1 className="mt-4 font-display text-2xl font-bold">Page not found</h1>
      <p className="mt-2 text-white/60">The page you are looking for does not exist.</p>
      <Link to="/" className="btn-primary mt-8">
        Back to Home
      </Link>
    </section>
  )
}
