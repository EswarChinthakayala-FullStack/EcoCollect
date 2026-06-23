package com.wastereporting.pages

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import com.wastereporting.components.ImageViewerDrawer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.wastereporting.R
import com.wastereporting.components.AppBadge
import com.wastereporting.components.AppCard
import com.wastereporting.components.NetworkImage
import com.wastereporting.components.MapLibreView
import com.wastereporting.components.rememberImagePicker
import com.wastereporting.network.ApiService
import com.wastereporting.network.IssueHistory
import com.wastereporting.network.IssueReport
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.maps.MapLibreMap
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorReportDetailsScreen(
    reportId: Int,
    onBack: () -> Unit,
    onNavigateToCompletedReports: () -> Unit
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    var report by remember { mutableStateOf<IssueReport?>(null) }
    var history by remember { mutableStateOf<List<IssueHistory>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedImageIndex by remember { mutableStateOf<Int?>(null) }

    // Live Tracking & Upload States
    var supervisorCoords by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    var distanceText by remember { mutableStateOf("Calculating...") }
    var durationText by remember { mutableStateOf("Calculating...") }

    var showCompleteDrawer by remember { mutableStateOf(false) }
    var completionImagesList by remember { mutableStateOf<List<ByteArray>>(emptyList()) }
    var completionRemarks by remember { mutableStateOf("") }
    var isUploadingAndCompleting by remember { mutableStateOf(false) }

    val imagePicker = rememberImagePicker { bytes ->
        if (bytes != null) {
            completionImagesList = completionImagesList + bytes
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePicker.launchCamera()
        }
    }

    LaunchedEffect(reportId) {
        val res = ApiService.getReportById(reportId)
        if (res.isSuccess) {
            val response = res.getOrNull()
            report = response?.issue ?: response?.report
            history = response?.history?.sortedByDescending { it.id } ?: emptyList()
        }
        isLoading = false
    }

    val currentReport = report
    val isCompleted = currentReport?.status?.equals("Completed", ignoreCase = true) == true || currentReport?.status?.equals("Resolved", ignoreCase = true) == true

    // Start location tracking loop if not completed
    LaunchedEffect(reportId, isCompleted) {
        if (reportId > 0 && !isCompleted) {
            while (true) {
                val details = fetchSupervisorLocationDetails(context)
                if (details != null) {
                    supervisorCoords = Pair(details.second, details.third)
                }
                kotlinx.coroutines.delay(10000) // update location every 10 seconds
            }
        }
    }

    // Update driving route when supervisor coordinates update
    LaunchedEffect(supervisorCoords, currentReport?.latitude, currentReport?.longitude) {
        val coords = supervisorCoords ?: return@LaunchedEffect
        val rep = currentReport ?: return@LaunchedEffect
        val incidentLat = if (rep.latitude != 0.0) rep.latitude else 12.9716
        val incidentLng = if (rep.longitude != 0.0) rep.longitude else 77.5946

        val result = ApiService.getDrivingRoute(
            startLat = coords.first,
            startLng = coords.second,
            endLat = incidentLat,
            endLng = incidentLng
        )
        if (result.isSuccess) {
            val routeDetails = result.getOrNull()
            val points = routeDetails?.coordinates?.map { LatLng(it.first, it.second) }
            if (!points.isNullOrEmpty()) {
                routePoints = points
            }
            val durationSecs = routeDetails?.duration ?: 0.0
            val distanceMtrs = routeDetails?.distance ?: 0.0

            val minutes = Math.round(durationSecs / 60.0)
            durationText = if (minutes < 1) "1 min" else "$minutes mins"

            val miles = distanceMtrs / 1609.34
            distanceText = String.format("%.1f mi", miles)
        } else {
            // Fallback L-shaped route
            routePoints = listOf(
                LatLng(coords.first, coords.second),
                LatLng(coords.first, incidentLng),
                LatLng(incidentLat, incidentLng)
            )
            durationText = "15 mins"
            distanceText = "1.2 mi"
        }
    }

    // Update map elements when coordinates or route changes
    LaunchedEffect(mapInstance, routePoints, supervisorCoords) {
        val map = mapInstance ?: return@LaunchedEffect
        val coords = supervisorCoords ?: return@LaunchedEffect
        val rep = currentReport ?: return@LaunchedEffect
        val incidentLat = if (rep.latitude != 0.0) rep.latitude else 12.9716
        val incidentLng = if (rep.longitude != 0.0) rep.longitude else 77.5946

        map.clear()

        // Draw route line dynamically
        if (routePoints.isNotEmpty()) {
            val polyline = PolylineOptions().apply {
                routePoints.forEach { add(it) }
                color(android.graphics.Color.parseColor("#2563EB"))
                width(6f)
            }
            map.addPolyline(polyline)
        }

        // Add destination and vehicle markers
        val binIcon = getMarkerIconFromVector(context, R.drawable.ic_incident_bin)
        val truckIcon = getMarkerIconFromVector(context, R.drawable.ic_truck)

        val incidentMarker = MarkerOptions().position(LatLng(incidentLat, incidentLng)).title("Incident Bin (${rep.category})")
        if (binIcon != null) {
            incidentMarker.icon(binIcon)
        }
        map.addMarker(incidentMarker)

        val truckMarker = MarkerOptions().position(LatLng(coords.first, coords.second)).title("My Location (Truck)")
        if (truckIcon != null) {
            truckMarker.icon(truckIcon)
        }
        map.addMarker(truckMarker)
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF3B82F6))
        }
        return
    }

    if (currentReport == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Report not found", color = Color(0xFFEF4444), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    val beforeUrls = (currentReport.image_url ?: currentReport.before_image)
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() } ?: emptyList()

    val afterUrls = (currentReport.completion_image_url ?: currentReport.after_image)
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() } ?: emptyList()

    val badgeVariant = when (currentReport.status.lowercase()) {
        "completed", "resolved" -> "success"
        "in_progress", "in progress" -> "info"
        "pending" -> "warning"
        else -> "info"
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color(0xFF1E293B), modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ticket #ID-${currentReport.id}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.weight(1f)
                )
                AppBadge(text = currentReport.status, variant = badgeVariant)
            }

            // Cleanup Images Card
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
                                LazyRow(
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
                                            NetworkImage(
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
                                    LazyRow(
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
                                                NetworkImage(
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

            // Description remarks Card
            AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "REPORTED DESCRIPTION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentReport.description ?: "No description remarks filed.",
                        fontSize = 14.sp,
                        color = Color(0xFF334155),
                        lineHeight = 20.sp
                    )
                }
            }

            // Details Card
            AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Category
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Category, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Filing Category", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                            Text(currentReport.category, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        }
                    }

                    // Filing Date
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Filing Date", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                            Text(ApiService.formatIsoDateTimeToIndian(currentReport.created_at), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        }
                    }

                    // Location
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Location Address", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                            Text(currentReport.address ?: currentReport.location ?: "Address unspecified", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        }
                    }

                    // Reporter Name (Citizen)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Reporter (Citizen)", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                            Text(currentReport.reporter_name ?: "Unknown Citizen", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        }
                    }
                }
            }

            // Conditional Map/Directions card
            val statusClean = currentReport.status.lowercase()
            val isInProgress = statusClean == "in_progress" || statusClean == "in progress" || statusClean == "assigned" || statusClean == "pending"
            if (isInProgress && !isCompleted) {
                AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val incidentLat = if (currentReport.latitude != 0.0) currentReport.latitude else 12.9716
                        val incidentLng = if (currentReport.longitude != 0.0) currentReport.longitude else 77.5946
                        val currentCoords = supervisorCoords

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE2E8F0))
                        ) {
                            if (currentCoords != null) {
                                MapLibreView(
                                    modifier = Modifier.fillMaxSize()
                                ) { map ->
                                    mapInstance = map
                                    map.setStyle("https://tiles.openfreemap.org/styles/liberty")
                                    map.cameraPosition = CameraPosition.Builder()
                                        .target(LatLng((incidentLat + currentCoords.first) / 2.0, (incidentLng + currentCoords.second) / 2.0))
                                        .zoom(13.5)
                                        .build()
                                }
                            } else {
                                // Loading/Placeholder state
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = Color(0xFF3B82F6), modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Acquiring GPS location...", color = Color(0xFF64748B), fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        if (currentCoords != null && routePoints.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Est. Arrival", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    Text(durationText, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Remaining Distance", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    Text(distanceText, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                }
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Real-Time Route to Incident", color = Color(0xFF64748B), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Button(
                                onClick = {
                                    try {
                                        val destLat = if (currentReport.latitude != 0.0) currentReport.latitude else 12.9716
                                        val destLng = if (currentReport.longitude != 0.0) currentReport.longitude else 77.5946
                                        val url = "https://www.google.com/maps/dir/?api=1&destination=$destLat,$destLng"
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                            setPackage("com.google.android.apps.maps")
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        val destLat = if (currentReport.latitude != 0.0) currentReport.latitude else 12.9716
                                        val destLng = if (currentReport.longitude != 0.0) currentReport.longitude else 77.5946
                                        val url = "https://www.google.com/maps/dir/?api=1&destination=$destLat,$destLng"
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6)),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Icon(Icons.Default.Directions, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Navigate", color = Color(0xFF3B82F6), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // Timeline Card
            AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "RESOLUTION TIMELINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (history.isEmpty()) {
                        SupervisorTimelineItem(
                            title = "Report Submitted",
                            time = ApiService.formatIsoDateTimeToIndian(currentReport.created_at),
                            remarks = "Report submitted successfully.",
                            isActive = true,
                            isLast = true
                        )
                    } else {
                        history.forEachIndexed { index, item ->
                            SupervisorTimelineItem(
                                title = item.status,
                                time = ApiService.formatIsoDateTimeToIndian(item.created_at),
                                remarks = item.remarks,
                                isActive = true,
                                isLast = index == history.size - 1
                            )
                        }
                    }
                }
            }

            // Status Actions Area
            if (!isCompleted) {
                Button(
                    onClick = {
                        showCompleteDrawer = true
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Complete Report", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            } else {
                Button(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF94A3B8)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Report Completed", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Image Viewer Drawer (ModalBottomSheet)
        if (selectedImageIndex != null) {
            val allImageUrls = beforeUrls + afterUrls
            ImageViewerDrawer(
                urls = allImageUrls,
                initialIndex = selectedImageIndex!!,
                title = "Ticket #ID-${currentReport.id} - ${currentReport.category}",
                description = currentReport.description ?: "",
                status = currentReport.status,
                date = "Reported: ${ApiService.formatIsoDateTimeToIndian(currentReport.created_at)}",
                onDismissRequest = { selectedImageIndex = null }
            )
        }
    }

    if (showCompleteDrawer) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = {
                if (!isUploadingAndCompleting) {
                    showCompleteDrawer = false
                    completionImagesList = emptyList()
                    completionRemarks = ""
                }
            },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Resolve & Complete Report",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1E293B)
                )

                Text(
                    "Please upload one or more photos showing the cleaned area and add resolution remarks to complete this ticket.",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )

                // Select images row / grid
                if (completionImagesList.isNotEmpty()) {
                    Text(
                        "Captured Photos (${completionImagesList.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    ) {
                        items(completionImagesList.size) { index ->
                            val bytes = completionImagesList[index]
                            val bitmap = remember(bytes) {
                                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            }
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Completion Image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .clickable {
                                            completionImagesList = completionImagesList.toMutableList().apply { removeAt(index) }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }

                // Add Photo options card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp)),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Camera Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPermission) {
                                    imagePicker.launchCamera()
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Camera", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(Color(0xFFE2E8F0))
                    )
                    
                    // Gallery Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable {
                                imagePicker.launchGallery()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Image, contentDescription = "Gallery", tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Gallery", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        }
                    }
                }

                // Remarks text field
                OutlinedTextField(
                    value = completionRemarks,
                    onValueChange = { completionRemarks = it },
                    label = { Text("Resolution Remarks") },
                    placeholder = { Text("Describe the cleanup resolution details...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF16A34A),
                        focusedLabelColor = Color(0xFF16A34A)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = {
                            showCompleteDrawer = false
                            completionImagesList = emptyList()
                            completionRemarks = ""
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isUploadingAndCompleting
                    ) {
                        Text("Cancel", color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (completionImagesList.isEmpty()) return@Button
                            isUploadingAndCompleting = true
                            coroutineScope.launch {
                                val uploadedUrls = mutableListOf<String>()
                                var uploadSuccess = true
                                
                                for (bytes in completionImagesList) {
                                    val url = ApiService.uploadImage(bytes)
                                    if (url != null) {
                                        uploadedUrls.add(url)
                                    } else {
                                        uploadSuccess = false
                                    }
                                }
                                
                                if (uploadSuccess && uploadedUrls.isNotEmpty()) {
                                    val finalImageUrl = uploadedUrls.joinToString(",")
                                    val res = ApiService.completeIssue(
                                        issueId = currentReport.id,
                                        completionImageUrl = finalImageUrl,
                                        remarks = completionRemarks.ifBlank { "Cleanup resolved and reported by supervisor" }
                                    )
                                    if (res.isSuccess) {
                                        report = currentReport.copy(
                                            status = "Completed",
                                            completion_image_url = finalImageUrl
                                        )
                                        showCompleteDrawer = false
                                        completionImagesList = emptyList()
                                        completionRemarks = ""
                                        onNavigateToCompletedReports()
                                    }
                                }
                                isUploadingAndCompleting = false
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = completionImagesList.isNotEmpty() && !isUploadingAndCompleting
                    ) {
                        if (isUploadingAndCompleting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Complete", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SupervisorTimelineItem(
    title: String,
    time: String,
    remarks: String?,
    isActive: Boolean,
    isLast: Boolean
) {
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
                        .height(56.dp)
                        .background(if (isActive) Color(0xFF3B82F6).copy(alpha = 0.4f) else Color(0xFFE2E8F0))
                )
            }
        }
        Column(modifier = Modifier.padding(start = 12.dp, bottom = if (isLast) 0.dp else 16.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            if (!remarks.isNullOrBlank()) {
                Text(
                    text = remarks,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Text(
                text = time,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(top = 4.dp)
            )
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
