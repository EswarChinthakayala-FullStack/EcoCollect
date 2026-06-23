package com.wastereporting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.History

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.wastereporting.pages.*

enum class Screen {
    AboutApp, ActiveNavigation, AdminAddSupervisor, AdminDashboard, AdminEditProfile, AdminLogin, AdminProfile, AdminReportDetails, AdminReports, AdminSettings, AdminSupervisors, AdminSupervisorDetails, AdminEditSupervisor, AllSet, Analytics, CameraCapture, ChatSupport, CitizenReportDetails, CollectionStatus, CollectionSummary, ConfirmLocation, ContactSupport, CreateAccount, DailyStatistics, EditProfile, EmailSubmitted, EmailSupport, EnableLocation, EnvironmentalImpact, FAQDetails, ForgotPassword, HelpSupport, History, Home, IssueHeatmap, LiveTracking, Map, MonthlyReports, NearbyIssues, Notifications, OTPVerification, Onboarding, Profile, ProfileSetup, RecyclingBreakdown, Report, ReportSuccess, ResetPassword, ReviewReport, SelectCategory, SmartInsights, StayUpdated, SupervisorDashboard, SupervisorEditProfile, SupervisorLogin, SupervisorPendingReports, SupervisorProfile, SupervisorReportDetails, SupervisorReportStatus, SupervisorCompletedReports, SupervisorSettings, WelcomeBack
}

fun isAdminScreen(screen: Screen): Boolean {
    return when (screen) {
        Screen.AdminDashboard,
        Screen.AdminReports,
        Screen.AdminReportDetails,
        Screen.AdminSupervisors,
        Screen.AdminAddSupervisor,
        Screen.AdminSupervisorDetails,
        Screen.AdminEditSupervisor,
        Screen.AdminProfile,
        Screen.AdminEditProfile,
        Screen.AdminSettings -> true
        else -> false
    }
}

fun isSupervisorScreen(screen: Screen): Boolean {
    return when (screen) {
        Screen.SupervisorDashboard,
        Screen.SupervisorPendingReports,
        Screen.SupervisorCompletedReports,
        Screen.SupervisorProfile,
        Screen.SupervisorEditProfile,
        Screen.SupervisorReportDetails,
        Screen.SupervisorReportStatus,
        Screen.SupervisorSettings -> true
        else -> false
    }
}

