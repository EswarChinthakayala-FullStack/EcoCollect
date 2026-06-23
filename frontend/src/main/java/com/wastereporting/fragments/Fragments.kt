package com.wastereporting.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wastereporting.R
import com.wastereporting.pages.*
import com.wastereporting.network.TokenManager

class OnboardingFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                OnboardingScreen(
                    onFinish = { findNavController().navigate(R.id.welcomeBackFragment) },
                    onNavigateToSupervisorLogin = { findNavController().navigate(R.id.supervisorLoginFragment) },
                    onNavigateToAdminLogin = { findNavController().navigate(R.id.adminLoginFragment) }
                )
            }
        }
    }
}

class WelcomeBackFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                WelcomeBackScreen(
                    onLogin = { findNavController().navigate(R.id.homeFragment) },
                    onCreateAccount = { findNavController().navigate(R.id.createAccountFragment) },
                    onNavigateToForgotPassword = { findNavController().navigate(R.id.forgotPasswordFragment) },
                    onBack = { findNavController().popBackStack() }
                )
            }
        }
    }
}

class ForgotPasswordFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ForgotPasswordScreen(
                    onBackToLogin = { findNavController().popBackStack() },
                    onNavigateToReset = { email, otp ->
                        val args = Bundle().apply {
                            putString("email", email)
                            putString("otp", otp)
                        }
                        findNavController().navigate(R.id.resetPasswordFragment, args)
                    }
                )
            }
        }
    }
}

class ResetPasswordFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val email = arguments?.getString("email") ?: ""
        val otp = arguments?.getString("otp") ?: ""
        return ComposeView(requireContext()).apply {
            setContent {
                ResetPasswordScreen(
                    email = email,
                    otp = otp,
                    onBack = { findNavController().popBackStack() },
                    onPasswordResetSuccess = { findNavController().navigate(R.id.welcomeBackFragment) }
                )
            }
        }
    }
}

class CreateAccountFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CreateAccountScreen(
                    onBack = { findNavController().popBackStack() },
                    onContinue = { email ->
                        val args = Bundle().apply {
                            putString("email", email)
                        }
                        findNavController().navigate(R.id.otpVerificationFragment, args)
                    }
                )
            }
        }
    }
}

class OTPVerificationFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val email = arguments?.getString("email") ?: ""
        return ComposeView(requireContext()).apply {
            setContent {
                OTPVerificationScreen(
                    email = email,
                    onBack = { findNavController().popBackStack() },
                    onVerifySuccess = {
                        if (TokenManager.userRole == "supervisor") {
                            findNavController().navigate(R.id.supervisorDashboardFragment)
                        } else {
                            findNavController().navigate(R.id.enableLocationFragment)
                        }
                    }
                )
            }
        }
    }
}

class ProfileSetupFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ProfileSetupScreen(
                    onBack = { findNavController().popBackStack() },
                    onNext = { findNavController().navigate(R.id.enableLocationFragment) }
                )
            }
        }
    }
}

class EnableLocationFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                EnableLocationScreen(
                    onAllow = { findNavController().navigate(R.id.stayUpdatedFragment) },
                    onSkip = { findNavController().navigate(R.id.stayUpdatedFragment) }
                )
            }
        }
    }
}

class StayUpdatedFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                StayUpdatedScreen(
                    onEnable = { findNavController().navigate(R.id.allSetFragment) },
                    onSkip = { findNavController().navigate(R.id.allSetFragment) }
                )
            }
        }
    }
}

class AllSetFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AllSetScreen(
                    onGoToDashboard = { findNavController().navigate(R.id.homeFragment) }
                )
            }
        }
    }
}

class HomeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                HomeDashboardScreen(
                    onNavigateToReport = { findNavController().navigate(R.id.reportFragment) },
                    onNavigateToHistory = { findNavController().navigate(R.id.historyFragment) },
                    onNavigateToNotifications = { findNavController().navigate(R.id.notificationsFragment) },
                    onLogout = {
                        TokenManager.clearToken()
                        findNavController().navigate(R.id.welcomeBackFragment)
                    },
                    onNavigateToCollectionStatus = { findNavController().navigate(R.id.collectionStatusFragment) },
                    onNavigateToReportDetails = { reportId ->
                        val args = Bundle().apply { putInt("reportId", reportId) }
                        findNavController().navigate(R.id.citizenReportDetailsFragment, args)
                    }
                )
            }
        }
    }
}

class ReportFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                WasteReportScreen(
                    onImageSelected = { findNavController().navigate(R.id.selectCategoryFragment) },
                    onBack = { findNavController().popBackStack() }
                )
            }
        }
    }
}

class ProfileFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                UserProfileScreen(
                    onNavigateToHelp = { findNavController().navigate(R.id.helpSupportFragment) },
                    onNavigateToAbout = { findNavController().navigate(R.id.aboutAppFragment) },
                    onNavigateToEditProfile = { findNavController().navigate(R.id.editProfileFragment) },
                    onLogout = {
                        TokenManager.clearToken()
                        findNavController().navigate(R.id.welcomeBackFragment)
                    }
                )
            }
        }
    }
}

class EditProfileFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                EditProfileScreen(
                    onBack = { findNavController().popBackStack() },
                    onSave = { findNavController().popBackStack() }
                )
            }
        }
    }
}

class HelpSupportFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                HelpSupportScreen(
                    onBack = { findNavController().popBackStack() },
                    onNavigateToChat = { findNavController().navigate(R.id.chatSupportFragment) },
                    onNavigateToCall = { findNavController().navigate(R.id.contactSupportFragment) },
                    onNavigateToFaq = { findNavController().navigate(R.id.faqDetailsFragment) },
                    onNavigateToEmail = { findNavController().navigate(R.id.emailSupportFragment) }
                )
            }
        }
    }
}

class ChatSupportFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ChatSupportScreen(
                    onBack = { findNavController().popBackStack() }
                )
            }
        }
    }
}

class ContactSupportFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ContactSupportScreen(
                    onBack = { findNavController().popBackStack() }
                )
            }
        }
    }
}

class FAQDetailsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                FAQDetailsScreen(
                    onBack = { findNavController().popBackStack() },
                    onNeedMoreHelp = { findNavController().navigate(R.id.contactSupportFragment) }
                )
            }
        }
    }
}

class EmailSupportFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                EmailSupportScreen(
                    onBack = { findNavController().popBackStack() },
                    onSubmit = { findNavController().navigate(R.id.emailSubmittedFragment) }
                )
            }
        }
    }
}

class EmailSubmittedFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                EmailSubmittedScreen(
                    onBackToHelp = { findNavController().navigate(R.id.helpSupportFragment) }
                )
            }
        }
    }
}

class AboutAppFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AboutAppScreen(
                    onBack = { findNavController().popBackStack() }
                )
            }
        }
    }
}

class HistoryFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ReportHistoryScreen(
                    onBack = { findNavController().popBackStack() },
                    onNavigateToReportDetails = { reportId ->
                        val args = Bundle().apply { putInt("reportId", reportId) }
                        findNavController().navigate(R.id.citizenReportDetailsFragment, args)
                    }
                )
            }
        }
    }
}

class CitizenReportDetailsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val reportId = arguments?.getInt("reportId") ?: 1
        return ComposeView(requireContext()).apply {
            setContent {
                CitizenReportDetailsScreen(
                    reportId = reportId,
                    onBack = { findNavController().popBackStack() },
                    onTrackLive = { id ->
                        val args = Bundle().apply {
                            putInt("reportId", id)
                        }
                        findNavController().navigate(R.id.liveTrackingFragment, args)
                    }
                )
            }
        }
    }
}

class SelectCategoryFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SelectCategoryScreen(
                    onBack = { findNavController().popBackStack() },
                    onContinue = { findNavController().navigate(R.id.confirmLocationFragment) }
                )
            }
        }
    }
}

class ConfirmLocationFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ConfirmLocationScreen(
                    onBack = { findNavController().popBackStack() },
                    onConfirm = { findNavController().navigate(R.id.reviewReportFragment) }
                )
            }
        }
    }
}

class ReviewReportFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ReviewReportScreen(
                    onBack = { findNavController().popBackStack() },
                    onSubmit = { findNavController().navigate(R.id.reportSuccessFragment) },
                    onEditLocation = { findNavController().navigate(R.id.confirmLocationFragment) },
                    onEditCategory = { findNavController().navigate(R.id.selectCategoryFragment) }
                )
            }
        }
    }
}

class ReportSuccessFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ReportSubmittedScreen(
                    onBackToDashboard = { findNavController().navigate(R.id.homeFragment) }
                )
            }
        }
    }
}

class CameraCaptureFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CameraCaptureScreen(
                    onCapture = { findNavController().navigate(R.id.selectCategoryFragment) },
                    onClose = { findNavController().navigate(R.id.reportFragment) }
                )
            }
        }
    }
}

class CollectionStatusFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CollectionStatusScreen(
                    onBack = { findNavController().popBackStack() },
                    onTrackLive = { reportId ->
                        val args = Bundle().apply {
                            putInt("reportId", reportId)
                        }
                        findNavController().navigate(R.id.liveTrackingFragment, args)
                    }
                )
            }
        }
    }
}

class LiveTrackingFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val reportId = arguments?.getInt("reportId") ?: 1
        return ComposeView(requireContext()).apply {
            setContent {
                LiveTrackingScreen(
                    reportId = reportId,
                    onBack = { findNavController().popBackStack() },
                    onDetails = { findNavController().navigate(R.id.collectionSummaryFragment) }
                )
            }
        }
    }
}

class CollectionSummaryFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CollectionSummaryScreen(
                    onBack = { findNavController().popBackStack() },
                    onReturnToDashboard = { findNavController().navigate(R.id.homeFragment) }
                )
            }
        }
    }
}

class AdminLoginFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AdminLoginScreen(
                    onLoginSuccess = { findNavController().navigate(R.id.adminDashboardFragment) },
                    onBack = { findNavController().popBackStack() }
                )
            }
        }
    }
}

class AdminDashboardFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AdminDashboardScreen(
                    onNavigateToReports = { findNavController().navigate(R.id.adminReportsFragment) },
                    onNavigateToProfile = { findNavController().navigate(R.id.adminProfileFragment) },
                    onNavigateToSupervisors = { findNavController().navigate(R.id.adminSupervisorsFragment) },
                    onNavigateToReportDetails = { reportId ->
                        val args = Bundle().apply { putInt("reportId", reportId) }
                        findNavController().navigate(R.id.adminReportDetailsFragment, args)
                    },
                    onNavigateToNotifications = { findNavController().navigate(R.id.notificationsFragment) }
                )
            }
        }
    }
}

class AdminReportsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AdminReportsScreen(
                    onNavigateToDashboard = { findNavController().navigate(R.id.adminDashboardFragment) },
                    onNavigateToProfile = { findNavController().navigate(R.id.adminProfileFragment) },
                    onNavigateToSupervisors = { findNavController().navigate(R.id.adminSupervisorsFragment) },
                    onNavigateToReportDetails = { reportId ->
                        val args = Bundle().apply { putInt("reportId", reportId) }
                        findNavController().navigate(R.id.adminReportDetailsFragment, args)
                    }
                )
            }
        }
    }
}

class AdminReportDetailsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val reportId = arguments?.getInt("reportId") ?: 1
        return ComposeView(requireContext()).apply {
            setContent {
                AdminReportDetailsScreen(
                    reportId = reportId,
                    onBack = { findNavController().popBackStack() }
                )
            }
        }
    }
}

class AdminSupervisorsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AdminSupervisorsScreen(
                    onNavigateToDashboard = { findNavController().navigate(R.id.adminDashboardFragment) },
                    onNavigateToReports = { findNavController().navigate(R.id.adminReportsFragment) },
                    onNavigateToProfile = { findNavController().navigate(R.id.adminProfileFragment) },
                    onBack = { findNavController().popBackStack() },
                    onNavigateToAddSupervisor = { findNavController().navigate(R.id.adminAddSupervisorFragment) },
                    onNavigateToSupervisorDetails = { id ->
                        val args = Bundle().apply { putInt("supervisorId", id) }
                        findNavController().navigate(R.id.adminSupervisorDetailsFragment, args)
                    }
                )
            }
        }
    }
}

class AdminAddSupervisorFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AdminAddSupervisorScreen(
                    onBack = { findNavController().popBackStack() },
                    onSupervisorAdded = { findNavController().popBackStack() }
                )
            }
        }
    }
}

class AdminSupervisorDetailsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val supervisorId = arguments?.getInt("supervisorId") ?: 1
        return ComposeView(requireContext()).apply {
            setContent {
                AdminSupervisorDetailsScreen(
                    supervisorId = supervisorId,
                    onBack = { findNavController().popBackStack() },
                    onNavigateToEditSupervisor = { id ->
                        val args = Bundle().apply { putInt("supervisorId", id) }
                        findNavController().navigate(R.id.adminEditSupervisorFragment, args)
                    }
                )
            }
        }
    }
}

class AdminEditSupervisorFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val supervisorId = arguments?.getInt("supervisorId") ?: 1
        return ComposeView(requireContext()).apply {
            setContent {
                AdminEditSupervisorScreen(
                    supervisorId = supervisorId,
                    onBack = { findNavController().popBackStack() },
                    onSupervisorUpdated = { findNavController().popBackStack() }
                )
            }
        }
    }
}

class AdminProfileFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AdminProfileScreen(
                    onNavigateToDashboard = { findNavController().navigate(R.id.adminDashboardFragment) },
                    onNavigateToReports = { findNavController().navigate(R.id.adminReportsFragment) },
                    onNavigateToSupervisors = { findNavController().navigate(R.id.adminSupervisorsFragment) },
                    onNavigateToSettings = { findNavController().navigate(R.id.adminSettingsFragment) },
                    onNavigateToEditProfile = { findNavController().navigate(R.id.adminEditProfileFragment) },
                    onLogout = {
                        TokenManager.clearToken()
                        findNavController().navigate(R.id.adminLoginFragment)
                    }
                )
            }
        }
    }
}

class AdminEditProfileFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AdminEditProfileScreen(
                    onBack = { findNavController().popBackStack() }
                )
            }
        }
    }
}

class AdminSettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AdminSettingsScreen(
                    onBack = { findNavController().popBackStack() }
                )
            }
        }
    }
}

class SupervisorLoginFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SupervisorLoginScreen(
                    onBack = { findNavController().popBackStack() },
                    onLoginSuccess = { findNavController().navigate(R.id.supervisorDashboardFragment) }
                )
            }
        }
    }
}

class SupervisorDashboardFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SupervisorDashboardScreen(
                    onNavigateToPendingReports = { findNavController().navigate(R.id.supervisorPendingReportsFragment) },
                    onNavigateToCompletedReports = { findNavController().navigate(R.id.supervisorCompletedReportsFragment) },
                    onNavigateToProfile = { findNavController().navigate(R.id.supervisorProfileFragment) },
                    onNavigateToReportDetails = { reportId ->
                        val args = Bundle().apply { putInt("reportId", reportId) }
                        findNavController().navigate(R.id.supervisorReportDetailsFragment, args)
                    },
                    onNavigateToNotifications = { findNavController().navigate(R.id.notificationsFragment) }
                )
            }
        }
    }
}

class SupervisorPendingReportsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SupervisorPendingReportsScreen(
                    onNavigateToDashboard = { findNavController().navigate(R.id.supervisorDashboardFragment) },
                    onNavigateToProfile = { findNavController().navigate(R.id.supervisorProfileFragment) },
                    onNavigateToReportDetails = { reportId ->
                        val args = Bundle().apply { putInt("reportId", reportId) }
                        findNavController().navigate(R.id.supervisorReportDetailsFragment, args)
                    },
                    onBack = { findNavController().popBackStack() }
                )
            }
        }
    }
}

class SupervisorCompletedReportsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SupervisorCompletedReportsScreen(
                    onNavigateToDashboard = { findNavController().navigate(R.id.supervisorDashboardFragment) },
                    onNavigateToProfile = { findNavController().navigate(R.id.supervisorProfileFragment) },
                    onNavigateToReportDetails = { reportId ->
                        val args = Bundle().apply { putInt("reportId", reportId) }
                        findNavController().navigate(R.id.supervisorReportDetailsFragment, args)
                    },
                    onBack = { findNavController().popBackStack() }
                )
            }
        }
    }
}

class SupervisorProfileFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SupervisorProfileScreen(
                    onNavigateToDashboard = { findNavController().navigate(R.id.supervisorDashboardFragment) },
                    onNavigateToHistory = { findNavController().navigate(R.id.supervisorCompletedReportsFragment) },
                    onNavigateToAbout = { findNavController().navigate(R.id.aboutAppFragment) },
                    onLogout = {
                        TokenManager.clearToken()
                        findNavController().navigate(R.id.supervisorLoginFragment)
                    }
                )
            }
        }
    }
}

class SupervisorEditProfileFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SupervisorEditProfileScreen(
                    onBack = { findNavController().popBackStack() }
                )
            }
        }
    }
}

class SupervisorReportDetailsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val reportId = arguments?.getInt("reportId") ?: 1
        return ComposeView(requireContext()).apply {
            setContent {
                SupervisorReportDetailsScreen(
                    reportId = reportId,
                    onBack = { findNavController().popBackStack() },
                    onNavigateToCompletedReports = { findNavController().navigate(R.id.supervisorCompletedReportsFragment) }
                )
            }
        }
    }
}

class SupervisorSettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SupervisorSettingsScreen(
                    onBack = { findNavController().popBackStack() }
                )
            }
        }
    }
}

class NotificationsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                NotificationsScreen(
                    onBack = { findNavController().popBackStack() }
                )
            }
        }
    }
}
