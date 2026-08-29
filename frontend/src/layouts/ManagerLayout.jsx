import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import DashboardShell from '../components/DashboardShell'

const baseNavItems = [
  { to: '/manager/contacts', label: 'Inbox' },
  { to: '/manager/enquiries', label: 'Enquiry Queue' },
  { to: '/manager/proforma', label: 'Proforma Invoices' },
  { to: '/manager/invoices', label: 'Tax Invoices' },
  { to: '/manager/receipts', label: 'Receipts' },
  { to: '/manager/shipments', label: 'Shipments' },
  { to: '/manager/documents', label: 'Documents' },
  { to: '/manager/reviews', label: 'Reviews' },
]

const adminNavItems = [
  { to: '/manager/reports', label: 'Reports' },
  { to: '/manager/audit', label: 'Audit Log' },
]

export default function ManagerLayout() {
  const { isAdmin } = useAuth()
  const navItems = isAdmin ? [...baseNavItems, ...adminNavItems] : baseNavItems

  return (
    <DashboardShell
      homeTo="/manager/enquiries"
      tag="Operations"
      tagClasses="bg-blue-500/20 text-blue-300"
      items={navItems}
      badgePath="/manager/contacts"
      headerExtras={
        isAdmin && (
          <Link
            to="/admin/users"
            className="rounded-lg border border-white/30 px-3 py-1.5 text-sm font-semibold transition hover:bg-white/10"
          >
            Admin
          </Link>
        )
      }
    />
  )
}
