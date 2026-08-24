import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { createEnquiry } from '../../api/client'
import { fetchCargoCategories, fetchPorts } from '../../api/public'

const steps = ['Client Info', 'Cargo', 'Shipping', 'Budget & Schedule', 'Review']

const initialForm = {
  contactName: '',
  contactPhone: '',
  cargoType: '',
  cargoCategoryId: '',
  cargoDescription: '',
  quantity: '',
  unit: 'MT',
  originCountry: '',
  originLocation: '',
  destinationCountry: '',
  destinationLocation: '',
  loadingPortId: '',
  destinationPortId: '',
  targetPricePerUnit: '',
  estimatedBudget: '',
  currency: 'USD',
  requiredLoadingDate: '',
  expectedDeliveryDate: '',
  message: '',
}

function StepClientInfo({ form, setField, errors }) {
  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
      <Field label="Contact Person *" error={errors.contactName}>
        <input className={inputClass(errors.contactName)} value={form.contactName}
          onChange={(e) => setField('contactName', e.target.value)} placeholder="Full name" />
      </Field>
      <Field label="Contact Phone" error={errors.contactPhone}>
        <input className={inputClass(errors.contactPhone)} value={form.contactPhone}
          onChange={(e) => setField('contactPhone', e.target.value)} placeholder="+91 …" />
      </Field>
    </div>
  )
}

function StepCargo({ form, setField, errors, categories }) {
  return (
    <div className="space-y-4">
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <Field label="Cargo Category">
          <select className={inputClass()} value={form.cargoCategoryId}
            onChange={(e) => setField('cargoCategoryId', e.target.value)}>
            <option value="">Select category</option>
            {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
        </Field>
        <Field label="Cargo Type *" error={errors.cargoType}>
          <input className={inputClass(errors.cargoType)} value={form.cargoType}
            onChange={(e) => setField('cargoType', e.target.value)}
            placeholder="e.g. Basmati Rice 1121 / Iron Ore Fines" />
        </Field>
        <Field label="Quantity *" error={errors.quantity}>
          <input type="number" min="0.0001" step="any" className={inputClass(errors.quantity)}
            value={form.quantity} onChange={(e) => setField('quantity', e.target.value)} />
        </Field>
        <Field label="Unit">
          <select className={inputClass()} value={form.unit}
            onChange={(e) => setField('unit', e.target.value)}>
            {['MT', 'KG', 'CBM', 'TEU', 'FEU'].map((u) => <option key={u}>{u}</option>)}
          </select>
        </Field>
        <Field label="Origin Country *" error={errors.originCountry}>
          <input className={inputClass(errors.originCountry)} value={form.originCountry}
            onChange={(e) => setField('originCountry', e.target.value)} placeholder="e.g. India" />
        </Field>
        <Field label="Destination Country *" error={errors.destinationCountry}>
          <input className={inputClass(errors.destinationCountry)} value={form.destinationCountry}
            onChange={(e) => setField('destinationCountry', e.target.value)} placeholder="e.g. United Arab Emirates" />
        </Field>
      </div>
      <Field label="Cargo Description">
        <textarea rows={3} className={inputClass()} value={form.cargoDescription}
          onChange={(e) => setField('cargoDescription', e.target.value)}
          placeholder="Grade, packaging, certificates, inspection needs…" />
      </Field>
    </div>
  )
}

function StepShipping({ form, setField, errors, ports }) {
  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
      <Field label="Loading Port">
        <select className={inputClass()} value={form.loadingPortId}
          onChange={(e) => setField('loadingPortId', e.target.value)}>
          <option value="">Select port</option>
          {ports.map((p) => <option key={p.id} value={p.id}>{p.name} ({p.code})</option>)}
        </select>
      </Field>
      <Field label="Destination Port">
        <select className={inputClass()} value={form.destinationPortId}
          onChange={(e) => setField('destinationPortId', e.target.value)}>
          <option value="">Select port</option>
          {ports.map((p) => <option key={p.id} value={p.id}>{p.name} ({p.code})</option>)}
        </select>
      </Field>
      <Field label="Origin Location (city/region)">
        <input className={inputClass()} value={form.originLocation}
          onChange={(e) => setField('originLocation', e.target.value)} placeholder="e.g. Mumbai, Maharashtra" />
      </Field>
      <Field label="Destination Location (city/region)">
        <input className={inputClass()} value={form.destinationLocation}
          onChange={(e) => setField('destinationLocation', e.target.value)} placeholder="e.g. Jebel Ali, Dubai" />
      </Field>
    </div>
  )
}

