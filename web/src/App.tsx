import { HashRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute, PublicRoute, RootRedirect } from './components/ProtectedRoute';
import { AppLayout } from './components/AppLayout';
import { ThemeProvider } from 'next-themes';

// Public Pages
import { WelcomeScreen } from './pages/WelcomeScreen';
import { LandingPage } from './pages/LandingPage';
import { AllPagesScreen } from './pages/AllPagesScreen';

// Citizen Pages
import { CitizenLogin } from './pages/citizen/Auth/CitizenLogin';
import { CreateAccount } from './pages/citizen/Auth/CreateAccount';
import { ForgotPassword } from './pages/citizen/Auth/ForgotPassword';
import { ResetPassword } from './pages/citizen/Auth/ResetPassword';
import { Onboarding } from './pages/Onboarding';
import { CitizenDashboard } from './pages/citizen/Dashboard/CitizenDashboard';
import { WasteReportScreen } from './pages/citizen/Report/WasteReportScreen';
import { SelectCategoryScreen } from './pages/citizen/Report/SelectCategoryScreen';
import { ConfirmLocationScreen } from './pages/citizen/Report/ConfirmLocationScreen';
import { ReportSubmittedScreen } from './pages/citizen/Report/ReportSubmittedScreen';
import { MapScreen } from './pages/citizen/Map/MapScreen';
import { NearbyIssuesScreen } from './pages/citizen/Map/NearbyIssuesScreen';
import { SettingsScreen } from './pages/citizen/Settings/SettingsScreen';
import { UserProfileScreen } from './pages/citizen/Settings/UserProfileScreen';
import { CitizenEditProfile } from './pages/citizen/Settings/CitizenEditProfile';
import { HelpSupportScreen } from './pages/citizen/Settings/HelpSupportScreen';
import { AboutAppScreen } from './pages/citizen/Settings/AboutAppScreen';
import { CitizenHistory } from './pages/citizen/History/CitizenHistory';
import { CitizenReportDetails } from './pages/citizen/History/CitizenReportDetails';

// Supervisor Pages
import { SupervisorLogin } from './pages/supervisor/Auth/SupervisorLogin';
import { SupervisorDashboard } from './pages/supervisor/Dashboard/SupervisorDashboard';
import { SupervisorHistory } from './pages/supervisor/History/SupervisorHistory';
import { SupervisorProfile } from './pages/supervisor/Settings/SupervisorProfile';
import { SupervisorEditProfile } from './pages/supervisor/Settings/SupervisorEditProfile';
import { SupervisorSettings } from './pages/supervisor/Settings/SupervisorSettings';
import { SupervisorReportDetails } from './pages/supervisor/Report/SupervisorReportDetails';
import { SupervisorReportStatus } from './pages/supervisor/Report/SupervisorReportStatus';
import { SupervisorMap } from './pages/supervisor/Map/SupervisorMap';

// Admin Pages
import { AdminLogin } from './pages/admin/Auth/AdminLogin';
import { AdminDashboard } from './pages/admin/Dashboard/AdminDashboard';
import { AdminReports } from './pages/admin/Management/AdminReports';
import { AdminReportDetails } from './pages/admin/Management/AdminReportDetails';
import { AdminSupervisors } from './pages/admin/Management/AdminSupervisors';
import { AdminAddSupervisor } from './pages/admin/Management/AdminAddSupervisor';
import { AdminSupervisorDetail } from './pages/admin/Management/AdminSupervisorDetail';
import { AdminEditSupervisor } from './pages/admin/Management/AdminEditSupervisor';
import { AdminProfile } from './pages/admin/Settings/AdminProfile';
import { AdminEditProfile } from './pages/admin/Settings/AdminEditProfile';
import { AdminSettings } from './pages/admin/Settings/AdminSettings';
import { AdminChangePassword } from './pages/admin/Settings/AdminChangePassword';
import { AdminPermissions } from './pages/admin/Settings/AdminPermissions';
import { AdminActivityLogs } from './pages/admin/Settings/AdminActivityLogs';

