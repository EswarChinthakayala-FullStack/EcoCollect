package com.wastereporting.pages

import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wastereporting.components.AppBadge
import com.wastereporting.components.AppButton
import com.wastereporting.components.AppCard
import com.wastereporting.components.NetworkImage
import com.wastereporting.network.ApiService
import com.wastereporting.network.IssueHistory
import com.wastereporting.network.IssueReport
import com.wastereporting.network.SupervisorDetailsResponse

@Composable
fun CitizenReportDetailsScreen(
    reportId: Int,
    onBack: () -> Unit,
    onTrackLive: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    var report by remember { mutableStateOf<IssueReport?>(null) }
    var history by remember { mutableStateOf<List<IssueHistory>>(emptyList()) }
    var assignedSupervisor by remember { mutableStateOf<SupervisorDetailsResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedImageIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(reportId) {
        val res = ApiService.getReportById(reportId)
        if (res.isSuccess) {
            val response = res.getOrNull()
            report = response?.issue ?: response?.report
            history = response?.history?.sortedByDescending { it.id } ?: emptyList()
            assignedSupervisor = response?.assigned_supervisor
        }
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF16A34A))
        }
        return
    }

    val currentReport = report
    if (currentReport == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Report not found", color = Color(0xFFEF4444), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
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

            // Image Gallery Card
            AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "CLEANUP IMAGES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val isCompleted = currentReport.status.equals("Completed", ignoreCase = true) || currentReport.status.equals("Resolved", ignoreCase = true)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Before image(s) column
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "My Submission (Before)",
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

                        // After image column (Only if completed)
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
                        "MY DESCRIPTION REMARKS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentReport.description ?: "No detailed description remarks filed.",
                        fontSize = 14.sp,
                        color = Color(0xFF334155),
                        lineHeight = 20.sp
                    )
                }
            }

            // Properties / Details Card
            AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Filing Category
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Category, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Filing Category", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                            Text(currentReport.category, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        }
                    }

                    // Filing Date
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Filing Date", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                            Text(ApiService.formatIsoDateTimeToIndian(currentReport.created_at), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        }
                    }

                    // Location / Area Address
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Location Address", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                            Text(currentReport.address ?: currentReport.location ?: "Address unspecified", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        }
                    }

                    // Assigned Supervisor (User)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Assigned User (Supervisor)", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                            val supervisor = assignedSupervisor
                            val assignedText = if (supervisor != null) {
                                supervisor.employee_id
                            } else if (currentReport.assigned_supervisor_id != null) {
                                "SUP-${currentReport.assigned_supervisor_id}"
                            } else {
                                "Unassigned"
                            }
                            Text(assignedText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        }
                    }
                }
            }

            // Resolution Timeline Card
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
                        TimelineItem(
                            title = "Report Submitted",
                            time = ApiService.formatIsoDateTimeToIndian(currentReport.created_at),
                            remarks = "Your report was submitted successfully.",
                            isActive = true,
                            isLast = true
                        )
                    } else {
                        history.forEachIndexed { index, item ->
                            TimelineItem(
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

            // Track Vehicle Live Button - ONLY show for In Progress / En Route state
            val statusClean = currentReport.status.lowercase()
            if (statusClean == "in_progress" || statusClean == "in progress" || statusClean == "assigned") {
                Spacer(modifier = Modifier.height(8.dp))
                AppButton(
                    text = "Track Vehicle Live",
                    onClick = { onTrackLive(currentReport.id) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                )
            }
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
}

@Composable
private fun TimelineItem(
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
                    .background(if (isActive) Color(0xFF16A34A) else Color(0xFFE2E8F0))
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(56.dp)
                        .background(if (isActive) Color(0xFF16A34A).copy(alpha = 0.4f) else Color(0xFFE2E8F0))
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
