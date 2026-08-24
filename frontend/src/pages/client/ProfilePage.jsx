import { useEffect, useState } from 'react'
import { fetchClientProfile, updateClientProfile } from '../../api/client'

const initialForm = {
  fullName: '',
  companyName: '',
  phone: '',
  country: '',
  gstin: '',
  addressLine1: '',
  addressLine2: '',
  city: '',
  state: '',
  postalCode: '',
}

export default function ProfilePage() {
  const [form, setForm] = useState(initialForm)
  const [email, setEmail] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    fetchClientProfile()
      .then((res) => {
        const profile = res.data
        setEmail(profile.email)
        setForm({
          fullName: profile.fullName || '',
          companyName: profile.companyName || '',
          phone: profile.phone || '',
          country: profile.country || '',
          gstin: profile.gstin || '',
          addressLine1: profile.addressLine1 || '',
          addressLine2: profile.addressLine2 || '',
          city: profile.city || '',
          state: profile.state || '',
          postalCode: profile.postalCode || '',
        })
      })
      .catch((err) => setError(err.response?.data?.message || 'Failed to load profile'))
      .finally(() => setLoading(false))
  }, [])

  const setField = (name, value) => setForm((prev) => ({ ...prev, [name]: value }))

  async function handleSubmit(event) {
    event.preventDefault()
    setSaving(true)
    setMessage(null)
    setError(null)
    try {
      const res = await updateClientProfile(form)
      setMessage(res.data.message || 'Profile updated')
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update profile')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <p className="text-sm text-navy-400">Loading profile...</p>

  const field = (label, name, props = {}) => (
    <label className="block">
      <span className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-navy-500">
        {label}
      </span>
      <input
        value={form[name]}
        onChange={(e) => setField(name, e.target.value)}
        className="w-full rounded-lg border border-navy-200 bg-white px-3.5 py-2.5 text-sm text-navy-900 outline-none transition focus:border-gold-500 focus:ring-2 focus:ring-gold-500/30"
        {...props}
      />
    </label>
  )

  return (
    <div className="space-y-6">
      <h1 className="font-display text-2xl font-bold text-navy-950">Company Profile</h1>

      <form onSubmit={handleSubmit} className="rounded-2xl border border-navy-100 bg-white p-6 shadow-sm">
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <label className="block">
            <span className="mb-1.5 block text-xs font-semibold uppercase tracking-wide text-navy-500">
              Email (login)
            </span>
            <input
              value={email}
              disabled
              className="w-full cursor-not-allowed rounded-lg border border-navy-100 bg-navy-50 px-3.5 py-2.5 text-sm text-navy-400"
            />
          </label>
          {field('Full Name *', 'fullName', { required: true })}
          {field('Company Name', 'companyName')}
          {field('Phone', 'phone')}
          {field('Country', 'country')}
          {field('GSTIN', 'gstin', { placeholder: 'e.g. 27AAPFU0939F1ZV' })}
          {field('Address Line 1', 'addressLine1')}
          {field('Address Line 2', 'addressLine2')}
          {field('City', 'city')}
          {field('State', 'state')}
          {field('Postal Code', 'postalCode')}
        </div>

        {message && (
          <p className="mt-4 rounded-lg bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-800">
            {message}
          </p>
        )}
        {error && (
          <p className="mt-4 rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700">{error}</p>
        )}

        <button
          type="submit"
          disabled={saving}
          className="mt-6 rounded-lg bg-navy-950 px-6 py-2.5 text-sm font-bold text-white transition hover:bg-navy-900 disabled:opacity-50"
        >
          {saving ? 'Saving...' : 'Save Profile'}
        </button>
      </form>
    </div>
  )
}
