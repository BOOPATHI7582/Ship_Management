import { NavLink } from 'react-router-dom'
import DashboardShell from '../components/DashboardShell'

const navItems = [
  { to: '/client', label: 'Dashboard', end: true },
  { to: '/client/enquiries/new', label: 'New Enquiry' },
  { to: '/client/enquiries', label: 'My Enquiries' },
  { to: '/client/messages', label: 'My Messages' },
  { to: '/client/proforma-invoices', label: 'Proforma Invoices' },
  { to: '/client/invoices', label: 'Tax Invoices' },
  { to: '/client/receipts', label: 'Receipts' },
  { to: '/client/shipments', label: 'Shipments' },
  { to: '/client/documents', label: 'Documents' },
  { to: '/client/notifications', label: 'Notifications' },
  { to: '/client/profile', label: 'Profile' },
]

export default function ClientLayout() {
  return (
    <DashboardShell homeTo="/client" items={navItems} />
  )
}
