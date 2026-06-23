package com.wastereporting.network

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val role: String? = null,
    val full_name: String,
    val email: String,
    val phone: String?,
    val dob: String? = null,
    val gender: String? = null,
    val address: String? = null,
    val city: String? = null,
    val country: String? = null,
    val employee_id: String? = null,
    val assigned_area: String? = null,
    val department: String? = null,
    val profile_image_url: String? = null,
    val profile_picture: String? = null, // legacy
    val created_at: String? = null
)

@Serializable
data class AuthResponse(
    val message: String,
    val token: String,
    val user: User
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class SendOtpRequest(
    val full_name: String = "",
    val email: String,
    val phone: String? = null,
    val dob: String? = null,
    val password: String = ""
)

@Serializable
data class SendOtpResponse(
    val message: String,
    val otp: String? = null
)

@Serializable
data class RegisterRequest(
    val email: String,
    val full_name: String,
    val phone: String,
    val dob: String,
    val password: String
)

@Serializable
data class ProfileResponse(
    val user: User
)

@Serializable
data class IssueReport(
    val id: Int,
    val citizen_id: Int,
    val assigned_supervisor_id: Int? = null,
    val supervisor_id: Int? = null, // legacy
    val title: String? = null,
    val category: String,
    val description: String?,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val location: String? = null,
    val image_url: String? = null,
    val before_image: String? = null, // legacy
    val completion_image_url: String? = null,
    val after_image: String? = null, // legacy
    val status: String,
    val created_at: String?,
    val updated_at: String? = null,
    val resolved_at: String? = null,
    val reporter_name: String? = null
)

@Serializable
data class IssueHistory(
    val id: Int,
    val issue_id: Int,
    val status: String,
    val updated_by: Int,
    val remarks: String?,
    val created_at: String
)

@Serializable
data class IssueReportResponse(
    val message: String,
    val issue: IssueReport
)

@Serializable
data class IssuesResponse(
    val issues: List<IssueReport>
)

@Serializable
data class SupervisorReportsWrapper(
    val reports: List<IssueReport>
)

@Serializable
data class SupervisorDetailsResponse(
    val id: Int,
    val name: String,
    val employee_id: String,
    val assigned_area: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val phone: String? = null
)

@Serializable
data class SingleReportResponse(
    val issue: IssueReport? = null,
    val report: IssueReport? = null,
    val history: List<IssueHistory> = emptyList(),
    val assigned_supervisor: SupervisorDetailsResponse? = null
)

@Serializable
data class AreaStatsResponse(
    val active_reports: Int,
    val resolved_today: Int,
    val cleanliness_score: Int
)

@Serializable
data class ErrorResponse(
    val message: String
)

@Serializable
data class SupervisorLoginRequest(
    val employee_id: String,
    val password: String
)

@Serializable
data class SupervisorRegisterRequest(
    val full_name: String,
    val employee_id: String,
    val email: String,
    val phone: String? = null,
    val assigned_area: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val coverage_radius: Double? = null,
    val password: String
)

@Serializable
data class SupervisorUpdateRequest(
    val full_name: String,
    val employee_id: String,
    val email: String,
    val phone: String? = null,
    val assigned_area: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val coverage_radius: Double? = null,
    val password: String? = null
)

@Serializable
data class Supervisor(
    val id: Int,
    val employee_id: String,
    val user_id: Int,
    val assigned_area: String?,
    val created_at: String,
    val name: String?,
    val email: String?,
    val phone: String?,
    val profile_picture: String?,
    val password_hash: String? = null
)

@Serializable
data class SupervisorAuthResponse(
    val message: String,
    val token: String? = null,
    val supervisor: Supervisor
)

@Serializable
data class CitizenDashboardResponse(
    val total_reports: Int,
    val resolved_reports: Int,
    val pending_reports: Int,
    val eco_points: Int
)

@Serializable
data class SupervisorDashboardResponse(
    val assigned_reports: Int,
    val completed_reports: Int,
    val pending_reports: Int,
    val performance_score: Int
)

@Serializable
data class AdminDashboardResponse(
    val total_citizens: Int,
    val total_supervisors: Int,
    val total_reports: Int,
    val resolved_reports: Int,
    val pending_reports: Int,
    val cleanliness_score: Int
)

@Serializable
data class AdminSupervisorsResponse(
    val supervisors: List<AdminSupervisorStats>
)

@Serializable
data class AdminSupervisorStats(
    val id: Int,
    val full_name: String,
    val email: String,
    val employee_id: String,
    val assigned_area: String?,
    val assigned_reports: Int,
    val resolved_reports: Int,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val coverage_radius: Double? = null,
    val is_active: Boolean? = true
)

@Serializable
data class AdminCitizensResponse(
    val citizens: List<User>
)

@Serializable
data class HeatmapPoint(
    val lat: Double,
    val lng: Double,
    val weight: Float,
    val status: String
)

@Serializable
data class AdminHeatmapResponse(
    val hotspots: List<HeatmapPoint>,
    val ai_recommendations: List<String>
)

@Serializable
data class Notification(
    val id: Int,
    val user_id: Int,
    val title: String,
    val message: String,
    val read_status: Int,
    val created_at: String
)

@Serializable
data class NotificationsResponse(
    val notifications: List<Notification>
)

@Serializable
data class WeeklyTrend(
    val label: String,
    val general: Float,
    val recycling: Float
)

@Serializable
data class AdminAnalyticsResponse(
    val total_volume: Float,
    val volume_trend: String,
    val recycling_rate: Float,
    val recycling_trend: String,
    val weeks: List<WeeklyTrend>,
    val total_waste_collected: Int,
    val avg_resolution_time_hrs: String,
    val recycling_rate_percent: String,
    val citizen_engagement_score: String,
    val issues_per_month: List<Int>,
    val category_breakdown: Map<String, Int>
)

@Serializable
data class OsrmGeometry(
    val coordinates: List<List<Double>>
)

@Serializable
data class OsrmRoute(
    val geometry: OsrmGeometry,
    val duration: Double? = null,
    val distance: Double? = null
)

@Serializable
data class OsrmResponse(
    val routes: List<OsrmRoute>
)

data class RouteDetails(
    val coordinates: List<Pair<Double, Double>>,
    val duration: Double,
    val distance: Double
)
