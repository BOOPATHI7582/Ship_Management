import { Route, Routes } from 'react-router-dom'
import PublicLayout from './layouts/PublicLayout'
import HomePage from './pages/public/HomePage'
import AboutPage from './pages/public/AboutPage'
import ServicesPage from './pages/public/ServicesPage'
import CargoPage from './pages/public/CargoPage'
import ShipmentsPage from './pages/public/ShipmentsPage'
import TrackingPage from './pages/public/TrackingPage'
import ContactPage from './pages/public/ContactPage'
import CareersPage from './pages/public/CareersPage'
import PublicQuotationPage from './pages/public/PublicQuotationPage'
import LoginPage from './pages/auth/LoginPage'
import VerifyLoginOtpPage from './pages/auth/VerifyLoginOtpPage'
import RegisterPage from './pages/auth/RegisterPage'
import ForgotPasswordPage from './pages/auth/ForgotPasswordPage'
import ResetPasswordPage from './pages/auth/ResetPasswordPage'
import VerifyEmailPage from './pages/auth/VerifyEmailPage'
import ClientLayout from './layouts/ClientLayout'
import AdminLayout from './layouts/AdminLayout'
import ManagerLayout from './layouts/ManagerLayout'
import EnquiryQueuePage from './pages/manager/EnquiryQueuePage'
import ManagerEnquiryDetailPage from './pages/manager/EnquiryDetailPage'
import ProformaPage from './pages/manager/ProformaPage'
import ManagerInvoicesPage from './pages/manager/InvoicesPage'
import ManagerReceiptsPage from './pages/manager/ReceiptsPage'
import ManagerShipmentsPage from './pages/manager/ShipmentsPage'
import ManagerDocumentsPage from './pages/manager/DocumentsPage'
import ManagerReviewsPage from './pages/manager/ReviewsPage'
import ManagerReportsPage from './pages/manager/ReportsPage'
import ManagerAuditPage from './pages/manager/AuditPage'
import ContactsPage from './pages/manager/ContactsPage'
import DashboardHome from './pages/client/DashboardHome'
import NewEnquiryPage from './pages/client/NewEnquiryPage'
import MyEnquiriesPage from './pages/client/MyEnquiriesPage'
import EnquiryDetailPage from './pages/client/EnquiryDetailPage'
import ProformaInvoicesPage from './pages/client/ProformaInvoicesPage'
import ClientInvoicesPage from './pages/client/InvoicesPage'
import ReceiptsPage from './pages/client/ReceiptsPage'
import ClientShipmentsPage from './pages/client/ShipmentsPage'
import ClientDocumentsPage from './pages/client/DocumentsPage'
import NotificationsPage from './pages/client/NotificationsPage'
import MyMessagesPage from './pages/client/MyMessagesPage'
import ProfilePage from './pages/client/ProfilePage'
import UsersPage from './pages/admin/UsersPage'
import AdminDashboardPage from './pages/admin/DashboardPage'
import ClientsPage from './pages/admin/ClientsPage'
import VesselsPage from './pages/admin/VesselsPage'
import CargoAdminPage from './pages/admin/CargoAdminPage'
import CategoriesPage from './pages/admin/CategoriesPage'
import TaxesPage from './pages/admin/TaxesPage'
import PortsPage from './pages/admin/PortsPage'
import ProtectedRoute from './components/ProtectedRoute'
import NotFoundPage from './pages/NotFoundPage'

function ClientRoutes() {
  return (
    <Routes>
      <Route element={<ClientLayout />}>
        <Route index element={<DashboardHome />} />
        <Route path="enquiries/new" element={<NewEnquiryPage />} />
        <Route path="enquiries" element={<MyEnquiriesPage />} />
        <Route path="enquiries/:id" element={<EnquiryDetailPage />} />
        <Route path="proforma-invoices" element={<ProformaInvoicesPage />} />
        <Route path="invoices" element={<ClientInvoicesPage />} />
        <Route path="receipts" element={<ReceiptsPage />} />
        <Route path="shipments" element={<ClientShipmentsPage />} />
        <Route path="documents" element={<ClientDocumentsPage />} />
        <Route path="shipments/:id" element={<ClientShipmentsPage />} />
        <Route path="notifications" element={<NotificationsPage />} />
        <Route path="messages" element={<MyMessagesPage />} />
        <Route path="profile" element={<ProfilePage />} />
      </Route>
    </Routes>
  )
}

function AdminRoutes() {
  return (
    <Routes>
      <Route element={<AdminLayout />}>
        <Route index element={<AdminDashboardPage />} />
        <Route path="dashboard" element={<AdminDashboardPage />} />
        <Route path="users" element={<UsersPage />} />
        <Route path="clients" element={<ClientsPage />} />
        <Route path="vessels" element={<VesselsPage />} />
        <Route path="cargo" element={<CargoAdminPage />} />
        <Route path="categories" element={<CategoriesPage />} />
        <Route path="taxes" element={<TaxesPage />} />
        <Route path="ports" element={<PortsPage />} />
      </Route>
    </Routes>
  )
}

function ManagerRoutes() {
  return (
    <Routes>
      <Route element={<ManagerLayout />}>
        <Route index element={<EnquiryQueuePage />} />
        <Route path="contacts" element={<ContactsPage />} />
        <Route path="enquiries" element={<EnquiryQueuePage />} />
        <Route path="enquiries/:id" element={<ManagerEnquiryDetailPage />} />
        <Route path="proforma" element={<ProformaPage />} />
        <Route path="invoices" element={<ManagerInvoicesPage />} />
        <Route path="receipts" element={<ManagerReceiptsPage />} />
        <Route path="shipments" element={<ManagerShipmentsPage />} />
        <Route path="documents" element={<ManagerDocumentsPage />} />
        <Route path="reviews" element={<ManagerReviewsPage />} />
        <Route path="reports" element={<ManagerReportsPage />} />
        <Route path="audit" element={<ManagerAuditPage />} />
      </Route>
    </Routes>
  )
}

export default function App() {
  return (
    <Routes>
      <Route element={<PublicLayout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/about" element={<AboutPage />} />
        <Route path="/services" element={<ServicesPage />} />
        <Route path="/cargo" element={<CargoPage />} />
        <Route path="/shipments" element={<ShipmentsPage />} />
        <Route path="/tracking" element={<TrackingPage />} />
        <Route path="/contact" element={<ContactPage />} />
        <Route path="/careers" element={<CareersPage />} />
        <Route path="/quotation/:secureToken" element={<PublicQuotationPage />} />
      </Route>

      <Route path="/login" element={<LoginPage />} />
      <Route path="/login/otp" element={<VerifyLoginOtpPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/verify-email" element={<VerifyEmailPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />

      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <ClientRoutes />
          </ProtectedRoute>
        }
      />

      <Route
        path="/client/*"
        element={
          <ProtectedRoute>
            <ClientRoutes />
          </ProtectedRoute>
        }
      />

      <Route
        path="/admin/*"
        element={
          <ProtectedRoute roles={['ADMIN']}>
            <AdminRoutes />
          </ProtectedRoute>
        }
      />

      <Route
        path="/manager/*"
        element={
          <ProtectedRoute roles={['ADMIN', 'SHIP_MANAGER']}>
            <ManagerRoutes />
          </ProtectedRoute>
        }
      />

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}