function StepBudget({ form, setField, errors }) {
  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
      <Field label="Target Price per Unit" error={errors.targetPricePerUnit}>
        <input type="number" min="0" step="0.01" className={inputClass(errors.targetPricePerUnit)}
          value={form.targetPricePerUnit} onChange={(e) => setField('targetPricePerUnit', e.target.value)}
          placeholder="Optional" />
      </Field>
      <Field label="Estimated Total Budget" error={errors.estimatedBudget}>
        <input type="number" min="0" step="0.01" className={inputClass(errors.estimatedBudget)}
          value={form.estimatedBudget} onChange={(e) => setField('estimatedBudget', e.target.value)}
          placeholder="Optional" />
      </Field>
      <Field label="Currency">
        <select className={inputClass()} value={form.currency}
          onChange={(e) => setField('currency', e.target.value)}>
          {['USD', 'EUR', 'INR', 'AED'].map((c) => <option key={c}>{c}</option>)}
        </select>
      </Field>
      <div />
      <Field label="Required Loading Date *" error={errors.requiredLoadingDate}>
        <input type="date" className={inputClass(errors.requiredLoadingDate)} value={form.requiredLoadingDate}
          onChange={(e) => setField('requiredLoadingDate', e.target.value)} />
      </Field>
      <Field label="Expected Delivery Date">
        <input type="date" className={inputClass()} value={form.expectedDeliveryDate}
          onChange={(e) => setField('expectedDeliveryDate', e.target.value)} />
      </Field>
    </div>
  )
}

function StepReview({ form, categories, ports }) {
  const categoryName = categories.find((c) => String(c.id) === String(form.cargoCategoryId))?.name
  const findPort = (id) => {
    const port = ports.find((p) => String(p.id) === String(id))
    return port ? `${port.name} (${port.code})` : '-'
  }
  const rows = [
    ['Contact Person', form.contactName],
    ['Phone', form.contactPhone || '-'],
    ['Cargo', `${categoryName ? categoryName + ' — ' : ''}${form.cargoType}`],
    ['Quantity', `${form.quantity} ${form.unit}`],
    ['Route', `${form.originCountry} → ${form.destinationCountry}`],
    ['Locations', `${form.originLocation || '-'} → ${form.destinationLocation || '-'}`],
    ['Ports', `${findPort(form.loadingPortId)} → ${findPort(form.destinationPortId)}`],
    ['Target Price', form.targetPricePerUnit ? `${form.currency} ${form.targetPricePerUnit}/${form.unit}` : '-'],
    ['Est. Budget', form.estimatedBudget ? `${form.currency} ${form.estimatedBudget}` : '-'],
    ['Loading From', form.requiredLoadingDate],
    ['Expected Delivery', form.expectedDeliveryDate || '-'],
  ]
  return (
    <dl className="divide-y divide-navy-100 rounded-xl border border-navy-100">
      {rows.map(([label, value]) => (
        <div key={label} className="flex justify-between gap-6 px-4 py-3 text-sm odd:bg-navy-50/60">
          <dt className="font-semibold text-navy-700">{label}</dt>
          <dd className="text-right text-navy-900">{value}</dd>
        </div>
      ))}
    </dl>
  )
}

const inputClass = (hasError) =>
  `w-full rounded-lg border bg-white px-3.5 py-2.5 text-sm text-navy-900 outline-none transition focus:border-gold-500 focus:ring-2 focus:ring-gold-500/30 ${
    hasError ? 'border-red-400' : 'border-navy-200'
  }`

function Field({ label, error, children }) {
  return (
    <label className="block">
      <span className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-navy-500">{label}</span>
      {children}
      {error && <span className="mt-1 block text-xs font-medium text-red-600">{error}</span>}
    </label>
  )
}

