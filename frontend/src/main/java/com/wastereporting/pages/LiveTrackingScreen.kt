package com.wastereporting.pages

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.wastereporting.R
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.maps.MapLibreMap

@Composable
fun LiveTrackingScreen(
    reportId: Int,
    onBack: () -> Unit,
    onDetails: () -> Unit
) {
    val context = LocalContext.current
    var report by remember { mutableStateOf<IssueReport?>(null) }
    var assignedSupervisor by remember { mutableStateOf<SupervisorDetailsResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var distanceText by remember { mutableStateOf("Calculating...") }
    var durationText by remember { mutableStateOf("Calculating...") }
    var showDetailsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(reportId) {
        while (true) {
            val result = ApiService.getReportById(reportId)
            if (result.isSuccess) {
                val response = result.getOrNull()
                report = response?.issue ?: response?.report
                assignedSupervisor = response?.assigned_supervisor
            } else {
                if (report == null) {
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to load live tracking details"
                }
            }
            isLoading = false
            kotlinx.coroutines.delay(5000) // Poll database every 5 seconds
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF16A34A))
        }
        return
    }

    if (errorMessage != null || report == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = errorMessage ?: "No active report found for live tracking.",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))
                AppButton(text = "Go Back", onClick = onBack)
            }
        }
        return
    }

    val currentReport = report!!
    val incidentLat = if (currentReport.latitude != 0.0) currentReport.latitude else 12.9716
    val incidentLng = if (currentReport.longitude != 0.0) currentReport.longitude else 77.5946

    // Retrieve active coordinates from supervisor. Fall back to slightly offset mock coordinates if supervisor has not reported yet.
    val supervisorLat = assignedSupervisor?.latitude ?: (incidentLat + 0.004)
    val supervisorLng = assignedSupervisor?.longitude ?: (incidentLng + 0.004)

    // Map supervisor details
    val supervisorId = assignedSupervisor?.id ?: currentReport.assigned_supervisor_id ?: currentReport.supervisor_id
    val supervisorName = assignedSupervisor?.name ?: if (supervisorId != null) "Supervisor" else "Unassigned"
    val supervisorPhone = assignedSupervisor?.phone ?: ""
    val supervisorEmployeeId = assignedSupervisor?.employee_id ?: if (supervisorId != null) "SUP-$supervisorId" else "Unassigned"

    LaunchedEffect(supervisorId, supervisorLat, supervisorLng, incidentLat, incidentLng) {
        if (supervisorId == null) {
            routePoints = emptyList()
            durationText = "N/A"
            distanceText = "N/A"
            return@LaunchedEffect
        }
        val result = ApiService.getDrivingRoute(
            startLat = supervisorLat,
            startLng = supervisorLng,
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
                LatLng(supervisorLat, supervisorLng),
                LatLng(supervisorLat, incidentLng),
                LatLng(incidentLat, incidentLng)
            )
            durationText = "15 mins"
            distanceText = "1.2 mi"
        }
    }

    // Update map elements when coordinates or route changes
    LaunchedEffect(mapInstance, routePoints, supervisorLat, supervisorLng, supervisorId) {
        val map = mapInstance ?: return@LaunchedEffect
        map.clear()

        // Draw route line dynamically
        if (supervisorId != null && routePoints.isNotEmpty()) {
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

        val incidentMarker = MarkerOptions().position(LatLng(incidentLat, incidentLng)).title("Incident Bin (${currentReport.category})")
        if (binIcon != null) {
            incidentMarker.icon(binIcon)
        }
        map.addMarker(incidentMarker)

        if (supervisorId != null) {
            val truckMarker = MarkerOptions().position(LatLng(supervisorLat, supervisorLng)).title("Supervisor $supervisorName (Truck)")
            if (truckIcon != null) {
                truckMarker.icon(truckIcon)
            }
            map.addMarker(truckMarker)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE2E8F0))
    ) {
        // Map Area
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            MapLibreView(
                modifier = Modifier.fillMaxSize()
            ) { map ->
                mapInstance = map
                map.setStyle("https://tiles.openfreemap.org/styles/liberty")
                
                map.cameraPosition = CameraPosition.Builder()
                    .target(LatLng((incidentLat + supervisorLat) / 2.0, (incidentLng + supervisorLng) / 2.0))
                    .zoom(13.8)
                    .build()
            }
            
            // Top Bar Overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color(0xFF1E293B))
                }
                Text(
                    text = "Live Tracking",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp)) // balance
            }

            // Floating action buttons on map (Recenter & Navigation launcher)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Recenter Camera FAB
                FloatingActionButton(
                    onClick = {
                        mapInstance?.let { map ->
                            val bounds = LatLngBounds.Builder()
                                .include(LatLng(incidentLat, incidentLng))
                                .include(LatLng(supervisorLat, supervisorLng))
                                .build()
                            map.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100), 1000)
                        }
                    },
                    containerColor = Color.White,
                    contentColor = Color(0xFF1E293B),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Recenter Camera",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Bottom Sheet Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2E8F0))
                        .align(Alignment.CenterHorizontally)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Supervisor: $supervisorName",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Assigned Crew • $supervisorEmployeeId",
                            color = Color(0xFF64748B),
                            fontSize = 14.sp
                        )
                    }
                    AppBadge(text = "En Route", variant = "info")
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AppCard(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Est. Arrival", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(durationText, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                        }
                    }
                    AppCard(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Distance", fontSize = 12.sp, color = Color(0xFF94A3B8))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(distanceText, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppButton(
                        text = "Details",
                        onClick = { showDetailsDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Route Navigation Intent Button
                    AppButton(
                        text = "Navigate",
                        onClick = {
                            try {
                                val url = "https://www.google.com/maps/dir/?api=1&origin=$supervisorLat,$supervisorLng&destination=$incidentLat,$incidentLng"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback to browser or generic map viewer
                                val url = "https://www.google.com/maps/dir/?api=1&origin=$supervisorLat,$supervisorLng&destination=$incidentLat,$incidentLng"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedButton(
                        onClick = {
                            if (supervisorPhone.isNotBlank()) {
                                try {
                                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:$supervisorPhone")
                                    }
                                    context.startActivity(dialIntent)
                                } catch (e: Exception) {
                                    // Dialer fallback
                                }
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF16A34A)),
                        enabled = supervisorPhone.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call Supervisor",
                            tint = if (supervisorPhone.isNotBlank()) Color(0xFF16A34A) else Color(0xFF94A3B8)
                        )
                    }
                } }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (showDetailsDialog) {
            AlertDialog(
                onDismissRequest = { showDetailsDialog = false },
                title = {
                    Text(
                        text = "Dispatch Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1E293B)
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Supervisor Information",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF16A34A)
                        )
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Name:", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text(supervisorName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF334155))
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Employee ID:", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text(supervisorEmployeeId, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF334155))
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Contact Phone:", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text(supervisorPhone, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF334155))
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFE2E8F0)))

                        Text(
                            text = "Location Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF16A34A)
                        )
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Incident Location:", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text(
                                currentReport.address ?: currentReport.location ?: "Unknown Location",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color(0xFF334155),
                                maxLines = 2,
                                modifier = Modifier.weight(1f).padding(start = 16.dp),
                                textAlign = TextAlign.End
                            )
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Supervisor Coordinates:", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text(
                                String.format("%.5f, %.5f", supervisorLat, supervisorLng),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color(0xFF334155)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Remaining Distance:", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text(distanceText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF334155))
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showDetailsDialog = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF16A34A))
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = Color.White
            )
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
