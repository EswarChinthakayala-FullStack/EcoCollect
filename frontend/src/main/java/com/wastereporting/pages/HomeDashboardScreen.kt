package com.wastereporting.pages

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.wastereporting.components.AppBadge
import com.wastereporting.components.AppCard
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.wastereporting.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

suspend fun fetchUserLocationSuspending(context: android.content.Context): String = withContext(Dispatchers.IO) {
    try {
        val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
        if (androidx.core.app.ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return@withContext "Downtown District"
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
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val locality = address.locality ?: address.subLocality ?: address.subAdminArea ?: "Bangalore"
                val thoroughfare = address.thoroughfare ?: address.subThoroughfare ?: "Downtown"
                "$thoroughfare, $locality"
            } else {
                "Lat: %.4f, Lng: %.4f".format(lat, lng)
            }
        } else {
            "Downtown District"
        }
    } catch (e: Exception) {
        "Downtown District"
    }
}

@Composable
fun HomeDashboardScreen(
    onNavigateToReport: () -> Unit,
    onNavigateToHistory: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onLogout: () -> Unit = {},
    onNavigateToCollectionStatus: () -> Unit = {},
    onNavigateToReportDetails: (Int) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var userName by remember { mutableStateOf("User") }
    var userLocationName by remember { mutableStateOf("Fetching Location...") }

    var totalReports by remember { mutableStateOf(0) }
    var completedCount by remember { mutableStateOf(0) }
    var pendingCount by remember { mutableStateOf(0) }

    var recentIssues by remember { mutableStateOf<List<com.wastereporting.network.IssueReport>>(emptyList()) }
    var isLoadingIssues by remember { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            coroutineScope.launch {
                userLocationName = fetchUserLocationSuspending(context)
            }
        } else {
            userLocationName = "Downtown District"
        }
    }

    LaunchedEffect(Unit) {
        val result = com.wastereporting.network.ApiService.getProfile()
        if (result.isSuccess) {
            userName = result.getOrNull()?.full_name ?: "User"
        }
        val dashboardResult = com.wastereporting.network.ApiService.getCitizenDashboard()
        if (dashboardResult.isSuccess) {
            val stats = dashboardResult.getOrNull()

            totalReports = stats?.total_reports ?: 0
            completedCount = stats?.resolved_reports ?: 0
            pendingCount = stats?.pending_reports ?: 0
        }
        val issuesResult = com.wastereporting.network.ApiService.getIssues()
        isLoadingIssues = false
        if (issuesResult.isSuccess) {
            recentIssues = issuesResult.getOrNull()?.take(3) ?: emptyList()
        }

        // Location check
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            userLocationName = fetchUserLocationSuspending(context)
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // bg-slate-50
            .verticalScroll(scrollState)
    ) {
        // Header (Green Background)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(Color(0xFF16A34A)) // bg-green-600
                .padding(top = 48.dp, bottom = 64.dp, start = 24.dp, end = 24.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.app_logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(24.dp).clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EcoCollect", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Good morning,", color = Color(0xFFDCFCE7), fontSize = 14.sp)
                        Text(userName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(userLocationName, color = Color.White, fontSize = 14.sp)
                }
            }
        }

        // Main Action Card (Overlapping header)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-32).dp)
                .padding(horizontal = 16.dp)
        ) {
            AppCard(modifier = Modifier.fillMaxWidth().clickable { onNavigateToReport() }) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Report Waste", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))
                        Text("Help keep the city clean", color = Color(0xFF64748B), fontSize = 14.sp)
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDCFCE7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Report", tint = Color(0xFF16A34A), modifier = Modifier.size(24.dp))
                    }
                }
            }
        }

        // Content Sections
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .offset(y = (-16).dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = "Total", tint = Color(0xFF8B5CF6), modifier = Modifier.size(24.dp))
                        Text(totalReports.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Submitted", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                }
                AppCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.HourglassEmpty, contentDescription = "Pending", tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                        Text(pendingCount.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Pending", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                }
                AppCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Completed", tint = Color(0xFF3B82F6), modifier = Modifier.size(24.dp))
                        Text(completedCount.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Completed", fontSize = 11.sp, color = Color(0xFF64748B))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader(
                title = "Recent Reports",
                actionText = "See All",
                onClickAction = onNavigateToHistory
            )

            if (isLoadingIssues) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF16A34A))
                }
            } else if (recentIssues.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No recent reports.", color = Color(0xFF64748B), fontSize = 14.sp)
                }
            } else {
                recentIssues.forEach { issue ->
                    val statusColor = when (issue.status.lowercase()) {
                        "completed" -> Color(0xFF16A34A)
                        else -> Color(0xFF3B82F6)
                    }
                    val statusBg = when (issue.status.lowercase()) {
                        "completed" -> Color(0xFFDCFCE7)
                        else -> Color(0xFFDBEAFE)
                    }
                    val statusIcon = when (issue.status.lowercase()) {
                        "completed" -> Icons.Default.CheckCircle
                        else -> Icons.Default.Schedule
                    }

                    AppCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        onClick = {
                            onNavigateToReportDetails(issue.id)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(statusBg, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(statusIcon, contentDescription = null, tint = statusColor)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    issue.title.orEmpty().ifEmpty { "Reported Issue" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF1E293B)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    issue.location ?: "Unknown Location",
                                    fontSize = 14.sp,
                                    color = Color(0xFF64748B),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    issue.created_at?.take(10) ?: "Unknown Date",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                issue.status.uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = statusColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String, actionText: String, onClickAction: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        if (actionText.isNotEmpty()) {
            Text(
                actionText, 
                color = Color(0xFF16A34A), 
                fontSize = 14.sp, 
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onClickAction() }.padding(4.dp)
            )
        }
    }
}
