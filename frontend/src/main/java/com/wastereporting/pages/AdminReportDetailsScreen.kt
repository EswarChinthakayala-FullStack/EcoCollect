package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.itemsIndexed
import com.wastereporting.components.AppBadge
import com.wastereporting.components.AppCard
import androidx.compose.ui.platform.LocalContext
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import com.wastereporting.components.MapLibreView
import com.wastereporting.R

@Composable
fun AdminReportDetailsScreen(
    reportId: Int,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    var report by remember { mutableStateOf<com.wastereporting.network.IssueReport?>(null) }
    var history by remember { mutableStateOf<List<com.wastereporting.network.IssueHistory>>(emptyList()) }
    var assignedSupervisor by remember { mutableStateOf<com.wastereporting.network.SupervisorDetailsResponse?>(null) }
    var supervisorsList by remember { mutableStateOf<List<com.wastereporting.network.AdminSupervisorStats>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isAssigning by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(reportId) {
        val result = com.wastereporting.network.ApiService.getReportById(reportId)
        if (result.isSuccess) {
            val response = result.getOrNull()
            report = response?.issue ?: response?.report
            history = response?.history ?: emptyList()
            assignedSupervisor = response?.assigned_supervisor
        }
        
        val supResult = com.wastereporting.network.ApiService.getAdminSupervisors()
        if (supResult.isSuccess) {
            supervisorsList = supResult.getOrNull() ?: emptyList()
        }
        
        isLoading = false
    }

    val nearbySupervisors = remember(supervisorsList, report) {
        val rep = report
        if (rep == null) emptyList()
        else {
            supervisorsList
                .filter { it.is_active != false }
                .map { sup ->
                    val dist = if (sup.latitude != null && sup.longitude != null && rep.latitude != 0.0 && rep.longitude != 0.0) {
                        calculateDistance(rep.latitude, rep.longitude, sup.latitude, sup.longitude)
                    } else {
                        Double.MAX_VALUE
                    }
                    sup to dist
                }
                .sortedBy { it.second }
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF0F172A))
        }
        return
    }

    val currentReport = report ?: return

    val beforeUrls = (currentReport.image_url ?: currentReport.before_image)
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() } ?: emptyList()

    val afterUrls = (currentReport.completion_image_url ?: currentReport.after_image)
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() } ?: emptyList()

    val allImageUrls = beforeUrls + afterUrls
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .verticalScroll(scrollState)
        ) {
            // Image Header & Status
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color(0xFFE2E8F0))
                    .then(
                        if (allImageUrls.isNotEmpty()) {
                            Modifier.clickable { selectedImageIndex = 0 }
                        } else {
                            Modifier
                        }
                    )
            ) {
                if (beforeUrls.isNotEmpty()) {
                    com.wastereporting.components.NetworkImage(
                        url = beforeUrls.first(),
                        contentDescription = "Main Image Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.align(Alignment.Center).size(64.dp))
                }
                
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    
                    AppBadge(text = currentReport.status, variant = if (currentReport.status == "Completed") "success" else "warning")
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Main Info
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(currentReport.category, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text("RPT-${currentReport.id} • Created ${com.wastereporting.network.ApiService.formatIsoDateTimeToIndian(currentReport.created_at)}", fontSize = 14.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 4.dp))
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEF2F2))
                            .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                            Text("High", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cleanup Images Card
                val isCompleted = currentReport.status.equals("Completed", ignoreCase = true) || currentReport.status.equals("Resolved", ignoreCase = true)
                AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "CLEANUP IMAGES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Before images
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Submission (Before)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                if (beforeUrls.isNotEmpty()) {
                                    androidx.compose.foundation.lazy.LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth().height(120.dp)
                                    ) {
                                        itemsIndexed(beforeUrls) { index, url ->
                                            Box(
                                                modifier = Modifier
                                                    .size(120.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFE2E8F0))
                                                    .clickable { selectedImageIndex = index }
                                            ) {
                                                com.wastereporting.components.NetworkImage(
                                                    url = url,
                                                    contentDescription = "Before cleanup",
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF1F5F9)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Image, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(32.dp))
                                    }
                                }
                            }

                            // After images
                            if (isCompleted) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Resolved Cleanup (After)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF94A3B8),
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                    if (afterUrls.isNotEmpty()) {
                                        androidx.compose.foundation.lazy.LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth().height(120.dp)
                                        ) {
                                            itemsIndexed(afterUrls) { index, url ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(120.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFFE2E8F0))
                                                        .clickable { selectedImageIndex = beforeUrls.size + index }
                                                ) {
                                                    com.wastereporting.components.NetworkImage(
                                                        url = url,
                                                        contentDescription = "After cleanup",
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(120.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFD1FAE5)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(32.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            // Description
            Text("Description", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
            Text(
                currentReport.description ?: "No description provided.",
                fontSize = 14.sp,
                color = Color(0xFF475569),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Location & Map
            val context = LocalContext.current
            Text("Location", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
            AppCard(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
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
                                .zoom(14.0)
                                .build()
                            
                            val binIcon = getMarkerIconFromVector(context, R.drawable.ic_incident_bin)
                            val incidentMarker = MarkerOptions().position(latLng).title("Report Location")
                            if (binIcon != null) {
                                incidentMarker.icon(binIcon)
                            }
                            map.addMarker(incidentMarker)

                            // Add supervisor en-route marker if assigned and coordinates are available
                            val supervisor = assignedSupervisor
                            if (supervisor?.latitude != null && supervisor.longitude != null) {
                                val truckIcon = getMarkerIconFromVector(context, R.drawable.ic_truck)
                                val truckMarker = MarkerOptions().position(LatLng(supervisor.latitude, supervisor.longitude))
                                    .title("Supervisor ${supervisor.name} (Truck)")
                                if (truckIcon != null) {
                                    truckMarker.icon(truckIcon)
                                }
                                map.addMarker(truckMarker)
                            }
                        }
                    }
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(currentReport.address ?: currentReport.location ?: "Unknown Location", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1E293B))
                            Text("GPS: ${currentReport.latitude}° N, ${currentReport.longitude}° E", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Timeline & Assignment
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Reporter
                AppCard(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    Column(modifier = Modifier.fillMaxHeight().padding(16.dp)) {
                        Text("Reported By", fontSize = 12.sp, color = Color(0xFF64748B))
                        Row(modifier = Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            val citizenName = currentReport.reporter_name ?: "Citizen"
                            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFDBEAFE)), contentAlignment = Alignment.Center) {
                                Text(citizenName.take(1).uppercase(), color = Color(0xFF1D4ED8), fontWeight = FontWeight.Bold)
                            }
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(citizenName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                                Text("User ID: CTZ-${currentReport.citizen_id}", fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                }
                
                // Supervisor
                AppCard(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    Column(modifier = Modifier.fillMaxHeight().padding(16.dp)) {
                        Text("Assigned To", fontSize = 12.sp, color = Color(0xFF64748B))
                        Row(modifier = Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (assignedSupervisor == null) {
                                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFF1F5F9)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.PersonOutline, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                                }
                                Text("Unassigned", fontSize = 14.sp, color = Color(0xFF64748B), modifier = Modifier.padding(start = 12.dp))
                            } else {
                                val supervisor = assignedSupervisor!!
                                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFEDE9FE)), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.SupervisorAccount, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                                }
                                Column(modifier = Modifier.padding(start = 12.dp)) {
                                    Text(supervisor.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                                    Text("ID: ${supervisor.employee_id}", fontSize = 10.sp, color = Color(0xFF64748B))
                                }
                            }
                        }
                    }
                }
            }

            val status = currentReport.status.lowercase()
            val isReportCompleted = status.contains("completed") || status.contains("resolved")
            
            if (!isReportCompleted && assignedSupervisor == null) {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Assign Supervisor", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                AppCard(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "NEARBY ACTIVE SUPERVISORS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        if (nearbySupervisors.isEmpty()) {
                            Text("No active supervisors found to assign.", color = Color(0xFF64748B), fontSize = 14.sp)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                nearbySupervisors.take(5).forEach { (sup, dist) ->
                                    val isCurrent = assignedSupervisor?.id == sup.id
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isCurrent) Color(0xFFEDE9FE) else Color(0xFFF1F5F9)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = if (isCurrent) Color(0xFF8B5CF6) else Color(0xFF94A3B8),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(sup.full_name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                                                val distText = if (dist == Double.MAX_VALUE) "Location unmapped" else "${"%.2f".format(dist)} km away"
                                                Text("${sup.employee_id} • $distText", fontSize = 12.sp, color = Color(0xFF64748B))
                                            }
                                        }
                                        
                                        val scope = rememberCoroutineScope()
                                        Button(
                                            onClick = {
                                                if (isCurrent) return@Button
                                                isAssigning = true
                                                scope.launch {
                                                    val res = com.wastereporting.network.ApiService.assignReport(currentReport.id, sup.id)
                                                    if (res.isSuccess) {
                                                        val refreshRes = com.wastereporting.network.ApiService.getReportById(currentReport.id)
                                                        if (refreshRes.isSuccess) {
                                                            val response = refreshRes.getOrNull()
                                                            report = response?.issue ?: response?.report
                                                            history = response?.history ?: emptyList()
                                                            assignedSupervisor = response?.assigned_supervisor
                                                        }
                                                    }
                                                    isAssigning = false
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isCurrent) Color(0xFFDCFCE7) else Color(0xFF3B82F6),
                                                contentColor = if (isCurrent) Color(0xFF16A34A) else Color.White
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.height(32.dp),
                                            enabled = !isAssigning
                                        ) {
                                            if (isCurrent) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Assigned", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            } else {
                                                Text("Assign", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Timeline
            Text("Timeline", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
            AppCard(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (history.isEmpty()) {
                        TimelineItem(
                            title = "Report Submitted",
                            time = com.wastereporting.network.ApiService.formatIsoDateTimeToIndian(currentReport.created_at),
                            isActive = true,
                            isLast = true
                        )
                    } else {
                        history.forEachIndexed { index, h ->
                            TimelineItem(
                                title = h.status,
                                time = com.wastereporting.network.ApiService.formatIsoDateTimeToIndian(h.created_at),
                                isActive = true,
                                isLast = index == history.size - 1
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            // Back Button
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0F172A))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back to Reports", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Image Viewer Drawer (ModalBottomSheet)
    if (selectedImageIndex != null) {
        com.wastereporting.components.ImageViewerDrawer(
            urls = allImageUrls,
            initialIndex = selectedImageIndex!!,
            title = "Ticket #RPT-${currentReport.id} - ${currentReport.category}",
            description = currentReport.description ?: "",
            status = currentReport.status,
            date = "Reported: ${com.wastereporting.network.ApiService.formatIsoDateTimeToIndian(currentReport.created_at)}",
            onDismissRequest = { selectedImageIndex = null }
        )
    }
}
}

@Composable
private fun TimelineItem(title: String, time: String, isActive: Boolean, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (isActive) Color(0xFF3B82F6) else Color(0xFFE2E8F0))
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(32.dp)
                        .background(if (isActive) Color(0xFF3B82F6).copy(alpha = 0.5f) else Color(0xFFE2E8F0))
                )
            }
        }
        Column(modifier = Modifier.padding(start = 12.dp, bottom = if (isLast) 0.dp else 16.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal, color = if (isActive) Color(0xFF1E293B) else Color(0xFF64748B))
            Text(time, fontSize = 12.sp, color = Color(0xFF94A3B8), modifier = Modifier.padding(top = 2.dp))
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

