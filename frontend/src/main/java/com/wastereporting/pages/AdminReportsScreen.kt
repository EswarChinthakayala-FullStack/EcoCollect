package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.wastereporting.components.AppBadge
import com.wastereporting.components.AppCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReportsScreen(
    onNavigateToDashboard: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSupervisors: () -> Unit,
    onNavigateToReportDetails: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Pending", "Completed")

    var reports by remember { mutableStateOf<List<com.wastereporting.network.IssueReport>>(emptyList()) }
    var totalReports by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        val reportsRes = com.wastereporting.network.ApiService.getAdminReports()
        if (reportsRes.isSuccess) {
            val fetchedReports = reportsRes.getOrNull() ?: emptyList()
            reports = fetchedReports
            totalReports = fetchedReports.size
        }
    }

    val filteredReports = remember(selectedFilter, searchQuery, reports) {
        val byStatus = when (selectedFilter) {
            "Pending" -> reports.filter { it.status == "Pending" }
            "Completed" -> reports.filter { it.status == "Completed" }
            else -> reports
        }
        if (searchQuery.isBlank()) byStatus
        else byStatus.filter {
            it.category.contains(searchQuery, ignoreCase = true) ||
            it.id.toString().contains(searchQuery) ||
            (it.location ?: "").contains(searchQuery, ignoreCase = true) ||
            (it.address ?: "").contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        bottomBar = {
            AdminBottomNav(
                currentTab = "Reports",
                onTabSelected = { tab ->
                    when (tab) {
                        "Dashboard" -> onNavigateToDashboard()
                        "Supervisors" -> onNavigateToSupervisors()
                        "Profile" -> onNavigateToProfile()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(innerPadding)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Reports Management", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEFF6FF))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Total: $totalReports", color = Color(0xFF1D4ED8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search reports by ID, location or category...", color = Color(0xFF94A3B8)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFE2E8F0))

            // Filters
            LazyRow(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) Color.White else Color(0xFF64748B),
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    }
                }
            }

            // Report List
            if (filteredReports.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No reports found.", color = Color(0xFF64748B), fontSize = 16.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredReports) { report ->
                        AdminReportManagementCard(
                            report = report,
                            onViewDetails = { onNavigateToReportDetails(report.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminReportManagementCard(report: com.wastereporting.network.IssueReport, onViewDetails: () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (report.status == "Completed") Color(0xFFDCFCE7) else Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (report.status == "Completed") Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = if (report.status == "Completed") Color(0xFF16A34A) else Color(0xFFF59E0B)
                        )
                    }
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(report.category, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontSize = 16.sp)
                        Text("RPT-${report.id} • ${report.created_at?.take(10)}", fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                            Text(report.address ?: report.location ?: "Unknown Location", fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(start = 4.dp), maxLines = 1)
                        }
                    }
                }

                AppBadge(
                    text = report.status,
                    variant = when (report.status) {
                        "Completed" -> "success"
                        "Pending" -> "warning"
                        else -> "info"
                    }
                )
            }

            HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val citizenName = report.reporter_name ?: "Citizen"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(0xFFDBEAFE)), contentAlignment = Alignment.Center) {
                        Text(citizenName.take(1).uppercase(), color = Color(0xFF1D4ED8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("Reported by $citizenName", fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(start = 8.dp))
                }

                Button(
                    onClick = onViewDetails,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("View Details", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
