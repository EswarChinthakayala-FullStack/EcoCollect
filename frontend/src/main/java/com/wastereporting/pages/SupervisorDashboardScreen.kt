package com.wastereporting.pages

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.wastereporting.R
import com.wastereporting.components.AppBadge
import com.wastereporting.components.AppCard
import com.wastereporting.components.NetworkImage
import com.wastereporting.network.ApiService
import com.wastereporting.network.IssueReport
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

suspend fun fetchSupervisorLocationDetails(context: android.content.Context): Triple<String, Double, Double>? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    try {
        val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
        if (androidx.core.app.ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return@withContext null
        }

        val providers = locationManager.getProviders(true)
        var bestLocation: android.location.Location? = null
        for (provider in providers) {
            val loc = locationManager.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                bestLocation = loc
            }
        }

        if (bestLocation != null) {
            val lat = bestLocation.latitude
            val lng = bestLocation.longitude
            val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            val name = if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val locality = address.locality ?: address.subLocality ?: address.subAdminArea ?: "Bangalore"
                val thoroughfare = address.thoroughfare ?: address.subThoroughfare ?: "Downtown"
                "$thoroughfare, $locality"
            } else {
                "Downtown District"
            }
            Triple(name, lat, lng)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0 // Earth radius in km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return r * c
}

