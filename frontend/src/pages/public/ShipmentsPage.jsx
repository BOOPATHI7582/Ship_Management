import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import Reveal from '../../components/Reveal'
import SectionHeading from '../../components/SectionHeading'
import { fetchAvailableCargo } from '../../api/public'

function formatMoney(value, currency) {
  if (value == null) return 'On request'
  try {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency, maximumFractionDigits: 0 }).format(value)
  } catch {
    return `${currency} ${value}`
  }
}

function formatDate(value) {
  if (!value) return 'Flexible'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleDateString('en-GB', { day: 'numeric', month: 'long', year: 'numeric' })
}

export default function ShipmentsPage() {
  const [cargo, setCargo] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let cancelled = false
    fetchAvailableCargo()
      .then((res) => {
        if (!cancelled) setCargo(res.data || [])
      })
      .catch(() => {
        if (!cancelled) setCargo([])
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [])

  return (
    <>
      <section className="bg-gradient-to-br from-navy-950 via-navy-900 to-navy-700 py-24 text-white">
        <div className="container-page text-center">
          <Reveal>
            <p className="text-xs font-bold uppercase tracking-[0.2em] text-gold-400">Available Shipments</p>
            <h1 className="mx-auto mt-3 max-w-3xl font-display text-4xl font-extrabold sm:text-5xl">
              Open cargo lots
            </h1>
            <p className="mx-auto mt-5 max-w-2xl text-lg text-white/70">
              Non-sensitive listings only — indicative prices per metric tonne, updated by our desk.
              Request a quote to receive firm pricing.
            </p>
          </Reveal>
        </div>
      </section>

      <section className="bg-white py-20">
        <div className="container-page">
          {loading && (
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
              {Array.from({ length: 6 }).map((_, index) => (
                <div key={index} className="h-72 animate-pulse rounded-xl bg-navy-100" />
              ))}
            </div>
          )}

          {!loading && cargo.length === 0 && (
            <div className="mx-auto max-w-2xl rounded-xl border border-dashed border-navy-200 bg-navy-50 p-12 text-center">
              <h2 className="font-display text-xl font-bold text-navy-950">No open lots right now</h2>
              <p className="mt-2 text-sm text-navy-500">
                Cargo lots appear here the moment our desk lists them. Register and we will notify you first.
              </p>
              <Link to="/register" className="btn-primary mt-6">Create Free Account</Link>
            </div>
          )}

          {!loading && cargo.length > 0 && (
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
              {cargo.map((lot, index) => (
                <Reveal key={lot.id} delay={(index % 3) * 0.07}>
                  <article className="flex h-full flex-col rounded-xl border border-navy-100 bg-white shadow-sm transition hover:-translate-y-1 hover:border-gold-500/50 hover:shadow-md">
                    <div className="rounded-t-xl bg-gradient-to-r from-navy-900 to-navy-700 px-6 py-4">
                      <p className="text-xs font-bold uppercase tracking-widest text-gold-400">{lot.categoryName || 'Cargo'}</p>
                      <h2 className="mt-1 font-display text-lg font-bold text-white">{lot.name}</h2>
                    </div>
                    <dl className="flex-1 space-y-2.5 px-6 py-5 text-sm">
                      {[
                        ['Quantity', `${lot.quantity?.toLocaleString() ?? '—'} ${lot.unit || 'MT'}`],
                        ['Route', `${lot.loadingPortName || lot.originCountry || '—'} → ${lot.destinationPortName || lot.destinationCountry || '—'}`],
                        ['Loading window', formatDate(lot.loadingDate)],
                        ['ETA destination', formatDate(lot.estimatedArrival)],
                      ].map(([label, value]) => (
                        <div key={label} className="flex items-start justify-between gap-4 border-b border-navy-50 pb-2.5 last:border-none">
                          <dt className="text-navy-400">{label}</dt>
                          <dd className="text-right font-semibold text-navy-900">{value}</dd>
                        </div>
                      ))}
                    </dl>
                    <div className="flex items-center justify-between gap-4 border-t border-navy-100 px-6 py-4">
                      <p className="font-display text-xl font-extrabold text-navy-950">
                        {formatMoney(lot.indicativePrice, lot.currency)}
                        <span className="ml-1 text-xs font-medium text-navy-400">/ {lot.unit || 'MT'}</span>
                      </p>
                      <span className="inline-flex items-center gap-1.5 rounded-full bg-emerald-50 px-3 py-1 text-xs font-bold text-emerald-600">
                        <span className="h-1.5 w-1.5 animate-pulse rounded-full bg-emerald-500" /> Available
                      </span>
                    </div>
                    <Link to={`/register?enquiry=${lot.id}`} className="btn-primary mx-6 mb-6 mt-0 w-[calc(100%-3rem)]">Request Quote</Link>
                  </article>
                </Reveal>
              ))}
            </div>
          )}
        </div>
      </section>
    </>
  )
}