export default function NewEnquiryPage() {
  const navigate = useNavigate()
  const [step, setStep] = useState(0)
  const [form, setForm] = useState(initialForm)
  const [errors, setErrors] = useState({})
  const [categories, setCategories] = useState([])
  const [ports, setPorts] = useState([])
  const [submitting, setSubmitting] = useState(false)
  const [serverError, setServerError] = useState(null)

  useEffect(() => {
    fetchCargoCategories().then((res) => setCategories(res.data)).catch(() => {})
    fetchPorts().then((res) => setPorts(res.data)).catch(() => {})
  }, [])

  const setField = (name, value) => {
    setForm((prev) => ({ ...prev, [name]: value }))
    setErrors((prev) => ({ ...prev, [name]: undefined }))
  }

  function validateStep(index) {
    const next = {}
    if (index === 0 && !form.contactName.trim()) next.contactName = 'Contact person is required'
    if (index === 1) {
      if (!form.cargoType.trim()) next.cargoType = 'Describe the cargo type'
      if (!form.quantity || Number(form.quantity) <= 0) next.quantity = 'Quantity must be positive'
      if (!form.originCountry.trim()) next.originCountry = 'Origin country is required'
      if (!form.destinationCountry.trim()) next.destinationCountry = 'Destination country is required'
    }
    if (index === 3 && !form.requiredLoadingDate) next.requiredLoadingDate = 'Required loading date is needed'
    setErrors(next)
    return Object.keys(next).length === 0
  }

  const payload = useMemo(() => ({
    contactName: form.contactName,
    contactPhone: form.contactPhone || null,
    cargoType: form.cargoType,
    cargoCategoryId: form.cargoCategoryId ? Number(form.cargoCategoryId) : null,
    cargoDescription: form.cargoDescription || null,
    quantity: Number(form.quantity),
    unit: form.unit,
    originCountry: form.originCountry,
    originLocation: form.originLocation || null,
    destinationCountry: form.destinationCountry,
    destinationLocation: form.destinationLocation || null,
    loadingPortId: form.loadingPortId ? Number(form.loadingPortId) : null,
    destinationPortId: form.destinationPortId ? Number(form.destinationPortId) : null,
    targetPricePerUnit: form.targetPricePerUnit ? Number(form.targetPricePerUnit) : null,
    estimatedBudget: form.estimatedBudget ? Number(form.estimatedBudget) : null,
    currency: form.currency,
    requiredLoadingDate: form.requiredLoadingDate || null,
    expectedDeliveryDate: form.expectedDeliveryDate || null,
    message: form.message || null,
  }), [form])

  async function handleSubmit() {
    for (let i = 0; i <= 3; i += 1) {
      if (!validateStep(i)) {
        setStep(i)
        return
      }
    }
    setSubmitting(true)
    setServerError(null)
    try {
      await createEnquiry(payload)
      navigate('/client/enquiries', { state: { created: true } })
    } catch (err) {
      setServerError(err.response?.data?.message || 'Failed to submit enquiry')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="space-y-6">
      <h1 className="font-display text-2xl font-bold text-navy-950">New Export Enquiry</h1>

      <ol className="flex flex-wrap gap-2">
        {steps.map((label, index) => (
          <li key={label}>
            <button
              type="button"
              onClick={() => index < step && setStep(index)}
              className={`rounded-full px-4 py-1.5 text-xs font-bold uppercase tracking-wide transition ${
                index === step
                  ? 'bg-navy-950 text-white'
                  : index < step
                    ? 'bg-gold-500/20 text-navy-800 hover:bg-gold-500/40'
                    : 'bg-white text-navy-300 border border-navy-100'
              }`}
            >
              {index + 1}. {label}
            </button>
          </li>
        ))}
      </ol>

      <div className="rounded-2xl border border-navy-100 bg-white p-6 shadow-sm">
        {step === 0 && <StepClientInfo form={form} setField={setField} errors={errors} />}
        {step === 1 && <StepCargo form={form} setField={setField} errors={errors} categories={categories} />}
        {step === 2 && <StepShipping form={form} setField={setField} errors={errors} ports={ports} />}
        {step === 3 && <StepBudget form={form} setField={setField} errors={errors} />}
        {step === 4 && <StepReview form={form} categories={categories} ports={ports} />}

        <div className="mt-4">
          <Field label="Notes to Operations Team">
            <textarea rows={3} className={inputClass()} value={form.message}
              onChange={(e) => setField('message', e.target.value)}
              placeholder="Anything else we should know?" />
          </Field>
        </div>

        {serverError && (
          <p className="mt-4 rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700">{serverError}</p>
        )}

        <div className="mt-6 flex items-center justify-between">
          <button
            type="button"
            disabled={step === 0}
            onClick={() => setStep((s) => Math.max(0, s - 1))}
            className="rounded-lg border border-navy-200 px-5 py-2.5 text-sm font-semibold text-navy-700 transition hover:bg-navy-50 disabled:opacity-40"
          >
            Back
          </button>
          {step < steps.length - 1 ? (
            <button
              type="button"
              onClick={() => validateStep(step) && setStep((s) => s + 1)}
              className="rounded-lg bg-navy-950 px-6 py-2.5 text-sm font-bold text-white transition hover:bg-navy-900"
            >
              Continue
            </button>
          ) : (
            <button
              type="button"
              disabled={submitting}
              onClick={handleSubmit}
              className="rounded-lg bg-gold-500 px-6 py-2.5 text-sm font-bold text-navy-950 transition hover:bg-gold-400 disabled:opacity-50"
            >
              {submitting ? 'Submitting...' : 'Submit Enquiry'}
            </button>
          )}
        </div>
      </div>
    </div>
  )
}
