import { useState } from 'react'
import Reveal from '../../components/Reveal'
import SectionHeading from '../../components/SectionHeading'
import { trackShipment } from '../../api/public'
import { apiErrorMessage } from '../../api/axios'

const STATUS_FLOW = [
  'BOOKING_CONFIRMED',
  'CARGO_PREPARATION',
  'LOADING',
  'LOADING_COMPLETED',
  'DEPARTED',
  'IN_TRANSIT',
  'NEAR_DESTINATION',
  'ARRIVED',
  'UNLOADING',
  'DELIVERED',
  'COMPLETED',
]

const STATUS_LABELS = {
  BOOKING_CONFIRMED: 'Booking Confirmed',
  CARGO_PREPARATION: 'Cargo Preparation',
  LOADING: 'Loading',
  LOADING_COMPLETED: 'Loading Completed',
  DEPARTED: 'Departed',
  IN_TRANSIT: 'In Transit',
  NEAR_DESTINATION: 'Near Destination',
  ARRIVED: 'Arrived',
  UNLOADING: 'Unloading',
  DELIVERED: 'Delivered',
  COMPLETED: 'Completed',
}

function formatDate(value) {
  if (!value) return '—'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleString('en-GB', { day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

export default function TrackingPage() {
  const [shipmentRef, setShipmentRef] = useState('')
  const [result, setResult] = useState(null)
  const [error, setError] = useState('')
  const [searching, setSearching] = useState(false)
  const [searched, setSearched] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSearching(true)
    setSearched(true)
    try {
      const res = await trackShipment(shipmentRef.trim())
      setResult(res.data)
    } catch (err) {
      setResult(null)
      setError(apiErrorMessage(err, 'Unable to track this shipment right now.'))
    } finally {
      setSearching(false)
    }
  }

  const currentIndex = result ? STATUS_FLOW.indexOf(result.status) : -1

  return (
    <>
      <section className="bg-gradient-to-br from-navy-950 via-navy-900 to-navy-700 py-24 text-white">
        <div className="container-page text-center">
          <Reveal>
            <p className="text-xs font-bold uppercase tracking-[0.2em] text-gold-400">Tracking</p>
            <h1 className="mx-auto mt-3 max-w-3xl font-display text-4xl font-extrabold sm:text-5xl">
              Where is my cargo?
            </h1>
            <p className="mx-auto mt-5 max-w-2xl text-lg text-white/70">
              Enter your shipment reference (e.g. SHP-2026-000012) for the latest milestones and ETA.
            </p>
            <form onSubmit={handleSubmit} className="mx-auto mt-8 flex max-w-xl flex-col gap-3 sm:flex-row">
              <input
                type="text"
                required
                value={shipmentRef}
                onChange={(e) => setShipmentRef(e.target.value)}
                placeholder="SHP-2026-000001"
                className="w-full rounded-lg border border-white/15 bg-navy-900 px-5 py-3.5 font-mono text-white placeholder-white/30 focus:border-gold-500 focus:outline-none focus:ring-1 focus:ring-gold-500"
              />
              <button type="submit" disabled={searching} className="btn-primary whitespace-nowrap">
                {searching ? 'Searching…' : 'Track'}
              </button>
            </form>
            {error && searched && !searching && (
              <p className="mx-auto mt-5 max-w-xl rounded-lg border border-red-400/40 bg-red-400/10 px-4 py-2.5 text-sm text-red-300">
                {error}
              </p>
            )}
          </Reveal>
        </div>
      </section>

      {result && (
        <section className="bg-white py-16">
          <div className="container-page max-w-4xl">
            <Reveal>
              <div className="flex flex-wrap items-center justify-between gap-4 rounded-xl bg-navy-950 px-7 py-5 text-white">
                <div>
                  <p className="font-display text-xl font-bold">{result.shipmentRef}</p>
                  <p className="text-sm text-white/60">
                    {result.cargoName ? `${result.cargoName} · ` : ''}
                    {result.quantity?.toLocaleString()} {result.unit || ''}
                  </p>
                </div>
                <span className="rounded-full bg-gold-500 px-4 py-1.5 text-sm font-bold text-navy-950">
                  {STATUS_LABELS[result.status] || result.status}
                </span>
              </div>

              <dl className="mt-6 grid gap-x-10 gap-y-4 rounded-xl border border-navy-100 p-7 sm:grid-cols-2 lg:grid-cols-3">
                {[
                  ['Vessel', result.vesselName || 'To be assigned'],
                  ['Cargo category', result.categoryName || '—'],
                  ['Route', `${result.originPortCode || result.originPortName || '—'} → ${result.destinationPortCode || result.destinationPortName || '—'}`],
                  ['Current location', result.currentLocation || 'Awaiting departure'],
                  ['Loading date', formatDate(result.loadingDate)],
                  ['Estimated arrival', formatDate(result.estimatedArrival)],
                ].map(([label, value]) => (
                  <div key={label}>
                    <dt className="text-xs font-bold uppercase tracking-wider text-navy-400">{label}</dt>
                    <dd className="mt-1 text-sm font-semibold text-navy-950">{value}</dd>
                  </div>
                ))}
              </dl>
            </Reveal>

            <Reveal delay={0.1}>
              <h2 className="mt-12 font-display text-2xl font-bold text-navy-950">Journey timeline</h2>
              <ol className="mt-6 space-y-0 border-l-2 border-dashed border-navy-200 pl-8">
                {STATUS_FLOW.map((status, index) => {
                  const reached = index <= currentIndex
                  const isCurrent = index === currentIndex
                  const event = result.timeline?.find((entry) => entry.status === status)
                  return (
                    <li key={status} className={`relative pb-7 ${reached ? '' : 'opacity-45'}`}>
                      <span className={`absolute -left-[41px] top-0.5 flex h-5 w-5 items-center justify-center rounded-full border-2 ${
                        isCurrent ? 'animate-pulse border-ocean-500 bg-ocean-500' :
                        reached ? 'border-gold-500 bg-gold-500' : 'border-navy-200 bg-white'
                      }`}>
                        {isCurrent && <span className="h-1.5 w-1.5 rounded-full bg-white" />}
                      </span>
                      <p className="text-sm font-bold text-navy-950">{STATUS_LABELS[status]}</p>
                      {event ? (
                        <>
                          <p className="mt-0.5 text-xs font-semibold text-navy-500">
                            {event.locationLabel ? `${event.locationLabel} · ` : ''}{formatDate(event.occurredAt)}
                          </p>
                          {event.notes && <p className="mt-1 text-xs leading-relaxed text-navy-500">{event.notes}</p>}
                        </>
                      ) : (
                        <p className="mt-0.5 text-xs text-navy-400">{isCurrent ? 'Current status' : 'Pending'}</p>
                      )}
                    </li>
                  )
                })}
              </ol>
            </Reveal>

            {(result.currentLatitude != null && result.currentLongitude != null) && (
              <Reveal delay={0.15}>
                <div className="mt-4 overflow-hidden rounded-xl border border-navy-100">
                  <iframe
                    title="Vessel position map"
                    width="100%"
                    height="320"
                    loading="lazy"
                    src={`https://www.openstreetmap.org/export/embed.html?bbox=${result.currentLongitude - 3},${result.currentLatitude - 2},${Number(result.currentLongitude) + 3},${Number(result.currentLatitude) + 2}&layer=mapnik&marker=${result.currentLatitude},${result.currentLongitude}`}
                  />
                </div>
                <p className="mt-2 text-center text-xs text-navy-400">
                  Approximate position — updates are manual checkpoints.
                </p>
              </Reveal>
            )}
          </div>
        </section>
      )}

      {!result && (
        <section className="bg-navy-50 py-20">
          <div className="container-page max-w-2xl text-center">
            <SectionHeading
              eyebrow="How it works"
              title="Checkpoint-based visibility"
              subtitle="Our ship managers log every milestone manually — booking, loading, departure, transit waypoints and arrival — so you always know where your cargo stands without sensitive operational detail."
            />
          </div>
        </section>
      )}
    </>
  )
}
