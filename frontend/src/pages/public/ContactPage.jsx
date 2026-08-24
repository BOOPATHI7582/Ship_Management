import { useState } from 'react'
import Reveal from '../../components/Reveal'
import SectionHeading from '../../components/SectionHeading'
import { submitContactMessage } from '../../api/public'
import { apiErrorMessage } from '../../api/axios'

const offices = [
  { city: 'Mumbai — HQ', lines: ['Nariman Point Business Centre', 'Mumbai 400021, India'] },
  { city: 'Dubai', lines: ['Jebel Ali Free Zone FZ-17', 'Dubai, UAE'] },
  { city: 'Singapore', lines: ['HarbourFront Centre, #09-11', 'Singapore 098585'] },
]

export default function ContactPage() {
  const [form, setForm] = useState({ fullName: '', email: '', phone: '', company: '', subject: '', message: '' })
  const [sent, setSent] = useState(false)
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  function update(field) {
    return (e) => setForm({ ...form, [field]: e.target.value })
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await submitContactMessage({
        fullName: form.fullName,
        email: form.email,
        phone: form.phone || undefined,
        company: form.company || undefined,
        subject: form.subject || undefined,
        message: form.message,
      })
      setSent(true)
    } catch (err) {
      setError(apiErrorMessage(err, 'Unable to send your message. Please try again.'))
    } finally {
      setSubmitting(false)
    }
  }

  const inputClass =
    'mt-1.5 w-full rounded-lg border border-navy-200 bg-white px-4 py-2.5 text-navy-950 placeholder-navy-300 focus:border-gold-500 focus:outline-none focus:ring-1 focus:ring-gold-500'

  return (
    <>
      <section className="bg-gradient-to-br from-navy-950 via-navy-900 to-navy-700 py-24 text-white">
        <div className="container-page text-center">
          <Reveal>
            <p className="text-xs font-bold uppercase tracking-[0.2em] text-gold-400">Contact Us</p>
            <h1 className="mx-auto mt-3 max-w-3xl font-display text-4xl font-extrabold sm:text-5xl">
              Let&apos;s talk trade
            </h1>
            <p className="mx-auto mt-5 max-w-2xl text-lg text-white/70">
              Questions about a category, a lane or pricing? Our export desk replies within one business day.
            </p>
          </Reveal>
        </div>
      </section>

      <section className="bg-white py-20">
        <div className="container-page grid gap-14 lg:grid-cols-[1.3fr_1fr]">
          <Reveal>
            {sent ? (
              <div className="rounded-2xl border border-emerald-200 bg-emerald-50 p-10 text-center">
                <h2 className="font-display text-2xl font-bold text-emerald-800">Message received</h2>
                <p className="mt-3 text-emerald-700">
                  Thank you for reaching out. A member of our export desk will reply to{' '}
                  <span className="font-semibold">{form.email}</span> shortly.
                </p>
              </div>
            ) : (
              <>
                <SectionHeading center={false} eyebrow="Write to us" title="Send a message" />
                {error && (
                  <p className="mt-5 rounded-lg border border-red-300 bg-red-50 px-4 py-2.5 text-sm text-red-600">
                    {error}
                  </p>
                )}
                <form onSubmit={handleSubmit} className="mt-7 grid gap-5 sm:grid-cols-2">
                  <div>
                    <label htmlFor="fullName" className="block text-sm font-semibold text-navy-900">Full Name *</label>
                    <input id="fullName" type="text" required maxLength={150} value={form.fullName} onChange={update('fullName')} className={inputClass} placeholder="Jane Trader" />
                  </div>
                  <div>
                    <label htmlFor="email" className="block text-sm font-semibold text-navy-900">Email *</label>
                    <input id="email" type="email" required value={form.email} onChange={update('email')} className={inputClass} placeholder="you@company.com" />
                  </div>
                  <div>
                    <label htmlFor="phone" className="block text-sm font-semibold text-navy-900">Phone</label>
                    <input id="phone" type="tel" maxLength={30} value={form.phone} onChange={update('phone')} className={inputClass} placeholder="+91 …" />
                  </div>
                  <div>
                    <label htmlFor="company" className="block text-sm font-semibold text-navy-900">Company</label>
                    <input id="company" type="text" maxLength={200} value={form.company} onChange={update('company')} className={inputClass} placeholder="Acme Trading Ltd." />
                  </div>
                  <div className="sm:col-span-2">
                    <label htmlFor="subject" className="block text-sm font-semibold text-navy-900">Subject</label>
                    <input id="subject" type="text" maxLength={255} value={form.subject} onChange={update('subject')} className={inputClass} placeholder="Iron ore enquiry — 50,000 MT" />
                  </div>
                  <div className="sm:col-span-2">
                    <label htmlFor="message" className="block text-sm font-semibold text-navy-900">Message *</label>
                    <textarea id="message" required rows={6} maxLength={5000} value={form.message} onChange={update('message')} className={inputClass} placeholder="Tell us about your cargo, quantity and destination…" />
                  </div>
                  <button type="submit" disabled={submitting} className="btn-primary sm:col-span-2">
                    {submitting ? 'Sending…' : 'Send Message'}
                  </button>
                </form>
              </>
            )}
          </Reveal>

          <Reveal delay={0.12}>
            <div className="space-y-6">
              {offices.map((office) => (
                <div key={office.city} className="rounded-xl border border-navy-100 bg-navy-50/60 p-6">
                  <h3 className="font-display font-bold text-navy-950">{office.city}</h3>
                  {office.lines.map((line) => (
                    <p key={line} className="mt-1 text-sm text-navy-500">{line}</p>
                  ))}
                </div>
              ))}
              <div className="rounded-xl bg-navy-950 p-6 text-white">
                <h3 className="font-display font-bold text-gold-400">Prefer email?</h3>
                <p className="mt-1.5 text-sm text-white/70">trade@exportplatform.example</p>
                <p className="text-sm text-white/70">operations@exportplatform.example</p>
              </div>
            </div>
          </Reveal>
        </div>
      </section>
    </>
  )
}