function App() {
  return (
    <ThemeProvider attribute="class" defaultTheme="system" enableSystem>
      <AuthProvider>
        <HashRouter>
          <Routes>
            {/* Public Routes */}
            <Route path="/" element={<RootRedirect />} />
            <Route path="/landing" element={<LandingPage />} />
            <Route path="/welcome" element={<PublicRoute><WelcomeScreen /></PublicRoute>} />
            <Route path="/all-pages" element={<AllPagesScreen />} />
            
            {/* Citizen Auth Public Routes */}
            <Route path="/citizen/login" element={<PublicRoute><CitizenLogin /></PublicRoute>} />
            <Route path="/citizen/signup" element={<PublicRoute><CreateAccount /></PublicRoute>} />
            <Route path="/citizen/forgot-password" element={<PublicRoute><ForgotPassword /></PublicRoute>} />
            <Route path="/citizen/reset-password" element={<PublicRoute><ResetPassword /></PublicRoute>} />
            <Route path="/onboarding" element={<PublicRoute><Onboarding /></PublicRoute>} />

            {/* Supervisor Auth Public Routes */}
            <Route path="/supervisor/login" element={<PublicRoute><SupervisorLogin /></PublicRoute>} />
            <Route path="/supervisor/signup" element={<Navigate to="/" replace />} />

            {/* Admin Auth Public Routes */}
            <Route path="/admin/login" element={<PublicRoute><AdminLogin /></PublicRoute>} />

            {/* Protected Citizen Routes */}
            <Route path="/citizen/dashboard" element={
              <ProtectedRoute allowedRoles={['citizen']}>
                <AppLayout><CitizenDashboard /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/citizen/report" element={
              <ProtectedRoute allowedRoles={['citizen']}>
                <AppLayout><WasteReportScreen /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/citizen/report/category" element={
              <ProtectedRoute allowedRoles={['citizen']}>
                <AppLayout><SelectCategoryScreen /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/citizen/report/location" element={
              <ProtectedRoute allowedRoles={['citizen']}>
                <AppLayout><ConfirmLocationScreen /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/citizen/report/success" element={
              <ProtectedRoute allowedRoles={['citizen']}>
                <AppLayout><ReportSubmittedScreen /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/citizen/map" element={
              <ProtectedRoute allowedRoles={['citizen']}>
                <AppLayout><MapScreen /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/citizen/map/nearby" element={
              <ProtectedRoute allowedRoles={['citizen']}>
                <AppLayout><NearbyIssuesScreen /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/citizen/settings" element={
              <ProtectedRoute allowedRoles={['citizen']}>
                <AppLayout><SettingsScreen /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/citizen/profile" element={
              <ProtectedRoute allowedRoles={['citizen']}>
                <AppLayout><UserProfileScreen /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/citizen/profile/edit" element={
              <ProtectedRoute allowedRoles={['citizen']}>
                <AppLayout><CitizenEditProfile /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/citizen/help" element={
              <ProtectedRoute allowedRoles={['citizen']}>
                <AppLayout><HelpSupportScreen /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/citizen/about" element={
              <ProtectedRoute allowedRoles={['citizen']}>
                <AppLayout><AboutAppScreen /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/citizen/history" element={
              <ProtectedRoute allowedRoles={['citizen']}>
                <AppLayout><CitizenHistory /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/citizen/history/:id" element={
              <ProtectedRoute allowedRoles={['citizen']}>
                <AppLayout><CitizenReportDetails /></AppLayout>
              </ProtectedRoute>
            } />

            {/* Protected Supervisor Routes */}
            <Route path="/supervisor/dashboard" element={
              <ProtectedRoute allowedRoles={['supervisor']}>
                <AppLayout><SupervisorDashboard /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/supervisor/history" element={
              <ProtectedRoute allowedRoles={['supervisor']}>
                <AppLayout><SupervisorHistory /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/supervisor/profile" element={
              <ProtectedRoute allowedRoles={['supervisor']}>
                <AppLayout><SupervisorProfile /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/supervisor/profile/edit" element={
              <ProtectedRoute allowedRoles={['supervisor']}>
                <AppLayout><SupervisorEditProfile /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/supervisor/settings" element={
              <ProtectedRoute allowedRoles={['supervisor']}>
                <AppLayout><SupervisorSettings /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/supervisor/about" element={
              <ProtectedRoute allowedRoles={['supervisor']}>
                <AppLayout><AboutAppScreen /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/supervisor/report/:id" element={
              <ProtectedRoute allowedRoles={['supervisor']}>
                <AppLayout><SupervisorReportDetails /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/supervisor/report/:id/status" element={
              <ProtectedRoute allowedRoles={['supervisor']}>
                <AppLayout><SupervisorReportStatus /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/supervisor/map" element={
              <ProtectedRoute allowedRoles={['supervisor']}>
                <AppLayout><MapScreen /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/supervisor/map/:id" element={
              <ProtectedRoute allowedRoles={['supervisor']}>
                <AppLayout><SupervisorMap /></AppLayout>
              </ProtectedRoute>
            } />

            {/* Protected Admin Routes */}
            <Route path="/admin/dashboard" element={
              <ProtectedRoute allowedRoles={['admin']}>
                <AppLayout><AdminDashboard /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/admin/reports" element={
              <ProtectedRoute allowedRoles={['admin']}>
                <AppLayout><AdminReports /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/admin/report/:id" element={
              <ProtectedRoute allowedRoles={['admin']}>
                <AppLayout><AdminReportDetails /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/admin/supervisors" element={
              <ProtectedRoute allowedRoles={['admin']}>
                <AppLayout><AdminSupervisors /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/admin/supervisors/add" element={
              <ProtectedRoute allowedRoles={['admin']}>
                <AppLayout><AdminAddSupervisor /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/admin/supervisors/:id" element={
              <ProtectedRoute allowedRoles={['admin']}>
                <AppLayout><AdminSupervisorDetail /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/admin/supervisors/:id/edit" element={
              <ProtectedRoute allowedRoles={['admin']}>
                <AppLayout><AdminEditSupervisor /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/admin/profile" element={
              <ProtectedRoute allowedRoles={['admin']}>
                <AppLayout><AdminProfile /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/admin/profile/edit" element={
              <ProtectedRoute allowedRoles={['admin']}>
                <AppLayout><AdminEditProfile /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/admin/settings" element={
              <ProtectedRoute allowedRoles={['admin']}>
                <AppLayout><AdminSettings /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/admin/password" element={
              <ProtectedRoute allowedRoles={['admin']}>
                <AppLayout><AdminChangePassword /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/admin/permissions" element={
              <ProtectedRoute allowedRoles={['admin']}>
                <AppLayout><AdminPermissions /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/admin/logs" element={
              <ProtectedRoute allowedRoles={['admin']}>
                <AppLayout><AdminActivityLogs /></AppLayout>
              </ProtectedRoute>
            } />
            <Route path="/admin/map" element={
              <ProtectedRoute allowedRoles={['admin']}>
                <AppLayout><MapScreen /></AppLayout>
              </ProtectedRoute>
            } />

            {/* Wildcard Fallback */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </HashRouter>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
