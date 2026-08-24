import { Link } from 'react-router-dom'
import Reveal from '../../components/Reveal'
import SectionHeading from '../../components/SectionHeading'

const serviceGroups = [
  {
    title: 'Export Solutions',
    items: [
      ['Bulk & break-bulk chartering', 'Vessel sourcing, laycan planning and loading supervision for full-cargo consignments.'],
      ['Container freight (FCL / LCL)', 'Box booking, stuffing plans and consolidation through partner CFS terminals.'],
      ['Agri commodity exports', 'Rice, wheat, pulses and agri-products with fumigation, grading and phytosanitary support.'],
    ],
  },
  {
    title: 'Trade Documentation',
    items: [
      ['Quotations & proforma invoices', 'Itemised, tax-transparent documents generated and versioned on-platform.'],
      ['Tax invoicing', 'GST-ready tax invoices with HSN codes and export treatment handled correctly.'],
      ['Customs & compliance', 'Shipping bills, declarations and certificate coordination with CHA partners.'],
    ],
  },
  {
    title: 'Logistics & Visibility',
    items: [
      ['Shipment management', 'One accountable ship manager per file from booking to delivery.'],
      ['Checkpoint tracking', 'Manual milestone updates plus vessel position for every active shipment.'],
      ['Payments & receipts', 'Secure online payment gateway, bank transfer reconciliation, instant receipts.'],
    ],
  },
]

export default function ServicesPage() {
  return (
    <>
      <section className="bg-gradient-to-br from-navy-950 via-navy-900 to-navy-700 py-24 text-white">
        <div className="container-page text-center">
          <Reveal>
            <p className="text-xs font-bold uppercase tracking-[0.2em] text-gold-400">Services</p>
            <h1 className="mx-auto mt-3 max-w-3xl font-display text-4xl font-extrabold sm:text-5xl">
              Everything your export deal needs
            </h1>
            <p className="mx-auto mt-5 max-w-2xl text-lg text-white/70">
              Commercial, documentary and logistical services under one roof — priced transparently in
              your quotation.
            </p>
          </Reveal>
        </div>
      </section>

      {serviceGroups.map((group, groupIndex) => (
        <section key={group.title} className={groupIndex % 2 === 0 ? 'bg-white py-20' : 'bg-navy-50 py-20'}>
          <div className="container-page">
            <Reveal><SectionHeading eyebrow={`0${groupIndex + 1}`} title={group.title} /></Reveal>
            <div className="mt-10 grid gap-6 md:grid-cols-3">
              {group.items.map(([title, text], index) => (
                <Reveal key={title} delay={(index % 3) * 0.08}>
                  <div className="h-full rounded-xl border border-navy-100 bg-white p-7 shadow-sm transition hover:-translate-y-1 hover:shadow-md">
                    <h3 className="font-display text-lg font-bold text-navy-950">{title}</h3>
                    <p className="mt-2 text-sm leading-relaxed text-navy-500">{text}</p>
                  </div>
                </Reveal>
              ))}
            </div>
          </div>
        </section>
      ))}

      <section className="bg-navy-950 py-16 text-center text-white">
        <div className="container-page">
          <Reveal>
            <h2 className="font-display text-2xl font-bold">Need something bespoke?</h2>
            <Link to="/contact" className="btn-primary mt-6">Contact Our Team</Link>
          </Reveal>
        </div>
      </section>
    </>
  )
}
