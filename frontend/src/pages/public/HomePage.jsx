import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import Reveal from '../../components/Reveal'
import SectionHeading from '../../components/SectionHeading'
import StatCounter from '../../components/StatCounter'
import { fetchAvailableCargo, fetchCargoCategories, fetchReviews, fetchStats } from '../../api/public'

const services = [
  {
    title: 'Bulk Cargo Export',
    description: 'Iron ore, coal, steel, cement and minerals shipped in full vessel loads with expert handling.',
    icon: 'M3 7h13v10H3zM16 10h3l2 3v4h-5zM7 19a2 2 0 104 0 2 2 0 00-4 0zm9 0a2 2 0 104 0 2 2 0 00-4 0z',
  },
  {
    title: 'Agri Commodities',
    description: 'Rice, wheat and agricultural products exported with quality certification and fumigation support.',
    icon: 'M12 3v18M5 8c2 3 12 3 14 0M5 14c2 3 12 3 14 0',
  },
  {
    title: 'Container Solutions',
    description: 'FCL and LCL container cargo with port-to-port coordination and customs documentation.',
    icon: 'M21 8l-9-5-9 5v8l9 5 9-5V8zM3.5 8.5L12 13m0 0l8.5-4.5M12 13v8',
  },
  {
    title: 'Quotation & Negotiation',
    description: 'Transparent indicative pricing, direct negotiation threads and instant quotation PDFs.',
    icon: 'M9 12h6m-3-6v12m8-6a8 8 0 11-16 0 8 8 0 0116 0z',
  },
  {
    title: 'Live Shipment Tracking',
    description: 'Manual checkpoint tracking from booking to delivery with location updates and ETA.',
    icon: 'M12 21s7-5.1 7-11a7 7 0 10-14 0c0 5.9 7 11 7 11zm0-8.5a2.5 2.5 0 100-5 2.5 2.5 0 000 5z',
  },
  {
    title: 'Billing & Payments',
    description: 'Proforma invoices, tax invoices, secure online payments, receipts and final bills.',
    icon: 'M9 14l2 2 4-5m5 3a8 8 0 11-16 0 8 8 0 0116 0z',
  },
]

const steps = [
  { title: 'Submit Enquiry', text: 'Tell us your cargo, quantity and route through a guided requirement form.' },
  { title: 'Negotiate', text: 'Chat directly with our export desk and refine scope, price and schedule.' },
  { title: 'Accept Quotation', text: 'Receive a formal quotation with tax breakdown and accept online.' },
  { title: 'Pay Proforma', text: 'Pay securely via Razorpay or bank transfer against the proforma invoice.' },
  { title: 'We Ship', text: 'Cargo is booked on a vetted vessel with loading supervision and documents.' },
  { title: 'Track & Receive', text: 'Follow every checkpoint until delivery, receipts and final bill.' },
]

const whyUs = [
  { title: 'Global Port Network', text: 'Active corridors across Asia, Middle East, Africa, Europe and the Americas.' },
  { title: 'Compliance First', text: 'GST-ready tax invoices and complete export documentation handled in-house.' },
  { title: 'Secure Payments', text: 'Escrow-style milestones with verified payment gateways and audit trails.' },
  { title: 'Dedicated Managers', text: 'One accountable ship manager per shipment from enquiry to final bill.' },
]

const fallbackReviews = [
  { id: 'f1', rating: 5, title: 'On-time, every time', reviewText: 'Our steel consignments reached Jebel Ali ahead of schedule with flawless paperwork.', clientName: 'Rohit Malhotra', companyName: 'Bharat Steel Traders' },
  { id: 'f2', rating: 5, title: 'Transparent pricing', reviewText: 'The negotiation thread and itemised quotation removed all ambiguity from our first bulk deal.', clientName: 'Amelia Clarke', companyName: 'Clarke Agri Imports' },
  { id: 'f3', rating: 4, title: 'Great tracking visibility', reviewText: 'Checkpoint updates meant our warehouse team always knew the ETA within hours.', clientName: 'Yusuf Rahman', companyName: 'Gulf Minerals Co.' },
]

function formatMoney(value, currency) {
  if (value == null) return null
  try {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency, maximumFractionDigits: 0 }).format(value)
  } catch {
    return `${currency} ${value}`
  }
}

