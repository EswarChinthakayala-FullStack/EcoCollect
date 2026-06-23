package com.wastereporting.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Serializable
data class IssueReportCreatePayload(
    val title: String?,
    val category: String,
    val description: String?,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val location: String?,
    val image_url: String?
)

/**
 * ApiService — Connected to Python FastAPI backend using Ktor Client.
 */
object ApiService {

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }

    private const val BASE_URL = "http://192.168.31.46:8000/api"

    fun getFullImageUrl(url: String): String {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url
        }
        val serverRoot = BASE_URL.removeSuffix("/api")
        val cleanUrl = if (url.startsWith("/")) url else "/$url"
        return "$serverRoot$cleanUrl"
    }

    fun formatIsoDateTimeToIndian(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "Unknown Date"
        return try {
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
            
            if (isoString.contains("Z") || isoString.contains("+") || (isoString.indexOf("-", 10) != -1)) {
                val zonedDateTime = ZonedDateTime.parse(isoString)
                val indiaDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("Asia/Kolkata"))
                indiaDateTime.format(formatter)
            } else {
                val localDateTime = LocalDateTime.parse(isoString)
                localDateTime.format(formatter)
            }
        } catch (e: Exception) {
            try {
                isoString.replace("T", " ")
            } catch (ex: Exception) {
                isoString
            }
        }
    }

    private fun HttpRequestBuilder.bearerAuth() {
        TokenManager.jwtToken?.let { token ->
            header("Authorization", "Bearer $token")
        }
    }

    suspend fun uploadImage(bytes: ByteArray): String? {
        return try {
            val response: UploadResponse = client.submitFormWithBinaryData(
                url = "$BASE_URL/upload",
                formData = formData {
                    append("file", bytes, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=\"upload.jpg\"")
                    })
                }
            ).body()
            response.file_url
        } catch (e: Exception) {
            null
        }
    }

    // ─────────────────────────────────────────────
    // HELPER FOR RESPONSE HANDLING
    // ─────────────────────────────────────────────

    private suspend inline fun <reified T> handleResponse(
        crossinline block: suspend () -> HttpResponse,
        fallbackError: String = "Request failed"
    ): Result<T> {
        return try {
            val response = block()
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                if (response.status == HttpStatusCode.Unauthorized) {
                    TokenManager.clearToken()
                }
                val errorMsg = try {
                    val errorBody = response.body<Map<String, String>>()
                    errorBody["detail"] ?: fallbackError
                } catch (e: Exception) {
                    fallbackError
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────────
    // CITIZEN AUTH
    // ─────────────────────────────────────────────

    suspend fun login(request: LoginRequest): Result<AuthResponse> {
        val result = handleResponse<AuthResponse>(
            block = {
                client.post("$BASE_URL/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            },
            fallbackError = "Login failed"
        )
        if (result.isSuccess) {
            val response = result.getOrThrow()
            TokenManager.userRole = response.user.role
            TokenManager.jwtToken = response.token
        }
        return result
    }

    suspend fun sendOtp(request: SendOtpRequest): Result<SendOtpResponse> {
        return handleResponse<SendOtpResponse>(
            block = {
                client.post("$BASE_URL/auth/send-otp") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            },
            fallbackError = "Failed to send OTP"
        )
    }

    suspend fun verifyOtp(email: String, otp: String): Result<AuthResponse> {
        val result = handleResponse<AuthResponse>(
            block = {
                client.post("$BASE_URL/auth/verify-otp") {
                    parameter("email", email)
                    parameter("otp", otp)
                }
            },
            fallbackError = "OTP verification failed"
        )
        if (result.isSuccess) {
            val response = result.getOrThrow()
            TokenManager.userRole = response.user.role
            TokenManager.jwtToken = response.token
        }
        return result
    }

    suspend fun forgotPassword(email: String): Result<Boolean> {
        return try {
            val response = client.post("$BASE_URL/auth/forgot-password") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("email" to email))
            }
            if (response.status.isSuccess()) {
                Result.success(true)
            } else {
                val errorMsg = try {
                    val errorBody = response.body<Map<String, String>>()
                    errorBody["detail"] ?: "Request failed"
                } catch (e: Exception) {
                    "Request failed"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyPasswordResetOtp(email: String, otp: String): Result<Boolean> {
        return try {
            val response = client.post("$BASE_URL/auth/verify-otp") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "email" to email,
                    "otp" to otp
                ))
            }
            if (response.status.isSuccess()) {
                Result.success(true)
            } else {
                val errorMsg = try {
                    val errorBody = response.body<Map<String, String>>()
                    errorBody["detail"] ?: "OTP verification failed"
                } catch (e: Exception) {
                    "OTP verification failed"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(email: String, otp: String, newPassword: String): Result<Boolean> {
        return try {
            val response = client.post("$BASE_URL/auth/reset-password") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "email" to email,
                    "otp" to otp,
                    "new_password" to newPassword
                ))
            }
            if (response.status.isSuccess()) {
                Result.success(true)
            } else {
                val errorMsg = try {
                    val errorBody = response.body<Map<String, String>>()
                    errorBody["detail"] ?: "Failed to reset password"
                } catch (e: Exception) {
                    "Failed to reset password"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        val result = handleResponse<AuthResponse>(
            block = {
                client.post("$BASE_URL/auth/register") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            },
            fallbackError = "Registration failed"
        )
        if (result.isSuccess) {
            val response = result.getOrThrow()
            TokenManager.userRole = response.user.role
            TokenManager.jwtToken = response.token
        }
        return result
    }

    // ─────────────────────────────────────────────
    // SUPERVISOR AUTH
    // ─────────────────────────────────────────────

    suspend fun supervisorLogin(request: SupervisorLoginRequest): Result<SupervisorAuthResponse> {
        val result = handleResponse<SupervisorAuthResponse>(
            block = {
                client.post("$BASE_URL/auth/supervisor/login") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            },
            fallbackError = "Supervisor login failed"
        )
        if (result.isSuccess) {
            val response = result.getOrThrow()
            TokenManager.userRole = "supervisor"
            TokenManager.jwtToken = response.token
        }
        return result
    }

    suspend fun supervisorRegister(request: SupervisorRegisterRequest): Result<SupervisorAuthResponse> {
        return handleResponse<SupervisorAuthResponse>(
            block = {
                client.post("$BASE_URL/auth/supervisor/register") {
                    contentType(ContentType.Application.Json)
                    bearerAuth()
                    setBody(request)
                }
            },
            fallbackError = "Supervisor registration failed"
        )
    }

    // ─────────────────────────────────────────────
    // PROFILE
    // ─────────────────────────────────────────────

    suspend fun getProfile(): Result<User> {
        return handleResponse<User>(
            block = {
                client.get("$BASE_URL/profile") {
                    bearerAuth()
                }
            },
            fallbackError = "Failed to load profile"
        )
    }

    suspend fun updateProfile(data: Map<String, String>, imageBytes: ByteArray? = null): Result<User> {
        return try {
            var imageUrl: String? = null
            if (imageBytes != null && imageBytes.isNotEmpty()) {
                imageUrl = uploadImage(imageBytes)
            }
            
            val payload = data.toMutableMap()
            if (imageUrl != null) {
                payload["profile_image_url"] = imageUrl
            }
            
            handleResponse<User>(
                block = {
                    client.put("$BASE_URL/profile") {
                        contentType(ContentType.Application.Json)
                        setBody(payload)
                        bearerAuth()
                    }
                },
                fallbackError = "Failed to update profile"
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────────
    // CITIZEN
    // ─────────────────────────────────────────────

    suspend fun getCitizenDashboard(): Result<CitizenDashboardResponse> {
        return handleResponse<CitizenDashboardResponse>(
            block = {
                client.get("$BASE_URL/citizen/dashboard") {
                    bearerAuth()
                }
            },
            fallbackError = "Failed to load dashboard data"
        )
    }

    suspend fun submitIssue(
        category: String,
        description: String,
        latitude: Double,
        longitude: Double,
        address: String,
        imagesList: List<ByteArray> = emptyList()
    ): Result<IssueReport> {
        return try {
            val uploadedUrls = mutableListOf<String>()
            for (bytes in imagesList) {
                if (bytes.isNotEmpty()) {
                    val url = uploadImage(bytes)
                    if (url != null) {
                        uploadedUrls.add(url)
                    }
                }
            }
            
            val finalImageUrl = if (uploadedUrls.isNotEmpty()) {
                uploadedUrls.joinToString(",")
            } else {
                null
            }
            
            val payload = IssueReportCreatePayload(
                title = category,
                category = category,
                description = description,
                latitude = latitude,
                longitude = longitude,
                address = address,
                location = address,
                image_url = finalImageUrl
            )
            
            handleResponse<IssueReport>(
                block = {
                    client.post("$BASE_URL/citizen/issues") {
                        contentType(ContentType.Application.Json)
                        setBody(payload)
                        bearerAuth()
                    }
                },
                fallbackError = "Failed to submit report"
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getIssues(): Result<List<IssueReport>> {
        return handleResponse<List<IssueReport>>(
            block = {
                client.get("$BASE_URL/citizen/issues") {
                    bearerAuth()
                }
            },
            fallbackError = "Failed to load reports"
        )
    }

    suspend fun getReportById(reportId: Int): Result<SingleReportResponse> {
        return handleResponse<SingleReportResponse>(
            block = {
                client.get("$BASE_URL/issues/$reportId") {
                    bearerAuth()
                }
            },
            fallbackError = "Failed to load report details"
        )
    }

    // ─────────────────────────────────────────────
    // SUPERVISOR
    // ─────────────────────────────────────────────

    suspend fun getSupervisorDashboard(): Result<SupervisorDashboardResponse> {
        return handleResponse<SupervisorDashboardResponse>(
            block = {
                client.get("$BASE_URL/supervisor/dashboard") {
                    bearerAuth()
                }
            },
            fallbackError = "Failed to load dashboard"
        )
    }

    suspend fun getSupervisorReports(status: String? = null): Result<List<IssueReport>> {
        return handleResponse<List<IssueReport>>(
            block = {
                client.get("$BASE_URL/supervisor/issues") {
                    if (!status.isNullOrBlank() && status != "All") {
                        parameter("status", status)
                    }
                    bearerAuth()
                }
            },
            fallbackError = "Failed to load supervisor reports"
        )
    }

    suspend fun getSupervisorNearbyReports(): Result<List<IssueReport>> {
        return handleResponse<List<IssueReport>>(
            block = {
                client.get("$BASE_URL/supervisor/issues/nearby") {
                    bearerAuth()
                }
            },
            fallbackError = "Failed to load nearby reports"
        )
    }

    suspend fun completeIssue(issueId: Int, completionImageUrl: String?, remarks: String? = null): Result<Boolean> {
        return try {
            val response = client.put("$BASE_URL/supervisor/issues/$issueId/complete") {
                completionImageUrl?.let { parameter("completion_image_url", it) }
                remarks?.let { parameter("remarks", it) }
                bearerAuth()
            }
            if (response.status.isSuccess()) {
                Result.success(true)
            } else {
                if (response.status == HttpStatusCode.Unauthorized) {
                    TokenManager.clearToken()
                }
                val errorMsg = try {
                    val errorBody = response.body<Map<String, String>>()
                    errorBody["detail"] ?: "Failed to complete issue"
                } catch (e: Exception) {
                    "Failed to complete issue"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────────
    // ADMIN
    // ─────────────────────────────────────────────

    suspend fun getAdminDashboard(): Result<AdminDashboardResponse> {
        return handleResponse<AdminDashboardResponse>(
            block = {
                client.get("$BASE_URL/admin/dashboard") {
                    bearerAuth()
                }
            },
            fallbackError = "Failed to load admin dashboard"
        )
    }

    suspend fun getAdminReports(): Result<List<IssueReport>> {
        return handleResponse<List<IssueReport>>(
            block = {
                client.get("$BASE_URL/admin/issues") {
                    bearerAuth()
                }
            },
            fallbackError = "Failed to load reports"
        )
    }

    suspend fun assignReport(reportId: Int, supervisorId: Int): Result<Boolean> {
        return try {
            val response = client.put("$BASE_URL/admin/issues/$reportId/assign") {
                parameter("supervisor_id", supervisorId)
                bearerAuth()
            }
            if (response.status.isSuccess()) {
                Result.success(true)
            } else {
                if (response.status == HttpStatusCode.Unauthorized) {
                    TokenManager.clearToken()
                }
                val errorMsg = try {
                    val errorBody = response.body<Map<String, String>>()
                    errorBody["detail"] ?: "Failed to assign report"
                } catch (e: Exception) {
                    "Failed to assign report"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAdminSupervisors(): Result<List<AdminSupervisorStats>> {
        return handleResponse<List<AdminSupervisorStats>>(
            block = {
                client.get("$BASE_URL/admin/supervisors") {
                    bearerAuth()
                }
            },
            fallbackError = "Failed to load supervisors list"
        )
    }

    suspend fun updateSupervisor(supervisorId: Int, request: SupervisorUpdateRequest): Result<Boolean> {
        return try {
            val response = client.put("$BASE_URL/admin/supervisors/$supervisorId") {
                contentType(ContentType.Application.Json)
                setBody(request)
                bearerAuth()
            }
            if (response.status.isSuccess()) {
                Result.success(true)
            } else {
                if (response.status == HttpStatusCode.Unauthorized) {
                    TokenManager.clearToken()
                }
                val errorMsg = try {
                    val errorBody = response.body<Map<String, String>>()
                    errorBody["detail"] ?: "Failed to update supervisor"
                } catch (e: Exception) {
                    "Failed to update supervisor"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleSupervisorStatus(supervisorId: Int): Result<Boolean> {
        return try {
            val response = client.put("$BASE_URL/admin/supervisors/$supervisorId/toggle-status") {
                bearerAuth()
            }
            if (response.status.isSuccess()) {
                Result.success(true)
            } else {
                if (response.status == HttpStatusCode.Unauthorized) {
                    TokenManager.clearToken()
                }
                val errorMsg = try {
                    val errorBody = response.body<Map<String, String>>()
                    errorBody["detail"] ?: "Failed to toggle supervisor status"
                } catch (e: Exception) {
                    "Failed to toggle supervisor status"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun getAdminHeatmap(): Result<AdminHeatmapResponse> {
        return handleResponse<AdminHeatmapResponse>(
            block = {
                client.get("$BASE_URL/admin/heatmap") {
                    bearerAuth()
                }
            },
            fallbackError = "Failed to load heatmap data"
        )
    }

    // ─────────────────────────────────────────────
    // NOTIFICATIONS
    // ─────────────────────────────────────────────

    suspend fun getNotifications(): Result<List<Notification>> {
        return handleResponse<List<Notification>>(
            block = {
                client.get("$BASE_URL/notifications") {
                    bearerAuth()
                }
            },
            fallbackError = "Failed to load notifications"
        )
    }

    suspend fun markNotificationRead(id: Int): Result<Boolean> {
        return try {
            val response = client.put("$BASE_URL/notifications/$id/read") {
                bearerAuth()
            }
            if (response.status.isSuccess()) {
                Result.success(true)
            } else {
                if (response.status == HttpStatusCode.Unauthorized) {
                    TokenManager.clearToken()
                }
                val errorMsg = try {
                    val errorBody = response.body<Map<String, String>>()
                    errorBody["detail"] ?: "Failed to mark notification as read"
                } catch (e: Exception) {
                    "Failed to mark notification as read"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─────────────────────────────────────────────
    // ANALYTICS
    // ─────────────────────────────────────────────

    suspend fun getAdminAnalytics(): Result<AdminAnalyticsResponse> {
        return handleResponse<AdminAnalyticsResponse>(
            block = {
                client.get("$BASE_URL/admin/analytics") {
                    bearerAuth()
                }
            },
            fallbackError = "Failed to load analytics data"
        )
    }

    suspend fun updateSupervisorLocation(latitude: Double, longitude: Double): Result<Boolean> {
        return try {
            val response = client.put("$BASE_URL/supervisor/location") {
                parameter("latitude", latitude)
                parameter("longitude", longitude)
                bearerAuth()
            }
            if (response.status.isSuccess()) {
                Result.success(true)
            } else {
                if (response.status == HttpStatusCode.Unauthorized) {
                    TokenManager.clearToken()
                }
                Result.failure(Exception("Failed to update location"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDrivingRoute(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): Result<RouteDetails> {
        return try {
            val response: HttpResponse = client.get("https://router.project-osrm.org/route/v1/driving/$startLng,$startLat;$endLng,$endLat") {
                parameter("overview", "full")
                parameter("geometries", "geojson")
            }
            if (response.status.isSuccess()) {
                val bodyText = response.body<String>()
                val osrmResponse = Json { ignoreUnknownKeys = true }.decodeFromString<OsrmResponse>(bodyText)
                val route = osrmResponse.routes.firstOrNull()
                val coordinates = route?.geometry?.coordinates
                val duration = route?.duration ?: 0.0
                val distance = route?.distance ?: 0.0
                if (coordinates != null) {
                    val path = coordinates.map { Pair(it[1], it[0]) }
                    Result.success(RouteDetails(path, duration, distance))
                } else {
                    Result.failure(Exception("No route geometry found"))
                }
            } else {
                Result.failure(Exception("Failed to fetch route from OSRM: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Boolean> {
        return try {
            val response = client.put("$BASE_URL/change-password") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("current_password" to currentPassword, "new_password" to newPassword))
                bearerAuth()
            }
            if (response.status.isSuccess()) {
                Result.success(true)
            } else {
                if (response.status == HttpStatusCode.Unauthorized) {
                    TokenManager.clearToken()
                }
                val errorMsg = try {
                    val errorBody = response.body<Map<String, String>>()
                    errorBody["detail"] ?: "Failed to change password"
                } catch (e: Exception) {
                    "Failed to change password"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
