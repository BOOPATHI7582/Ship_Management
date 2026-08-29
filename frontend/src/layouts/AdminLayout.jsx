import { Link } from 'react-router-dom'
import DashboardShell from '../components/DashboardShell'

const navItems = [
  { to: '/admin/dashboard', label: 'Dashboard', end: true },
  { to: '/manager/contacts', label: 'Inbox' },
  { to: '/admin/users', label: 'Users' },
  { to: '/admin/clients', label: 'Clients' },
  { to: '/admin/vessels', label: 'Vessels' },
  { to: '/admin/cargo', label: 'Cargo Lots' },
  { to: '/admin/categories', label: 'Categories' },
  { to: '/admin/taxes', label: 'Taxes' },
  { to: '/admin/ports', label: 'Ports' },
  { to: '/manager/enquiries', label: 'Enquiry Queue' },
]

export default function AdminLayout() {
  return (
    <DashboardShell
      homeTo="/admin/dashboard"
      tag="Admin"
      tagClasses="bg-gold-500/20 text-gold-400"
      items={navItems}
      badgePath="/manager/contacts"
      headerExtras={
        <Link
          to="/manager/enquiries"
          className="rounded-lg border border-white/30 px-3 py-1.5 text-sm font-semibold transition hover:bg-white/10"
        >
          Ops
        </Link>
      }
    />
  )
}