@Composable
fun SupervisorDashboardScreen(
    onNavigateToPendingReports: () -> Unit = {},
    onNavigateToCompletedReports: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToReportDetails: (Int) -> Unit,
    onNavigateToNotifications: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    var supervisorName by remember { mutableStateOf("Supervisor") }
    var supervisorArea by remember { mutableStateOf("Fetching Location...") }
    var employeeId by remember { mutableStateOf("Unknown EMP") }

    var assignedReports by remember { mutableStateOf(0) }
    var pendingReportsCount by remember { mutableStateOf(0) }
    var completedReportsCount by remember { mutableStateOf(0) }

    var recentIssues by remember { mutableStateOf<List<IssueReport>>(emptyList()) }
    var nearbyIssues by remember { mutableStateOf<List<IssueReport>>(emptyList()) }
    var isLoadingRecent by remember { mutableStateOf(true) }
    var isLoadingNearby by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var supervisorLat by remember { mutableStateOf(12.9716) }
    var supervisorLng by remember { mutableStateOf(77.5946) }
    var hasCoordinates by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            coroutineScope.launch {
                val details = fetchSupervisorLocationDetails(context)
                if (details != null) {
                    supervisorArea = details.first
                    supervisorLat = details.second
                    supervisorLng = details.third
                    hasCoordinates = true
                    ApiService.updateSupervisorLocation(details.second, details.third)
                } else {
                    supervisorArea = "Downtown District"
                }
            }
        } else {
            supervisorArea = "Downtown District"
        }
    }

    LaunchedEffect(Unit) {
        // Profile
        coroutineScope.launch {
            val profileRes = ApiService.getProfile()
            if (profileRes.isSuccess) {
                val p = profileRes.getOrNull()
                supervisorName = p?.full_name ?: "Supervisor"
                employeeId = p?.employee_id ?: "Unknown EMP"
                if (supervisorArea == "Fetching Location...") {
                    supervisorArea = p?.assigned_area ?: "Unknown Area"
                }
            }
        }

        // Dashboard Stats
        coroutineScope.launch {
            val dashRes = ApiService.getSupervisorDashboard()
            if (dashRes.isSuccess) {
                val d = dashRes.getOrNull()
                assignedReports = d?.assigned_reports ?: 0
                pendingReportsCount = d?.pending_reports ?: 0
                completedReportsCount = d?.completed_reports ?: 0
            }
        }

        // Fetch Assigned Reports (both for Recent Assigned and Nearby Reports sections)
        coroutineScope.launch {
            val result = ApiService.getSupervisorReports(null)
            isLoadingRecent = false
            isLoadingNearby = false
            if (result.isSuccess) {
                val allAssigned = result.getOrNull() ?: emptyList()
                val activeAssigned = allAssigned.filter { 
                    it.status.lowercase() != "completed" && it.status.lowercase() != "resolved" 
                }
                
                recentIssues = activeAssigned.sortedByDescending { it.id }.take(3)
                nearbyIssues = activeAssigned
            } else {
                errorMessage = "Failed to load assigned reports"
            }
        }

        // Permission & Location check
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            coroutineScope.launch {
                val details = fetchSupervisorLocationDetails(context)
                if (details != null) {
                    supervisorArea = details.first
                    supervisorLat = details.second
                    supervisorLng = details.third
                    hasCoordinates = true
                    ApiService.updateSupervisorLocation(details.second, details.third)
                } else {
                    supervisorArea = "Downtown District"
                }
            }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Periodic Location Update Loop
        coroutineScope.launch {
            while (true) {
                try {
                    val details = fetchSupervisorLocationDetails(context)
                    if (details != null) {
                        supervisorLat = details.second
                        supervisorLng = details.third
                        hasCoordinates = true
                        ApiService.updateSupervisorLocation(details.second, details.third)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(10000)
            }
        }
    }

    val sortedNearbyIssues = remember(nearbyIssues, supervisorLat, supervisorLng) {
        nearbyIssues.sortedBy { issue ->
            calculateDistance(supervisorLat, supervisorLng, issue.latitude, issue.longitude)
        }
    }

    Scaffold(
        bottomBar = {
            SupervisorBottomNav(
                currentTab = "Reports",
                onTabSelected = { tab ->
                    when (tab) {
                        "History" -> onNavigateToCompletedReports()
                        "Profile" -> onNavigateToProfile()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC)) // bg-slate-50
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            // Header (Blue Background with notification bell and location pill)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Color(0xFF3B82F6)) // bg-blue-500
                    .padding(top = 48.dp, bottom = 64.dp, start = 24.dp, end = 24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(R.drawable.app_logo),
                                contentDescription = "Logo",
                                modifier = Modifier.size(24.dp).clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("EcoCollect", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .clickable { onNavigateToNotifications() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Welcome,", color = Color(0xFFDBEAFE), fontSize = 14.sp)
                            Text(supervisorName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("$employeeId", color = Color(0xFFBFDBFE), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(supervisorArea, color = Color.White, fontSize = 14.sp)
                    }
                }
            }

            // Stats Cards Row (3 cards overlapping the header)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-32).dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    count = assignedReports.toString(),
                    label = "Assigned",
                    icon = Icons.AutoMirrored.Filled.ListAlt,
                    iconColor = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToPendingReports
                )
                StatCard(
                    count = pendingReportsCount.toString(),
                    label = "Pending",
                    icon = Icons.Default.HourglassEmpty,
                    iconColor = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToPendingReports
                )
                StatCard(
                    count = completedReportsCount.toString(),
                    label = "Completed",
                    icon = Icons.Default.CheckCircle,
                    iconColor = Color(0xFF16A34A),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToCompletedReports
                )
            }

            // Lists Content Section
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .offset(y = (-16).dp)
            ) {
                // Section 1: Recent Assigned Reports
                SupervisorSectionHeader(
                    title = "Recent Assigned",
                    actionText = "See All",
                    onClickAction = onNavigateToPendingReports
                )

                if (isLoadingRecent) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF3B82F6))
                    }
                } else if (recentIssues.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No recent assigned pending reports.", color = Color(0xFF64748B), fontSize = 14.sp)
                    }
                } else {
                    recentIssues.forEach { issue ->
                        val distanceKm = calculateDistance(supervisorLat, supervisorLng, issue.latitude, issue.longitude)
                        val distanceStr = if (distanceKm < 1.0) {
                            "${(distanceKm * 1000).toInt()} m"
                        } else {
                            "%.1f km".format(distanceKm)
                        }

                        ReportCard(
                            title = (issue.title ?: "").ifEmpty { "Issue ${issue.category}" },
                            category = "RPT-848${issue.id}",
                            address = issue.location ?: "Unknown Location",
                            time = issue.created_at?.take(10) ?: "Unknown Date",
                            distance = distanceStr,
                            status = issue.status,
                            imageUrl = issue.image_url,
                            onClick = { onNavigateToReportDetails(issue.id) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 2: Nearby Reports (Sorted by distance)
                SupervisorSectionHeader(
                    title = "Nearby Reports",
                    actionText = ""
                )

                if (isLoadingNearby) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF3B82F6))
                    }
                } else if (errorMessage != null) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                    }
                } else if (sortedNearbyIssues.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("No active reports nearby.", color = Color(0xFF64748B), fontSize = 14.sp)
                    }
                } else {
                    sortedNearbyIssues.forEach { issue ->
                        val distanceKm = calculateDistance(supervisorLat, supervisorLng, issue.latitude, issue.longitude)
                        val distanceStr = if (distanceKm < 1.0) {
                            "${(distanceKm * 1000).toInt()} m"
                        } else {
                            "%.1f km".format(distanceKm)
                        }

                        ReportCard(
                            title = (issue.title ?: "").ifEmpty { "Issue ${issue.category}" },
                            category = "RPT-848${issue.id}",
                            address = issue.location ?: "Unknown Location",
                            time = issue.created_at?.take(10) ?: "Unknown Date",
                            distance = distanceStr,
                            status = issue.status,
                            imageUrl = issue.image_url,
                            onClick = { onNavigateToReportDetails(issue.id) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SupervisorSectionHeader(title: String, actionText: String, onClickAction: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontSize = 18.sp)
        if (actionText.isNotEmpty()) {
            Text(
                actionText,
                color = Color(0xFF3B82F6),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onClickAction() }.padding(4.dp)
            )
        }
    }
}

@Composable
fun StatCard(
    count: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    AppCard(modifier = modifier.clickable { onClick() }) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
fun ReportCard(
    title: String,
    category: String,
    address: String,
    time: String,
    distance: String,
    status: String,
    statusColor: Color = Color.Gray,
    statusBg: Color = Color.LightGray,
    imageUrl: String? = null,
    onClick: () -> Unit
) {
    AppCard(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                val imgUrl = imageUrl?.split(",")?.firstOrNull()?.trim()
                if (!imgUrl.isNullOrEmpty()) {
                    NetworkImage(
                        url = imgUrl,
                        contentDescription = "Report Image",
                        modifier = Modifier.fillMaxSize(),
                        loadingPlaceholder = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                            }
                        },
                        errorPlaceholder = {
                            Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(24.dp))
                        }
                    )
                } else {
                    Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(24.dp))
                }
            }

            Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B), modifier = Modifier.weight(1f))
                    AppBadge(text = status, variant = if (status.lowercase() == "pending") "warning" else if (status.lowercase() == "completed") "success" else "info")
                }
                Text(category, fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(vertical = 4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(address, fontSize = 12.sp, color = Color(0xFF64748B), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(time, fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                    Text(distance, fontSize = 12.sp, color = Color(0xFF3B82F6), fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun SupervisorBottomNav(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Reports") },
            label = { Text("Reports") },
            selected = currentTab == "Reports",
            onClick = { onTabSelected("Reports") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF3B82F6),
                selectedTextColor = Color(0xFF3B82F6),
                indicatorColor = Color(0xFFDBEAFE)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.History, contentDescription = "History") },
            label = { Text("History") },
            selected = currentTab == "History",
            onClick = { onTabSelected("History") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF3B82F6),
                selectedTextColor = Color(0xFF3B82F6),
                indicatorColor = Color(0xFFDBEAFE)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = currentTab == "Profile",
            onClick = { onTabSelected("Profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF3B82F6),
                selectedTextColor = Color(0xFF3B82F6),
                indicatorColor = Color(0xFFDBEAFE)
            )
        )
    }
}
