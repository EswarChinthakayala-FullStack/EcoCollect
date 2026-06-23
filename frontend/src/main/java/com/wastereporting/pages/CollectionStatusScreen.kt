package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wastereporting.components.AppBadge
import com.wastereporting.components.AppButton
import com.wastereporting.components.AppCard
import com.wastereporting.components.MapLibreView
import com.wastereporting.network.ApiService
import com.wastereporting.network.IssueReport
import com.wastereporting.network.SupervisorDetailsResponse
import androidx.compose.ui.platform.LocalContext
import com.wastereporting.R
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition

@Composable
fun CollectionStatusScreen(
    onBack: () -> Unit,
    onTrackLive: (Int) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var report by remember { mutableStateOf<IssueReport?>(null) }
    var assignedSupervisor by remember { mutableStateOf<SupervisorDetailsResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val result = ApiService.getIssues()
        if (result.isSuccess) {
            val list = result.getOrNull() ?: emptyList()
            val latestReport = list.maxByOrNull { it.id }
            if (latestReport != null) {
                val detailResult = ApiService.getReportById(latestReport.id)
                if (detailResult.isSuccess) {
                    val detailResponse = detailResult.getOrNull()
                    report = detailResponse?.issue ?: detailResponse?.report ?: latestReport
                    assignedSupervisor = detailResponse?.assigned_supervisor
                } else {
                    report = latestReport
                }
            }
        } else {
            errorMessage = result.exceptionOrNull()?.message ?: "Failed to fetch status details"
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color(0xFF1E293B), modifier = Modifier.size(28.dp))
            }
            Text(
                "Collection Status",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF1E293B),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF16A34A))
            }
            return@Column
        }

        if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
            return@Column
        }

        val currentReport = report
        if (currentReport == null) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No Reports Found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Collection status and route tracking details will appear here once you file a waste report and it gets accepted.",
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
            return@Column
        }

        val statusClean = currentReport.status.lowercase()
        val isInProgress = statusClean == "in_progress" || statusClean == "in progress" || statusClean == "assigned"
        val isCompleted = statusClean == "completed" || statusClean == "resolved"

        // Map area - ONLY show for In Progress / En Route state, outside scrollable Column to avoid nested scroll layout crashes
        if (isInProgress) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFE2E8F0))
            ) {
                MapLibreView(
                    modifier = Modifier.fillMaxSize()
                ) { map ->
                    map.setStyle("https://tiles.openfreemap.org/styles/liberty")
                    val lat = if (currentReport.latitude != 0.0) currentReport.latitude else 12.9716
                    val lng = if (currentReport.longitude != 0.0) currentReport.longitude else 77.5946
                    val latLng = LatLng(lat, lng)

                    map.cameraPosition = CameraPosition.Builder()
                        .target(latLng)
                        .zoom(14.5)
                        .build()
                    
                    val binIcon = getMarkerIconFromVector(context, R.drawable.ic_incident_bin)
                    val truckIcon = getMarkerIconFromVector(context, R.drawable.ic_truck)

                    val incidentMarker = MarkerOptions().position(latLng).title("Report Location")
                    if (binIcon != null) {
                        incidentMarker.icon(binIcon)
                    }
                    map.addMarker(incidentMarker)

                    // Place vehicle marker using real supervisor location from database if available
                    val supervisor = assignedSupervisor
                    if (supervisor?.latitude != null && supervisor.longitude != null) {
                        val truckMarker = MarkerOptions().position(LatLng(supervisor.latitude, supervisor.longitude)).title("Supervisor ${supervisor.name} (Truck)")
                        if (truckIcon != null) {
                            truckMarker.icon(truckIcon)
                        }
                        map.addMarker(truckMarker)
                    } else {
                        val truckMarker = MarkerOptions().position(LatLng(lat + 0.003, lng + 0.003)).title("Recycling Truck #402")
                        if (truckIcon != null) {
                            truckMarker.icon(truckIcon)
                        }
                        map.addMarker(truckMarker)
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {

            // Main Collection details card
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isInProgress) "${currentReport.category} Collection" else currentReport.category,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.weight(1f)
                        )
                        val badgeText = when {
                            isInProgress -> "En Route"
                            isCompleted -> "Completed"
                            else -> "Pending"
                        }
                        val badgeVariant = when {
                            isInProgress -> "info"
                            isCompleted -> "success"
                            else -> "warning"
                        }
                        AppBadge(text = badgeText, variant = badgeVariant)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    
                    if (isInProgress) {
                        val supervisor = assignedSupervisor
                        val supText = if (supervisor != null) {
                            "Vehicle #402 • Supervisor: ${supervisor.name} (${supervisor.employee_id})"
                        } else if (currentReport.assigned_supervisor_id != null) {
                            "Vehicle #402 • Supervisor: SUP-${currentReport.assigned_supervisor_id}"
                        } else {
                            "Vehicle #402 • Unassigned"
                        }
                        Text(
                            text = supText,
                            color = Color(0xFF64748B),
                            fontSize = 14.sp
                        )
                    } else {
                        Text(
                            text = "Ticket #ID-${currentReport.id}",
                            color = Color(0xFF64748B),
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Arrival Estimate - ONLY show for In Progress / En Route state
                    if (isInProgress) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDBEAFE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF2563EB))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Estimated Arrival", fontSize = 12.sp, color = Color(0xFF64748B))
                                Text("10:45 AM (15 mins)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    } else if (isCompleted) {
                        // Thank you message for completed report
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFBBF7D0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Status Update", fontSize = 12.sp, color = Color(0xFF15803D))
                                Text("Cleaned up & resolved successfully!", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF166534))
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    } else {
                        // Pending status message
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFEF3C7))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFDE68A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFD97706))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Awaiting Assignment", fontSize = 12.sp, color = Color(0xFFB45309))
                                Text("Awaiting assignment to a cleanup crew.", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF92400E))
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Ticket information fields
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Category, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Category: ", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text(currentReport.category, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reported Date: ", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text(ApiService.formatIsoDateTimeToIndian(currentReport.created_at), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                        }
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp).padding(top = 2.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Location: ", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text(currentReport.address ?: currentReport.location ?: "Unknown Location", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155), modifier = Modifier.weight(1f))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Timeline
                    Text("TIMELINE PROGRESS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(16.dp))

                    if (isCompleted) {
                        StatusTimelineItem(title = "Report Resolved & Cleaned", time = if (currentReport.resolved_at.isNullOrBlank()) "Resolved" else ApiService.formatIsoDateTimeToIndian(currentReport.resolved_at), isActive = true, isLast = true)
                    } else if (isInProgress) {
                        StatusTimelineItem(title = "Crew Dispatched (En Route)", time = "Ongoing", isActive = true, isLast = false)
                        StatusTimelineItem(title = "Report Accepted", time = if (currentReport.updated_at.isNullOrBlank()) "Updated" else ApiService.formatIsoDateTimeToIndian(currentReport.updated_at), isActive = true, isLast = true)
                    } else {
                        StatusTimelineItem(title = "Awaiting Crew Assignment", time = "Pending", isActive = false, isLast = false)
                        StatusTimelineItem(title = "Report Submitted", time = if (currentReport.created_at.isNullOrBlank()) "Submitted" else ApiService.formatIsoDateTimeToIndian(currentReport.created_at), isActive = true, isLast = true)
                    }

                    // Track Vehicle Live Button - ONLY show for In Progress / En Route state
                    if (isInProgress) {
                        Spacer(modifier = Modifier.height(32.dp))
                        AppButton(
                            text = "Track Vehicle Live",
                            onClick = { onTrackLive(currentReport.id) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatusTimelineItem(
    title: String,
    time: String,
    isActive: Boolean,
    isLast: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(20.dp)) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isActive) Color(0xFF16A34A) else Color(0xFFCBD5E1))
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(32.dp)
                        .background(if (isActive) Color(0xFF16A34A).copy(alpha = 0.4f) else Color(0xFFE2E8F0))
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
            Text(time, fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
    }
}

private fun getMarkerIconFromVector(context: android.content.Context, vectorResId: Int): org.maplibre.android.annotations.Icon? {
    return try {
        val drawable = androidx.core.content.ContextCompat.getDrawable(context, vectorResId) ?: return null
        val bitmap = android.graphics.Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            android.graphics.Bitmap.Config.ARGB_8888
        )
        val canvas = android.graphics.Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        org.maplibre.android.annotations.IconFactory.getInstance(context).fromBitmap(bitmap)
    } catch (e: Exception) {
        null
    }
}