fun isCitizenScreen(screen: Screen): Boolean {
    val isPublic = when (screen) {
        Screen.Onboarding,
        Screen.WelcomeBack,
        Screen.AdminLogin,
        Screen.SupervisorLogin,
        Screen.CreateAccount,
        Screen.ForgotPassword,
        Screen.ResetPassword,
        Screen.OTPVerification -> true
        else -> false
    }
    return !isPublic && !isAdminScreen(screen) && !isSupervisorScreen(screen) && 
            screen != Screen.Notifications && screen != Screen.AboutApp
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    MaterialTheme {
        val isLoggedIn by com.wastereporting.network.TokenManager.isLoggedInFlow.collectAsState()
        var currentScreen by remember { 
            mutableStateOf(
                if (com.wastereporting.network.TokenManager.isLoggedIn()) {
                    when (com.wastereporting.network.TokenManager.userRole) {
                        "admin" -> Screen.AdminDashboard
                        "supervisor" -> Screen.SupervisorDashboard
                        else -> Screen.Home
                    }
                } else {
                    Screen.Onboarding
                }
            ) 
        }
        var registeredEmail by remember { mutableStateOf("") }
        var registeredOtp by remember { mutableStateOf("") }
        var selectedReportId by remember { mutableStateOf<Int?>(null) }
        var selectedSupervisorId by remember { mutableStateOf<Int?>(null) }
        var supervisorFilter by remember { mutableStateOf("resolved") }
        var detailsBackScreen by remember { mutableStateOf(Screen.History) }
        var trackingReportId by remember { mutableStateOf(1) }
        var trackingBackScreen by remember { mutableStateOf(Screen.CollectionStatus) }

        LaunchedEffect(currentScreen, isLoggedIn) {
            val loggedIn = isLoggedIn
            val role = com.wastereporting.network.TokenManager.userRole

            if (!loggedIn) {
                val isPublic = when (currentScreen) {
                    Screen.Onboarding,
                    Screen.WelcomeBack,
                    Screen.AdminLogin,
                    Screen.SupervisorLogin,
                    Screen.CreateAccount,
                    Screen.ForgotPassword,
                    Screen.ResetPassword,
                    Screen.OTPVerification -> true
                    else -> false
                }
                if (!isPublic) {
                    currentScreen = when {
                        isAdminScreen(currentScreen) -> Screen.AdminLogin
                        isSupervisorScreen(currentScreen) -> Screen.SupervisorLogin
                        else -> Screen.WelcomeBack
                    }
                }
            } else {
                val isPublicAuthScreen = when (currentScreen) {
                    Screen.Onboarding,
                    Screen.WelcomeBack,
                    Screen.AdminLogin,
                    Screen.SupervisorLogin,
                    Screen.CreateAccount,
                    Screen.ForgotPassword,
                    Screen.ResetPassword,
                    Screen.OTPVerification -> true
                    else -> false
                }
                if (isPublicAuthScreen) {
                    currentScreen = when (role) {
                        "admin" -> Screen.AdminDashboard
                        "supervisor" -> Screen.SupervisorDashboard
                        else -> Screen.Home
                    }
                } else {
                    if (isAdminScreen(currentScreen) && role != "admin") {
                        currentScreen = Screen.AdminLogin
                    } else if (isSupervisorScreen(currentScreen) && role != "supervisor") {
                        currentScreen = Screen.SupervisorLogin
                    } else if (isCitizenScreen(currentScreen) && (role == "admin" || role == "supervisor")) {
                        currentScreen = when (role) {
                            "admin" -> Screen.AdminDashboard
                            "supervisor" -> Screen.SupervisorDashboard
                            else -> Screen.Home
                        }
                    }
                }
            }
        }
        
        val isMainTab = currentScreen == Screen.Home || 
                        currentScreen == Screen.History || 
                        currentScreen == Screen.Report || 
                        currentScreen == Screen.Profile

        Scaffold(
            // We removed the global topBar because each screen has its own custom 
            // designed header to match the mockups perfectly.
            bottomBar = {
                if (isMainTab) {
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home") },
                            selected = currentScreen == Screen.Home,
                            onClick = { currentScreen = Screen.Home }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.History, contentDescription = "History") },
                            label = { Text("History") },
                            selected = currentScreen == Screen.History,
                            onClick = { currentScreen = Screen.History }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Add, contentDescription = "Report") },
                            label = { Text("Report") },
                            selected = currentScreen == Screen.Report,
                            onClick = { currentScreen = Screen.Report }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("Profile") },
                            selected = currentScreen == Screen.Profile,
                            onClick = { currentScreen = Screen.Profile }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    Screen.Onboarding -> OnboardingScreen(
                        onFinish = { currentScreen = Screen.WelcomeBack },
                        onNavigateToSupervisorLogin = { currentScreen = Screen.SupervisorLogin },
                        onNavigateToAdminLogin = { currentScreen = Screen.AdminLogin }
                    )
                    Screen.WelcomeBack -> WelcomeBackScreen(
                        onLogin = { currentScreen = Screen.Home },
                        onCreateAccount = { currentScreen = Screen.CreateAccount },
                        onNavigateToForgotPassword = { currentScreen = Screen.ForgotPassword },
                        onBack = { currentScreen = Screen.Onboarding }
                    )
                    Screen.ForgotPassword -> ForgotPasswordScreen(
                        onBackToLogin = { currentScreen = Screen.WelcomeBack },
                        onNavigateToReset = { email, otp -> 
                            registeredEmail = email
                            registeredOtp = otp
                            currentScreen = Screen.ResetPassword 
                        }
                    )
                    Screen.ResetPassword -> ResetPasswordScreen(
                        email = registeredEmail,
                        otp = registeredOtp,
                        onBack = { currentScreen = Screen.ForgotPassword },
                        onPasswordResetSuccess = { currentScreen = Screen.WelcomeBack }
                    )
                    Screen.EditProfile -> EditProfileScreen(
                        onBack = { currentScreen = Screen.Profile },
                        onSave = { currentScreen = Screen.Profile }
                    )
                    Screen.CreateAccount -> CreateAccountScreen(
                        onBack = { currentScreen = Screen.WelcomeBack },
                        onContinue = { email -> 
                            registeredEmail = email
                            currentScreen = Screen.OTPVerification 
                        }
                    )
                    Screen.OTPVerification -> OTPVerificationScreen(
                        email = registeredEmail,
                        onBack = { currentScreen = Screen.CreateAccount },
                        onVerifySuccess = { 
                            currentScreen = if (com.wastereporting.network.TokenManager.userRole == "supervisor") {
                                Screen.SupervisorDashboard
                            } else {
                                Screen.EnableLocation 
                            }
                        }
                    )
                    Screen.ProfileSetup -> ProfileSetupScreen(
                        onBack = { currentScreen = Screen.CreateAccount },
                        onNext = { currentScreen = Screen.EnableLocation }
                    )
                    Screen.EnableLocation -> EnableLocationScreen(
                        onAllow = { currentScreen = Screen.StayUpdated },
                        onSkip = { currentScreen = Screen.StayUpdated }
                    )
                    Screen.StayUpdated -> StayUpdatedScreen(
                        onEnable = { currentScreen = Screen.AllSet },
                        onSkip = { currentScreen = Screen.AllSet }
                    )
                    Screen.AllSet -> AllSetScreen(
                        onGoToDashboard = { currentScreen = Screen.Home }
                    )
                    Screen.Home -> HomeDashboardScreen(
                        onNavigateToReport = { currentScreen = Screen.Report },
                        onNavigateToHistory = { currentScreen = Screen.History },
                        onNavigateToNotifications = { currentScreen = Screen.Notifications },
                        onLogout = { 
                            com.wastereporting.network.TokenManager.clearToken()
                            currentScreen = Screen.WelcomeBack 
                        },
                        onNavigateToCollectionStatus = { currentScreen = Screen.CollectionStatus },
                        onNavigateToReportDetails = { reportId ->
                            selectedReportId = reportId
                            detailsBackScreen = Screen.Home
                            currentScreen = Screen.CitizenReportDetails
                        }
                    )
                    Screen.Report -> WasteReportScreen(
                        onImageSelected = { currentScreen = Screen.SelectCategory },
                        onBack = { currentScreen = Screen.Home }
                    )
                    Screen.Map -> MapScreen()
                    Screen.History -> ReportHistoryScreen(
                        onBack = { currentScreen = Screen.Home },
                        onNavigateToReportDetails = { reportId ->
                            selectedReportId = reportId
                            detailsBackScreen = Screen.History
                            currentScreen = Screen.CitizenReportDetails
                        }
                    )
                    Screen.CitizenReportDetails -> CitizenReportDetailsScreen(
                        reportId = selectedReportId ?: 1,
                        onBack = { currentScreen = detailsBackScreen },
                        onTrackLive = { id ->
                            trackingReportId = id
                            trackingBackScreen = Screen.CitizenReportDetails
                            currentScreen = Screen.LiveTracking
                        }
                    )
                    Screen.Analytics -> AnalyticsScreen(
                        onNavigateToDaily = { currentScreen = Screen.DailyStatistics },
                        onNavigateToMonthly = { currentScreen = Screen.MonthlyReports },
                        onNavigateToRecycling = { currentScreen = Screen.RecyclingBreakdown },
                        onNavigateToInsights = { currentScreen = Screen.SmartInsights },
                        onNavigateToImpact = { currentScreen = Screen.EnvironmentalImpact }
                    )
                    Screen.Profile -> UserProfileScreen(
                        onNavigateToHelp = { currentScreen = Screen.HelpSupport },
                        onNavigateToAbout = { currentScreen = Screen.AboutApp },
                        onNavigateToEditProfile = { currentScreen = Screen.EditProfile },
                        onLogout = { 
                            com.wastereporting.network.TokenManager.clearToken()
                            currentScreen = Screen.WelcomeBack 
                        }
                    )
                    Screen.AdminDashboard -> AdminDashboardScreen(
                        onNavigateToReports = { currentScreen = Screen.AdminReports },
                        onNavigateToProfile = { currentScreen = Screen.AdminProfile },
                        onNavigateToSupervisors = { currentScreen = Screen.AdminSupervisors },
                        onNavigateToReportDetails = { reportId -> 
                            selectedReportId = reportId
                            currentScreen = Screen.AdminReportDetails 
                        },
                        onNavigateToNotifications = { currentScreen = Screen.Notifications }
                    )
                    Screen.AdminLogin -> AdminLoginScreen(
                        onLoginSuccess = { currentScreen = Screen.AdminDashboard },
                        onBack = { currentScreen = Screen.Onboarding }
                    )
                    Screen.AdminReports -> AdminReportsScreen(
                        onNavigateToDashboard = { currentScreen = Screen.AdminDashboard },
                        onNavigateToProfile = { currentScreen = Screen.AdminProfile },
                        onNavigateToSupervisors = { currentScreen = Screen.AdminSupervisors },
                        onNavigateToReportDetails = { reportId -> 
                            selectedReportId = reportId
                            currentScreen = Screen.AdminReportDetails 
                        }
                    )
                    Screen.AdminReportDetails -> AdminReportDetailsScreen(
                        reportId = selectedReportId ?: 1,
                        onBack = { currentScreen = Screen.AdminReports }
                    )
                    Screen.AdminSupervisors -> AdminSupervisorsScreen(
                        onNavigateToDashboard = { currentScreen = Screen.AdminDashboard },
                        onNavigateToReports = { currentScreen = Screen.AdminReports },
                        onNavigateToProfile = { currentScreen = Screen.AdminProfile },
                        onBack = { currentScreen = Screen.AdminDashboard },
                        onNavigateToAddSupervisor = { currentScreen = Screen.AdminAddSupervisor },
                        onNavigateToSupervisorDetails = { id ->
                            selectedSupervisorId = id
                            currentScreen = Screen.AdminSupervisorDetails
                        }
                    )
                    Screen.AdminAddSupervisor -> AdminAddSupervisorScreen(
                        onBack = { currentScreen = Screen.AdminSupervisors },
                        onSupervisorAdded = { currentScreen = Screen.AdminSupervisors }
                    )
                    Screen.AdminSupervisorDetails -> AdminSupervisorDetailsScreen(
                        supervisorId = selectedSupervisorId ?: 1,
                        onBack = { currentScreen = Screen.AdminSupervisors },
                        onNavigateToEditSupervisor = { id ->
                            selectedSupervisorId = id
                            currentScreen = Screen.AdminEditSupervisor
                        }
                    )
                    Screen.AdminEditSupervisor -> AdminEditSupervisorScreen(
                        supervisorId = selectedSupervisorId ?: 1,
                        onBack = { currentScreen = Screen.AdminSupervisorDetails },
                        onSupervisorUpdated = { currentScreen = Screen.AdminSupervisorDetails }
                    )
                    Screen.AdminProfile -> AdminProfileScreen(
                        onNavigateToDashboard = { currentScreen = Screen.AdminDashboard },
                        onNavigateToReports = { currentScreen = Screen.AdminReports },
                        onNavigateToSupervisors = { currentScreen = Screen.AdminSupervisors },
                        onNavigateToSettings = { currentScreen = Screen.AdminSettings },
                        onNavigateToEditProfile = { currentScreen = Screen.AdminEditProfile },
                        onLogout = { 
                            com.wastereporting.network.TokenManager.clearToken()
                            currentScreen = Screen.AdminLogin 
                        }
                    )
                    Screen.AdminEditProfile -> AdminEditProfileScreen(
                        onBack = { currentScreen = Screen.AdminProfile }
                    )
                    Screen.AdminSettings -> AdminSettingsScreen(
                        onBack = { currentScreen = Screen.AdminProfile }
                    )
                    Screen.HelpSupport -> HelpSupportScreen(
                        onBack = { currentScreen = Screen.Profile },
                        onNavigateToChat = { currentScreen = Screen.ChatSupport },
                        onNavigateToCall = { currentScreen = Screen.ContactSupport },
                        onNavigateToFaq = { currentScreen = Screen.FAQDetails },
                        onNavigateToEmail = { currentScreen = Screen.EmailSupport }
                    )
                    Screen.ChatSupport -> ChatSupportScreen(onBack = { currentScreen = Screen.HelpSupport })
                    Screen.ContactSupport -> ContactSupportScreen(onBack = { currentScreen = Screen.HelpSupport })
                    Screen.FAQDetails -> FAQDetailsScreen(
                        onBack = { currentScreen = Screen.HelpSupport },
                        onNeedMoreHelp = { currentScreen = Screen.ContactSupport }
                    )
                    Screen.EmailSupport -> EmailSupportScreen(
                        onBack = { currentScreen = Screen.HelpSupport },
                        onSubmit = { currentScreen = Screen.EmailSubmitted }
                    )
                    Screen.EmailSubmitted -> EmailSubmittedScreen(
                        onBackToHelp = { currentScreen = Screen.HelpSupport }
                    )
                    Screen.AboutApp -> AboutAppScreen(onBack = {
                        currentScreen = when (com.wastereporting.network.TokenManager.userRole) {
                            "supervisor" -> Screen.SupervisorProfile
                            "admin" -> Screen.AdminProfile
                            else -> Screen.Profile
                        }
                    })
                    Screen.NearbyIssues -> NearbyIssuesScreen(
                        onBack = { currentScreen = Screen.Home },
                        onNavigateToReview = { currentScreen = Screen.ReviewReport }
                    )
                    Screen.ReviewReport -> ReviewReportScreen(
                        onBack = { currentScreen = Screen.ConfirmLocation },
                        onSubmit = { currentScreen = Screen.ReportSuccess },
                        onEditLocation = { currentScreen = Screen.ConfirmLocation },
                        onEditCategory = { currentScreen = Screen.SelectCategory }
                    )
                    Screen.ReportSuccess -> ReportSubmittedScreen(
                        onBackToDashboard = { currentScreen = Screen.Home }
                    )
                    Screen.CameraCapture -> CameraCaptureScreen(
                        onCapture = { currentScreen = Screen.SelectCategory },
                        onClose = { currentScreen = Screen.Report }
                    )
                    Screen.SelectCategory -> SelectCategoryScreen(
                        onBack = { currentScreen = Screen.Report },
                        onContinue = { currentScreen = Screen.ConfirmLocation }
                    )
                    Screen.ConfirmLocation -> ConfirmLocationScreen(
                        onBack = { currentScreen = Screen.SelectCategory },
                        onConfirm = { currentScreen = Screen.ReviewReport }
                    )
                    Screen.DailyStatistics -> DailyStatisticsScreen(onBack = { currentScreen = Screen.Analytics })
                    Screen.MonthlyReports -> MonthlyReportsScreen(onBack = { currentScreen = Screen.Analytics })
                    Screen.RecyclingBreakdown -> RecyclingBreakdownScreen(
                        onBack = { currentScreen = Screen.Analytics },
                        onNavigateToInsights = { currentScreen = Screen.SmartInsights }
                    )
                    Screen.SmartInsights -> SmartInsightsScreen(
                        onBack = { currentScreen = Screen.Analytics },
                        onReturnToDashboard = { currentScreen = Screen.Home }
                    )
                    Screen.EnvironmentalImpact -> EnvironmentalImpactScreen(onBack = { currentScreen = Screen.Analytics })
                    Screen.CollectionStatus -> CollectionStatusScreen(
                        onBack = { currentScreen = Screen.Home },
                        onTrackLive = { id ->
                            trackingReportId = id
                            trackingBackScreen = Screen.CollectionStatus
                            currentScreen = Screen.LiveTracking
                        }
                    )
                    Screen.LiveTracking -> LiveTrackingScreen(
                        reportId = trackingReportId,
                        onBack = { currentScreen = trackingBackScreen },
                        onDetails = { currentScreen = Screen.CollectionSummary }
                    )
                    Screen.CollectionSummary -> CollectionSummaryScreen(
                        onBack = { currentScreen = Screen.LiveTracking },
                        onReturnToDashboard = { currentScreen = Screen.Home }
                    )
                    Screen.IssueHeatmap -> IssueHeatmapScreen(
                        onBack = { currentScreen = Screen.Home },
                        onNavigateToNavigation = { currentScreen = Screen.ActiveNavigation }
                    )
                    Screen.ActiveNavigation -> ActiveNavigationScreen(
                        onArrived = { currentScreen = Screen.Home },
                        onClose = { currentScreen = Screen.Home }
                    )
                    Screen.SupervisorLogin -> SupervisorLoginScreen(
                        onBack = { currentScreen = Screen.Onboarding },
                        onLoginSuccess = { currentScreen = Screen.SupervisorDashboard }
                    )
                    Screen.SupervisorDashboard -> SupervisorDashboardScreen(
                        onNavigateToPendingReports = { currentScreen = Screen.SupervisorPendingReports },
                        onNavigateToCompletedReports = { currentScreen = Screen.SupervisorCompletedReports },
                        onNavigateToProfile = { currentScreen = Screen.SupervisorProfile },
                        onNavigateToReportDetails = { reportId -> 
                            selectedReportId = reportId
                            currentScreen = Screen.SupervisorReportDetails 
                        },
                        onNavigateToNotifications = { currentScreen = Screen.Notifications }
                    )
                    Screen.SupervisorPendingReports -> SupervisorPendingReportsScreen(
                        onNavigateToDashboard = { currentScreen = Screen.SupervisorDashboard },
                        onNavigateToProfile = { currentScreen = Screen.SupervisorProfile },
                        onNavigateToReportDetails = { reportId -> 
                            selectedReportId = reportId
                            currentScreen = Screen.SupervisorReportDetails 
                        },
                        onBack = { currentScreen = Screen.SupervisorDashboard }
                    )
                                        Screen.SupervisorCompletedReports -> SupervisorCompletedReportsScreen(
                        onNavigateToDashboard = { currentScreen = Screen.SupervisorDashboard },
                        onNavigateToProfile = { currentScreen = Screen.SupervisorProfile },
                        onNavigateToReportDetails = { reportId -> 
                            selectedReportId = reportId
                            currentScreen = Screen.SupervisorReportDetails 
                        },
                        onBack = { currentScreen = Screen.SupervisorDashboard }
                    )
                    Screen.SupervisorProfile -> SupervisorProfileScreen(
                        onNavigateToDashboard = { currentScreen = Screen.SupervisorDashboard },
                        onNavigateToHistory = { currentScreen = Screen.SupervisorCompletedReports },
                        onNavigateToAbout = { currentScreen = Screen.AboutApp },
                        onLogout = { 
                            com.wastereporting.network.TokenManager.clearToken()
                            currentScreen = Screen.SupervisorLogin 
                        }
                    )
                    Screen.SupervisorEditProfile -> SupervisorEditProfileScreen(
                        onBack = { currentScreen = Screen.SupervisorProfile }
                    )
                    Screen.SupervisorReportDetails -> SupervisorReportDetailsScreen(
                        reportId = selectedReportId ?: 1, // Fallback to 1 if null for safety
                        onBack = { currentScreen = Screen.SupervisorDashboard },
                                                onNavigateToCompletedReports = { currentScreen = Screen.SupervisorCompletedReports }
                    )
                    Screen.SupervisorReportStatus -> SupervisorReportStatusScreen(
                        onBack = { currentScreen = Screen.SupervisorDashboard }
                    )
                    Screen.SupervisorSettings -> SupervisorSettingsScreen(
                        onBack = { currentScreen = Screen.SupervisorProfile }
                    )
                    Screen.Notifications -> NotificationsScreen(
                        onBack = { 
                            currentScreen = when (com.wastereporting.network.TokenManager.userRole) {
                                "supervisor" -> Screen.SupervisorDashboard
                                "admin" -> Screen.AdminDashboard
                                else -> Screen.Home
                            }
                        }
                    )
                    else -> {
                        androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            androidx.compose.material3.Text("Screen not implemented yet: $currentScreen")
                        }
                    }
                }
            }
        }
    }
}
