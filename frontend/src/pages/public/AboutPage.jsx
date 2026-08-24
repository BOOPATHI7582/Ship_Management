import { Link } from 'react-router-dom'
import Reveal from '../../components/Reveal'
import SectionHeading from '../../components/SectionHeading'

const milestones = [
  { year: '2001', text: 'Founded as a commodity trading desk in Mumbai.' },
  { year: '2009', text: 'First owned chartering operation across the Arabian Gulf.' },
  { year: '2016', text: 'Expanded to agri exports with dedicated fumigation and QC partners.' },
  { year: '2024', text: 'Launched the ExportPlatform digital trading experience.' },
]

export default function AboutPage() {
  return (
    <>
      <section className="bg-gradient-to-br from-navy-950 via-navy-900 to-navy-700 py-24 text-white">
        <div className="container-page text-center">
          <Reveal>
            <p className="text-xs font-bold uppercase tracking-[0.2em] text-gold-400">About Us</p>
            <h1 className="mx-auto mt-3 max-w-3xl font-display text-4xl font-extrabold sm:text-5xl">
              Two decades of moving the world&apos;s cargo
            </h1>
            <p className="mx-auto mt-5 max-w-2xl text-lg text-white/70">
              ExportPlatform is the digital arm of an established export house — pairing deep
              maritime relationships with modern trade technology.
            </p>
          </Reveal>
        </div>
      </section>

      <section className="bg-white py-20">
        <div className="container-page grid gap-12 lg:grid-cols-2">
          <Reveal>
            <SectionHeading
              center={false}
              eyebrow="Our Mission"
              title="Make bulk export as simple as e-commerce"
              subtitle="We remove friction from international commodity trade: transparent indicative pricing, structured negotiation, compliant invoicing and verifiable shipment milestones."
            />
          </Reveal>
          <Reveal delay={0.1}>
            <SectionHeading
              center={false}
              eyebrow="Our Values"
              title="Integrity, safety, clarity"
              subtitle="Every consignment is insured and inspected. Every document is audit-ready. Every commitment is tracked on-platform for both sides of the deal."
            />
          </Reveal>
        </div>
      </section>

      <section className="bg-navy-50 py-20">
        <div className="container-page max-w-3xl">
          <Reveal><SectionHeading eyebrow="Journey" title="Milestones" /></Reveal>
          <ol className="mt-12 space-y-8 border-l-2 border-gold-500/40 pl-8">
            {milestones.map((item, index) => (
              <Reveal key={item.year} delay={index * 0.08}>
                <li className="relative">
                  <span className="absolute -left-[42px] flex h-6 w-6 items-center justify-center rounded-full bg-gold-500 font-display text-[10px] font-extrabold text-navy-950">
                    {index + 1}
                  </span>
                  <p className="font-display text-xl font-bold text-navy-950">{item.year}</p>
                  <p className="mt-1 text-navy-500">{item.text}</p>
                </li>
              </Reveal>
            ))}
          </ol>
        </div>
      </section>

      <section className="bg-white py-16 text-center">
        <div className="container-page">
          <Reveal>
            <h2 className="font-display text-2xl font-bold text-navy-950">Want to work with us?</h2>
            <Link to="/register" className="btn-primary mt-6">Create Your Account</Link>
          </Reveal>
        </div>
      </section>
    </>
  )
}
