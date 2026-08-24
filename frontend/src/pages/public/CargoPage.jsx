import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import Reveal from '../../components/Reveal'
import SectionHeading from '../../components/SectionHeading'
import { fetchCargoCategories } from '../../api/public'

const categoryIcons = {
  'Iron Ore': 'M12 3l9 5-9 5-9-5 9-5zm-9 9l9 5 9-5m-18 4l9 5 9-5',
  Coal: 'M6 6h12l3 6-3 6H6l-3-6 3-6z',
  Steel: 'M4 8h16v4H4zM4 14h10v2H4zm0 4h7v2H4z',
  Cement: 'M5 21V8l7-5 7 5v13H5zm4-8a3 3 0 006 0',
  Rice: 'M12 3c3 2.5 3 6.5 0 9-3-2.5-3-6.5 0-9zM5 14c2.5-.5 5 .5 7 3-2.5.5-5-.5-7-3zm14 0c-2.5-.5-5 .5-7 3 2.5.5 5-.5 7-3zM11 20h2v2h-2z',
  Wheat: 'M12 22v-8m0 0C8 12 6 8 7 4c4 1 6 4 5 8m0 2c0-4 2-7 6-8 1 4-1 8-6 8z',
  Minerals: 'M12 2l7 7-7 13L5 9l7-7zm-7 7h14',
}

function iconFor(name) {
  return categoryIcons[name] || 'M4 7h16v10H4zm4 10V7m4 10V7m4 10V7'
}

export default function CargoPage() {
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    let cancelled = false
    fetchCargoCategories()
      .then((res) => {
        if (!cancelled) setCategories(res.data || [])
      })
      .catch(() => {
        if (!cancelled) setFailed(true)
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
            <p className="text-xs font-bold uppercase tracking-[0.2em] text-gold-400">Cargo</p>
            <h1 className="mx-auto mt-3 max-w-3xl font-display text-4xl font-extrabold sm:text-5xl">
              Categories we export
            </h1>
            <p className="mx-auto mt-5 max-w-2xl text-lg text-white/70">
              A dynamic catalogue maintained by our operations team — new commodity categories are
              added as trade lanes open.
            </p>
          </Reveal>
        </div>
      </section>

      <section className="bg-white py-20">
        <div className="container-page">
          {loading && (
            <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
              {Array.from({ length: 8 }).map((_, index) => (
                <div key={index} className="h-44 animate-pulse rounded-xl bg-navy-100" />
              ))}
            </div>
          )}

          {!loading && categories.length > 0 && (
            <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
              {categories.map((category, index) => (
                <Reveal key={category.id} delay={(index % 4) * 0.06}>
                  <div className="group h-full rounded-xl border border-navy-100 bg-white p-7 shadow-sm transition hover:-translate-y-1 hover:border-gold-500/50 hover:shadow-md">
                    <span className="flex h-12 w-12 items-center justify-center rounded-lg bg-navy-900 transition group-hover:bg-gold-500">
                      <svg className="h-6 w-6 text-gold-400 transition group-hover:text-navy-950" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
                        <path d={iconFor(category.name)} />
                      </svg>
                    </span>
                    <h2 className="mt-5 font-display text-lg font-bold text-navy-950">{category.name}</h2>
                    {category.description && (
                      <p className="mt-2 text-sm leading-relaxed text-navy-500">{category.description}</p>
                    )}
                  </div>
                </Reveal>
              ))}
            </div>
          )}

          {!loading && categories.length === 0 && (
            <div className={failed ? '' : 'hidden'}>
              <SectionHeading
                title="Catalogue updating"
                subtitle="Our cargo catalogue is being refreshed right now. Please check back shortly."
              />
            </div>
          )}

          <Reveal delay={0.15}>
            <div className="mt-14 rounded-2xl bg-navy-950 p-10 text-center text-white">
              <h2 className="font-display text-2xl font-bold">Don&apos;t see your commodity?</h2>
              <p className="mx-auto mt-3 max-w-xl text-white/70">
                We regularly open new categories for qualified buyers. Tell us what you need.
              </p>
              <Link to="/contact" className="btn-primary mt-6">Request a Category</Link>
            </div>
          </Reveal>
        </div>
      </section>
    </>
  )
}
