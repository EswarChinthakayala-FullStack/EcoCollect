package com.wastereporting.network

import com.russhwolf.settings.SharedPreferencesSettings
import android.content.Context
import com.wastereporting.WasteReportingApp
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object LocalDataStore {
    private val settings by lazy {
        val sharedPrefs = WasteReportingApp.appContext.getSharedPreferences("wastereporting_prefs", Context.MODE_PRIVATE)
        SharedPreferencesSettings(sharedPrefs)
    }
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }

    // Storage keys
    private const val ISSUES_KEY = "local_issues_v2"
    private const val NOTIFICATIONS_KEY = "local_notifications_v2"
    private const val SUPERVISORS_KEY = "local_supervisors_v2"
    private const val CITIZENS_KEY = "local_citizens_v2"
    private const val PROFILE_KEY = "local_profile_v2"

    // ─────────────────────────────────────────────
    // SEED DATA
    // ─────────────────────────────────────────────

    private val defaultIssues = listOf(
        IssueReport(
            id = 1,
            citizen_id = 1,
            title = "Overflowing Trash",
            category = "Garbage",
            description = "Trash bin is overflowing near the park entrance.",
            latitude = 37.7749,
            longitude = -122.4194,
            address = "Golden Gate Park, SF",
            status = "Pending",
            created_at = "2026-06-11T10:00:00Z"
        ),
        IssueReport(
            id = 2,
            citizen_id = 1,
            title = "Broken Streetlight",
            category = "Infrastructure",
            description = "Streetlight is out, making the area dark and unsafe.",
            latitude = 37.7750,
            longitude = -122.4180,
            address = "5th Ave, SF",
            status = "Completed",
            created_at = "2026-06-10T08:30:00Z",
            resolved_at = "2026-06-11T09:00:00Z"
        ),
        IssueReport(
            id = 3,
            citizen_id = 1,
            title = "Illegal Dumping",
            category = "Hazardous Waste",
            description = "Illegal dumping of construction debris on the roadside.",
            latitude = 37.7760,
            longitude = -122.4170,
            address = "Market St, SF",
            status = "Pending",
            created_at = "2026-06-09T14:00:00Z"
        )
    )

    private val defaultNotifications = listOf(
        Notification(
            id = 1,
            user_id = 1,
            title = "Welcome to EcoCollect",
            message = "Thank you for joining the Smart Waste Management platform.",
            read_status = 0,
            created_at = "2026-06-10T10:00:00Z"
        ),
        Notification(
            id = 2,
            user_id = 1,
            title = "Report Update",
            message = "Your report 'Broken Streetlight' has been marked as Completed.",
            read_status = 0,
            created_at = "2026-06-11T09:00:00Z"
        )
    )

    val mockSupervisor = Supervisor(
        id = 101,
        employee_id = "SUP-100",
        user_id = 2,
        assigned_area = "Downtown SF",
        created_at = "2026-01-01T00:00:00Z",
        name = "Senior Supervisor",
        email = "supervisor@demo.com",
        phone = "555-0200",
        profile_picture = null,
        password_hash = "password123"
    )

    val mockCitizenUser = User(
        id = 1,
        role = "citizen",
        full_name = "Demo Citizen",
        email = "citizen@demo.com",
        phone = "555-0100",
        city = "San Francisco",
        country = "USA"
    )

    // ─────────────────────────────────────────────
    // INITIALIZATION — seeds if storage is empty
    // ─────────────────────────────────────────────

    init {
        if (settings.getStringOrNull(ISSUES_KEY) == null) {
            saveIssues(defaultIssues)
        }
        if (settings.getStringOrNull(NOTIFICATIONS_KEY) == null) {
            saveNotifications(defaultNotifications)
        }
        // Supervisor seeding: migrate from old key if new key is empty
        if (settings.getStringOrNull(SUPERVISORS_KEY) == null) {
            val oldData = settings.getStringOrNull("mock_supervisors_list")
            if (oldData != null) {
                // Migrate old supervisors to new key
                try {
                    val oldList = json.decodeFromString<List<Supervisor>>(oldData)
                    if (oldList.isNotEmpty()) {
                        saveSupervisors(oldList)
                    } else {
                        saveSupervisors(listOf(mockSupervisor))
                    }
                } catch (e: Exception) {
                    saveSupervisors(listOf(mockSupervisor))
                }
            } else {
                saveSupervisors(listOf(mockSupervisor))
            }
        }
        if (settings.getStringOrNull(CITIZENS_KEY) == null) {
            saveCitizens(listOf(mockCitizenUser))
        }
    }

    // ─────────────────────────────────────────────
    // ISSUES
    // ─────────────────────────────────────────────

    fun getIssues(): List<IssueReport> {
        val str = settings.getStringOrNull(ISSUES_KEY) ?: return defaultIssues
        return try {
            json.decodeFromString(str)
        } catch (e: Exception) {
            saveIssues(defaultIssues)
            defaultIssues
        }
    }

    fun saveIssues(issues: List<IssueReport>) {
        settings.putString(ISSUES_KEY, json.encodeToString(issues))
    }

    fun addIssue(issue: IssueReport) {
        val issues = getIssues().toMutableList()
        issues.add(0, issue)
        saveIssues(issues)
    }

    fun updateIssue(issue: IssueReport) {
        val issues = getIssues().map { if (it.id == issue.id) issue else it }
        saveIssues(issues)
    }

    // ─────────────────────────────────────────────
    // NOTIFICATIONS
    // ─────────────────────────────────────────────

    fun getNotifications(): List<Notification> {
        val str = settings.getStringOrNull(NOTIFICATIONS_KEY) ?: return defaultNotifications
        return try {
            json.decodeFromString(str)
        } catch (e: Exception) {
            saveNotifications(defaultNotifications)
            defaultNotifications
        }
    }

    fun saveNotifications(notifications: List<Notification>) {
        settings.putString(NOTIFICATIONS_KEY, json.encodeToString(notifications))
    }

    fun markNotificationRead(id: Int) {
        val updated = getNotifications().map { if (it.id == id) it.copy(read_status = 1) else it }
        saveNotifications(updated)
    }

    // ─────────────────────────────────────────────
    // SUPERVISORS — persisted, admin-created only
    // ─────────────────────────────────────────────

    fun getSupervisors(): List<Supervisor> {
        val str = settings.getStringOrNull(SUPERVISORS_KEY)
        if (str == null) {
            val seed = listOf(mockSupervisor)
            saveSupervisors(seed)
            return seed
        }
        return try {
            val list = json.decodeFromString<List<Supervisor>>(str)
            // If list somehow got corrupted/empty, restore seed
            if (list.isEmpty()) {
                val seed = listOf(mockSupervisor)
                saveSupervisors(seed)
                seed
            } else {
                list
            }
        } catch (e: Exception) {
            val seed = listOf(mockSupervisor)
            saveSupervisors(seed)
            seed
        }
    }

    fun saveSupervisors(supervisors: List<Supervisor>) {
        settings.putString(SUPERVISORS_KEY, json.encodeToString(supervisors))
    }

    fun addSupervisor(supervisor: Supervisor) {
        val list = getSupervisors().toMutableList()
        list.add(0, supervisor)
        saveSupervisors(list)
    }

    // ─────────────────────────────────────────────
    // CITIZENS — persisted
    // ─────────────────────────────────────────────

    fun getCitizens(): List<User> {
        val str = settings.getStringOrNull(CITIZENS_KEY) ?: return listOf(mockCitizenUser)
        return try {
            json.decodeFromString(str)
        } catch (e: Exception) {
            listOf(mockCitizenUser)
        }
    }

    fun saveCitizens(citizens: List<User>) {
        settings.putString(CITIZENS_KEY, json.encodeToString(citizens))
    }

    fun addCitizen(citizen: User) {
        val list = getCitizens().toMutableList()
        list.add(citizen)
        saveCitizens(list)
    }

    // ─────────────────────────────────────────────
    // PROFILE (citizen — persists edits)
    // ─────────────────────────────────────────────

    fun getSavedProfile(): User? {
        val str = settings.getStringOrNull(PROFILE_KEY) ?: return null
        return try {
            json.decodeFromString(str)
        } catch (e: Exception) {
            null
        }
    }

    fun saveProfile(user: User) {
        settings.putString(PROFILE_KEY, json.encodeToString(user))
    }
}
