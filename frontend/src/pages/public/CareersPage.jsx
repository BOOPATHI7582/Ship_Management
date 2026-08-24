import { Link } from 'react-router-dom'
import Reveal from '../../components/Reveal'

const openings = [
  { role: 'Ship Manager', location: 'Mumbai · Full-time', type: 'Operations' },
  { role: 'Trade Finance Analyst', location: 'Mumbai · Full-time', type: 'Finance' },
  { role: 'Chartering Executive', location: 'Dubai · Full-time', type: 'Operations' },
]

export default function CareersPage() {
  return (
    <>
      <section className="bg-gradient-to-br from-navy-950 via-navy-900 to-navy-700 py-24 text-white">
        <div className="container-page text-center">
          <Reveal>
            <p className="text-xs font-bold uppercase tracking-[0.2em] text-gold-400">Careers</p>
            <h1 className="mx-auto mt-3 max-w-3xl font-display text-4xl font-extrabold sm:text-5xl">
              Build the future of global trade
            </h1>
            <p className="mx-auto mt-5 max-w-2xl text-lg text-white/70">
              Join a team that moves real cargo across real oceans — with modern tools and global exposure.
            </p>
          </Reveal>
        </div>
      </section>

      <section className="bg-white py-20">
        <div className="container-page max-w-4xl">
          <div className="space-y-5">
            {openings.map((job, index) => (
              <Reveal key={job.role} delay={index * 0.07}>
                <Link
                  to="/contact"
                  className="flex flex-col gap-3 rounded-xl border border-navy-100 bg-white p-6 shadow-sm transition hover:-translate-y-0.5 hover:border-gold-500/50 hover:shadow-md sm:flex-row sm:items-center sm:justify-between"
                >
                  <div>
                    <h2 className="font-display text-lg font-bold text-navy-950">{job.role}</h2>
                    <p className="mt-0.5 text-sm text-navy-500">{job.location}</p>
                  </div>
                  <span className="rounded-full bg-gold-500 px-4 py-1.5 text-xs font-bold uppercase tracking-wider text-navy-950">
                    Apply →
                  </span>
                </Link>
              </Reveal>
            ))}
          </div>
          <Reveal delay={0.2}>
            <p className="mt-10 text-center text-sm text-navy-400">
              Don&apos;t see your role? Send a speculative application via our contact form.
            </p>
          </Reveal>
        </div>
      </section>
    </>
  )
}