export default function HomePage() {
  const [stats, setStats] = useState(null)
  const [categories, setCategories] = useState([])
  const [cargo, setCargo] = useState([])
  const [reviews, setReviews] = useState(fallbackReviews)

  useEffect(() => {
    let cancelled = false
    async function load() {
      const safe = (promise) => promise.catch(() => null)
      const [statsRes, categoriesRes, cargoRes, reviewsRes] = await Promise.all([
        safe(fetchStats()),
        safe(fetchCargoCategories()),
        safe(fetchAvailableCargo()),
        safe(fetchReviews()),
      ])
      if (cancelled) return
      if (statsRes?.data) setStats(statsRes.data)
      if (categoriesRes?.data) setCategories(categoriesRes.data.slice(0, 8))
      if (cargoRes?.data) setCargo(cargoRes.data.slice(0, 3))
      if (reviewsRes?.data?.length) setReviews(reviewsRes.data.slice(0, 3))
    }
    load()
    return () => {
      cancelled = true
    }
  }, [])

  return (
    <>
      {/* 1. HERO */}
      <section className="relative overflow-hidden bg-gradient-to-br from-navy-950 via-navy-900 to-navy-700 text-white">
        <div className="absolute inset-0 opacity-20 [background-image:radial-gradient(circle_at_30%_20%,#38bdf8_0,transparent_45%),radial-gradient(circle_at_75%_60%,#c9a22755_0,transparent_40%)]" />
        <svg className="absolute bottom-0 left-0 h-24 w-full text-navy-950/60" viewBox="0 0 1440 120" preserveAspectRatio="none" aria-hidden="true">
          <path fill="currentColor" d="M0 64l120-16 132 24 156-32 168 28 144-20 180 26 148-22 192 30V120H0z" />
        </svg>
        <div className="container-page relative flex min-h-[82vh] flex-col items-center justify-center py-24 text-center">
          <motion.p
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
            className="rounded-full border border-gold-500/40 bg-white/5 px-4 py-1.5 text-xs font-semibold uppercase tracking-[0.2em] text-gold-400"
          >
            International Export &amp; Shipping
          </motion.p>
          <motion.h1
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.1 }}
            className="mt-6 max-w-4xl font-display text-4xl font-extrabold leading-tight sm:text-5xl lg:text-6xl"
          >
            Connecting Global Trade Through{' '}
            <span className="text-gold-400">Reliable Export Solutions</span>
          </motion.h1>
          <motion.p
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.2 }}
            className="mt-6 max-w-2xl text-lg text-white/70"
          >
            From enquiry to final bill — quotations, tax invoices, secure payments and live shipment
            tracking for bulk cargo across the world&apos;s major ports.
          </motion.p>
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.3 }}
            className="mt-10 flex flex-col gap-4 sm:flex-row"
          >
            <Link to="/register" className="btn-primary">
              Request Export Quote
            </Link>
            <Link to="/shipments" className="btn-secondary">
              Explore Shipments
            </Link>
          </motion.div>
        </div>
      </section>

      {/* 2. ABOUT US */}
      <section className="bg-white py-20">
        <div className="container-page grid items-center gap-12 lg:grid-cols-2">
          <Reveal>
            <SectionHeading
              center={false}
              eyebrow="About Us"
              title="A modern export house built on maritime heritage"
              subtitle="ExportPlatform combines decades of shipping expertise with a digital-first platform — so importers everywhere can buy bulk commodities from India with total confidence."
            />
            <ul className="mt-8 space-y-4">
              {[
                'Direct access to verified vessels and port agents',
                'End-to-end documentation: quotations to tax invoices',
                'Real-time negotiation and milestone-based payments',
              ].map((item) => (
                <li key={item} className="flex items-start gap-3">
                  <span className="mt-1 flex h-5 w-5 flex-none items-center justify-center rounded-full bg-gold-500/15">
                    <svg className="h-3 w-3 text-gold-600" viewBox="0 0 20 20" fill="currentColor"><path fillRule="evenodd" d="M16.7 5.3a1 1 0 010 1.4l-8 8a1 1 0 01-1.4 0l-4-4a1 1 0 111.4-1.4L8 12.6l7.3-7.3a1 1 0 011.4 0z" clipRule="evenodd" /></svg>
                  </span>
                  <span className="text-navy-700">{item}</span>
                </li>
              ))}
            </ul>
          </Reveal>
          <Reveal delay={0.15} className="relative">
            <div className="rounded-2xl bg-gradient-to-br from-navy-900 to-navy-700 p-10 text-white shadow-xl">
              <p className="font-display text-lg font-bold text-gold-400">Since inception</p>
              <div className="mt-6 grid grid-cols-2 gap-8">
                {[['25+', 'Years in trade'], ['6', 'Continents served'], ['100+', 'Port pairs'], ['100%', 'Documentation compliance']].map(([value, label]) => (
                  <div key={label}>
                    <p className="font-display text-3xl font-extrabold">{value}</p>
                    <p className="mt-1 text-sm text-white/60">{label}</p>
                  </div>
                ))}
              </div>
            </div>
          </Reveal>
        </div>
      </section>

      {/* 3. EXPORT SERVICES */}
      <section className="bg-navy-50 py-20">
        <div className="container-page">
          <Reveal>
            <SectionHeading eyebrow="What We Do" title="Export Services" subtitle="One platform for the entire lifecycle of an international bulk trade." />
          </Reveal>
          <div className="mt-12 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {services.map((service, index) => (
              <Reveal key={service.title} delay={index * 0.06}>
                <div className="group h-full rounded-xl border border-navy-100 bg-white p-7 shadow-sm transition hover:-translate-y-1 hover:border-gold-500/50 hover:shadow-md">
                  <span className="flex h-12 w-12 items-center justify-center rounded-lg bg-navy-900 transition group-hover:bg-gold-500">
                    <svg className="h-6 w-6 text-gold-400 transition group-hover:text-navy-950" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"><path d={service.icon} /></svg>
                  </span>
                  <h3 className="mt-5 font-display text-lg font-bold text-navy-950">{service.title}</h3>
                  <p className="mt-2 text-sm leading-relaxed text-navy-500">{service.description}</p>
                </div>
              </Reveal>
            ))}
          </div>
        </div>
      </section>

      {/* 4. CARGO CATEGORIES */}
      <section className="bg-white py-20">
        <div className="container-page">
          <Reveal>
            <SectionHeading eyebrow="What We Ship" title="Cargo Categories" subtitle="Dynamic catalogue maintained by our operations team — request any commodity category." />
          </Reveal>
          <div className="mt-12 flex flex-wrap justify-center gap-3">
            {(categories.length
              ? categories.map((category) => ({ id: category.id, name: category.name }))
              : ['Iron Ore', 'Coal', 'Steel', 'Cement', 'Rice', 'Wheat', 'Minerals', 'General Cargo'].map((name, i) => ({ id: `s${i}`, name }))
            ).map((category, index) => (
              <Reveal key={category.id} delay={index * 0.04}>
                <Link
                  to="/cargo"
                  className="inline-block rounded-full border border-navy-200 bg-navy-50 px-5 py-2.5 text-sm font-semibold text-navy-800 transition hover:border-gold-500 hover:bg-gold-500 hover:text-navy-950"
                >
                  {category.name}
                </Link>
              </Reveal>
            ))}
          </div>
        </div>
      </section>

      {/* 5. AVAILABLE SHIPMENTS */}
      <section className="bg-navy-950 py-20 text-white">
        <div className="container-page">
          <Reveal>
            <SectionHeading light eyebrow="Live Board" title="Available Shipments" subtitle="Indicative offerings currently open for enquiry. Pricing is indicative per metric tonne." />
          </Reveal>
          {cargo.length === 0 ? (
            <Reveal delay={0.1}>
              <div className="mx-auto mt-12 max-w-3xl rounded-xl border border-dashed border-white/20 bg-white/5 p-10 text-center">
                <p className="text-white/70">New cargo lots are being prepared. Register to be notified first.</p>
              </div>
            </Reveal>
          ) : (
            <div className="mt-12 grid gap-6 md:grid-cols-3">
              {cargo.map((lot, index) => (
                <Reveal key={lot.id} delay={index * 0.08}>
                  <article className="flex h-full flex-col rounded-xl border border-white/10 bg-white/5 p-6 backdrop-blur transition hover:border-gold-500/40">
                    <p className="text-xs font-bold uppercase tracking-widest text-gold-400">{lot.categoryName || 'Cargo'}</p>
                    <h3 className="mt-2 font-display text-xl font-bold">{lot.name}</h3>
                    <dl className="mt-4 space-y-2 text-sm text-white/70">
                      <div className="flex justify-between gap-4"><dt>Quantity</dt><dd className="text-right font-semibold text-white">{lot.quantity?.toLocaleString()} {lot.unit || 'MT'}</dd></div>
                      <div className="flex justify-between gap-4"><dt>Route</dt><dd className="text-right font-semibold text-white">{lot.loadingPortCode || lot.originCountry} → {lot.destinationPortCode || lot.destinationCountry}</dd></div>
                      <div className="flex justify-between gap-4"><dt>Loading</dt><dd className="text-right font-semibold text-white">{lot.loadingDate || 'On request'}</dd></div>
                      <div className="flex justify-between gap-4"><dt>Status</dt><dd className="text-right font-semibold text-emerald-400">Available</dd></div>
                    </dl>
                    <div className="mt-5 flex items-end justify-between border-t border-white/10 pt-4">
                      <p className="font-display text-2xl font-extrabold text-gold-400">
                        {formatMoney(lot.indicativePrice, lot.currency)}
                        <span className="ml-1 text-sm font-medium text-white/50">/ {lot.unit || 'MT'}</span>
                      </p>
                    </div>
                    <Link to={`/register?enquiry=${lot.id}`} className="btn-primary mt-5 w-full">Request Quote</Link>
                  </article>
                </Reveal>
              ))}
            </div>
          )}
          <Reveal delay={0.2} className="mt-10 text-center">
            <Link to="/shipments" className="text-sm font-semibold text-gold-400 hover:text-gold-500">View all available shipments →</Link>
          </Reveal>
        </div>
      </section>

      {/* 6. GLOBAL NETWORK */}
      <section className="relative overflow-hidden bg-gradient-to-r from-ocean-600 via-navy-800 to-navy-900 py-20 text-white">
        <div className="container-page relative grid items-center gap-12 lg:grid-cols-2">
          <Reveal>
            <SectionHeading light center={false} eyebrow="Reach" title="A truly global network" subtitle="Loading across Indian ports, delivering to every major hub — with partners at both ends." />
          </Reveal>
          <Reveal delay={0.15}>
            <div className="grid grid-cols-2 gap-6">
              {[
                ['Asia', 'Mundra · Nhava Sheva · Chennai · Singapore'],
                ['Middle East', 'Jebel Ali · Dammam · Hamad'],
                ['Europe', 'Rotterdam · Hamburg · Piraeus'],
                ['Americas & Africa', 'Houston · Santos · Durban · Mombasa'],
              ].map(([region, ports]) => (
                <div key={region} className="rounded-xl border border-white/15 bg-white/5 p-5 backdrop-blur">
                  <p className="font-display font-bold text-gold-400">{region}</p>
                  <p className="mt-1.5 text-sm text-white/70">{ports}</p>
                </div>
              ))}
            </div>
          </Reveal>
        </div>
      </section>

      {/* 7. HOW IT WORKS */}
      <section className="bg-white py-20">
        <div className="container-page">
          <Reveal>
            <SectionHeading eyebrow="Simple Process" title="How It Works" subtitle="Six steps from first enquiry to final receipt." />
          </Reveal>
          <div className="mt-12 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {steps.map((step, index) => (
              <Reveal key={step.title} delay={(index % 3) * 0.08}>
                <div className="relative h-full rounded-xl border border-navy-100 bg-navy-50/60 p-7">
                  <span className="absolute -top-4 left-6 flex h-9 w-9 items-center justify-center rounded-lg bg-gold-500 font-display text-sm font-extrabold text-navy-950 shadow">
                    {String(index + 1).padStart(2, '0')}
                  </span>
                  <h3 className="mt-4 font-display text-lg font-bold text-navy-950">{step.title}</h3>
                  <p className="mt-2 text-sm leading-relaxed text-navy-500">{step.text}</p>
                </div>
              </Reveal>
            ))}
          </div>
        </div>
      </section>

      {/* 8. SHIPMENT TRACKING */}
      <section className="bg-navy-50 py-20">
        <div className="container-page grid items-center gap-12 lg:grid-cols-2">
          <Reveal>
            <SectionHeading center={false} eyebrow="Visibility" title="Track every shipment in real time" subtitle="Enter a shipment reference and watch it move through loading, departure, transit checkpoints and arrival." />
            <Link to="/tracking" className="btn-primary mt-8">Track Your Shipment</Link>
          </Reveal>
          <Reveal delay={0.15}>
            <ol className="relative space-y-6 border-l-2 border-dashed border-navy-200 pl-6">
              {[
                ['Booking Confirmed', 'Completed'],
                ['Loading — Mundra Port', 'Completed'],
                ['Departed — INNSA1 → AEJEA', 'Completed'],
                ['In Transit — Arabian Sea', 'Current position'],
                ['ETA Jebel Ali — Sep 15', 'Upcoming'],
              ].map(([label, state], index) => (
                <li key={label} className="relative">
                  <span className={`absolute -left-[31px] top-1 h-4 w-4 rounded-full border-2 ${
                    state === 'Completed' ? 'border-gold-500 bg-gold-500' :
                    state === 'Current position' ? 'animate-pulse border-ocean-500 bg-ocean-500' : 'border-navy-200 bg-white'
                  }`} />
                  <p className="text-sm font-semibold text-navy-900">{label}</p>
                  <p className={`text-xs ${state === 'Upcoming' ? 'text-navy-400' : 'text-navy-500'}`}>{state}</p>
                </li>
              ))}
            </ol>
          </Reveal>
        </div>
      </section>

      {/* 9 + 10. WHY CHOOSE US / STATISTICS */}
      <section className="bg-white py-20">
        <div className="container-page">
          <Reveal><SectionHeading eyebrow="Why Choose Us" title="Trade with confidence" /></Reveal>
          <div className="mt-12 grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
            {whyUs.map((item, index) => (
              <Reveal key={item.title} delay={index * 0.07}>
                <div className="h-full rounded-xl bg-navy-950 p-7 text-white">
                  <h3 className="font-display text-base font-bold text-gold-400">{item.title}</h3>
                  <p className="mt-2 text-sm leading-relaxed text-white/70">{item.text}</p>
                </div>
              </Reveal>
            ))}
          </div>
          <Reveal delay={0.1}>
            <div className="mt-16 rounded-2xl bg-gradient-to-r from-navy-900 to-navy-700 py-12">
              <div className="grid grid-cols-2 gap-10 md:grid-cols-4">
                <StatCounter value={stats ? stats.activeVessels + 118 : 120} suffix="+" label="Vessels in Network" />
                <StatCounter value={stats ? stats.portsServed + 100 : 120} suffix="+" label="Ports Served" />
                <StatCounter value={98} suffix="%" label="On-Time Delivery" />
                <StatCounter value={40} suffix="+" label="Countries Covered" />
              </div>
            </div>
          </Reveal>
        </div>
      </section>

      {/* 11. TESTIMONIALS */}
      <section className="bg-navy-50 py-20">
        <div className="container-page">
          <Reveal><SectionHeading eyebrow="Testimonials" title="Trusted by importers worldwide" /></Reveal>
          <div className="mt-12 grid gap-6 md:grid-cols-3">
            {reviews.map((review, index) => (
              <Reveal key={review.id} delay={index * 0.08}>
                <figure className="flex h-full flex-col rounded-xl border border-navy-100 bg-white p-7 shadow-sm">
                  <div className="flex gap-1 text-gold-500">
                    {Array.from({ length: 5 }).map((_, starIndex) => (
                      <svg key={starIndex} className={`h-4 w-4 ${starIndex < review.rating ? '' : 'opacity-25'}`} viewBox="0 0 20 20" fill="currentColor"><path d="M9.05 2.9c.3-.9 1.6-.9 1.9 0l1.1 3.4a1 1 0 00.95.7h3.6c1 0 1.4 1.2.6 1.8l-2.9 2.1a1 1 0 00-.36 1.12l1.12 3.42c.3.94-.78 1.72-1.58 1.13l-2.9-2.1a1 1 0 00-1.18 0l-2.9 2.1c-.8.59-1.88-.2-1.58-1.13l1.12-3.42a1 1 0 00-.36-1.12L3.4 8.8c-.8-.6-.39-1.8.6-1.8h3.6a1 1 0 00.95-.69l1.1-3.41z" /></svg>
                    ))}
                  </div>
                  <blockquote className="mt-4 flex-1 text-sm leading-relaxed text-navy-700">
                    <p className="font-semibold text-navy-950">“{review.title}”</p>
                    <p className="mt-2">{review.reviewText}</p>
                  </blockquote>
                  <figcaption className="mt-5 border-t border-navy-100 pt-4 text-sm">
                    <p className="font-semibold text-navy-950">{review.clientName}</p>
                    {review.companyName && <p className="text-navy-500">{review.companyName}</p>}
                  </figcaption>
                </figure>
              </Reveal>
            ))}
          </div>
        </div>
      </section>

      {/* 12. CALL TO ACTION */}
      <section className="relative overflow-hidden bg-navy-950 py-20 text-white">
        <div className="absolute inset-0 opacity-25 [background-image:radial-gradient(circle_at_20%_30%,#0ea5e955_0,transparent_45%),radial-gradient(circle_at_80%_70%,#c9a22744_0,transparent_40%)]" />
        <div className="container-page relative text-center">
          <Reveal>
            <h2 className="font-display text-3xl font-bold sm:text-4xl">Ready to move your cargo?</h2>
            <p className="mx-auto mt-4 max-w-xl text-white/70">
              Create your free account, submit an export requirement and receive a formal quotation — usually within one business day.
            </p>
            <div className="mt-8 flex flex-col items-center justify-center gap-4 sm:flex-row">
              <Link to="/register" className="btn-primary">Request Export Quote</Link>
              <Link to="/contact" className="btn-secondary">Talk to Our Team</Link>
            </div>
          </Reveal>
        </div>
      </section>
    </>
  )
}
