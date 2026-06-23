package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wastereporting.components.AppCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSupervisorsScreen(
    onNavigateToDashboard: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onBack: () -> Unit,
    onNavigateToAddSupervisor: () -> Unit,
    onNavigateToSupervisorDetails: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var supervisors by remember { mutableStateOf<List<com.wastereporting.network.AdminSupervisorStats>>(emptyList()) }

    LaunchedEffect(Unit) {
        val res = com.wastereporting.network.ApiService.getAdminSupervisors()
        if (res.isSuccess) {
            supervisors = res.getOrNull() ?: emptyList()
        }
    }

    val filtered = remember(searchQuery, supervisors) {
        if (searchQuery.isBlank()) supervisors
        else supervisors.filter {
            it.full_name.contains(searchQuery, ignoreCase = true) ||
            it.employee_id.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        bottomBar = {
            AdminBottomNav(
                currentTab = "Supervisors",
                onTabSelected = { tab ->
                    when (tab) {
                        "Dashboard" -> onNavigateToDashboard()
                        "Reports" -> onNavigateToReports()
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.padding(end = 8.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
                        }
                        Text("Supervisors", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), modifier = Modifier.weight(1f))
                        Button(
                            onClick = onNavigateToAddSupervisor,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add New", color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Summary Stats
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AdminSupStatCard("Total", supervisors.size.toString(), Icons.Default.People, Color(0xFF3B82F6), Modifier.weight(1f))
                        AdminSupStatCard(
                            "Pending",
                            supervisors.sumOf { it.assigned_reports }.toString(),
                            Icons.Default.HourglassEmpty, Color(0xFFF59E0B), Modifier.weight(1f)
                        )
                        AdminSupStatCard(
                            "Completed",
                            supervisors.sumOf { it.resolved_reports }.toString(),
                            Icons.Default.CheckCircle, Color(0xFF10B981), Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search by name or supervisor ID...", color = Color(0xFF94A3B8)) },
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

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No supervisors found.", color = Color(0xFF64748B), fontSize = 16.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filtered) { sup ->
                        AdminSupervisorCard(
                            sup = sup,
                            onClick = { onNavigateToSupervisorDetails(sup.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSupStatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF1F5F9))
            .padding(12.dp)
    ) {
        Column {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Text(title, fontSize = 12.sp, color = Color(0xFF64748B))
        }
    }
}

@Composable
fun AdminSupervisorCard(
    sup: com.wastereporting.network.AdminSupervisorStats,
    onClick: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color(0xFFDBEAFE)), contentAlignment = Alignment.Center) {
                        Text(sup.full_name.take(1).uppercase(), color = Color(0xFF1D4ED8), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(sup.full_name, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontSize = 16.sp)
                        Text(sup.employee_id, fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                            Text(sup.assigned_area ?: "Unassigned", fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
                
                val isActive = sup.is_active != false
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isActive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isActive) "Active" else "Deactivated",
                        color = if (isActive) Color(0xFF16A34A) else Color(0xFFEF4444),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))

            // Report Statistics
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Total", fontSize = 12.sp, color = Color(0xFF64748B))
                    Text("${sup.assigned_reports + sup.resolved_reports}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.padding(top = 4.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Pending", fontSize = 12.sp, color = Color(0xFF64748B))
                    Text("${sup.assigned_reports}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B), modifier = Modifier.padding(top = 4.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Completed", fontSize = 12.sp, color = Color(0xFF64748B))
                    Text("${sup.resolved_reports}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981), modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}
