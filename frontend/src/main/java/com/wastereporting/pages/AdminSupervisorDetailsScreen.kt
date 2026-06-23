package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wastereporting.components.AppCard
import com.wastereporting.network.ApiService
import com.wastereporting.network.AdminSupervisorStats
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSupervisorDetailsScreen(
    supervisorId: Int,
    onBack: () -> Unit,
    onNavigateToEditSupervisor: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var supervisors by remember { mutableStateOf<List<AdminSupervisorStats>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var toggling by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val res = ApiService.getAdminSupervisors()
        if (res.isSuccess) {
            supervisors = res.getOrNull() ?: emptyList()
        }
        isLoading = false
    }

    val supervisor = supervisors.find { it.id == supervisorId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.padding(end = 8.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
                }
                Text("Supervisor Details", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            }
        }

        HorizontalDivider(color = Color(0xFFE2E8F0))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF3B82F6))
            }
        } else if (supervisor == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Supervisor not found", color = Color(0xFF64748B), fontSize = 16.sp)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                // Profile Avatar Card
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDBEAFE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = supervisor.full_name.take(1).uppercase(),
                                color = Color(0xFF1D4ED8),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = supervisor.full_name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        Text(
                            text = "Employee ID: ${supervisor.employee_id}",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        val isActive = supervisor.is_active != false
                        Box(
                            modifier = Modifier
                                .padding(top = 12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isActive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isActive) Color(0xFF16A34A) else Color(0xFFEF4444))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isActive) "Active" else "Deactivated",
                                    color = if (isActive) Color(0xFF16A34A) else Color(0xFFEF4444),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // KPI Job Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AppCard(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Assignment, contentDescription = null, tint = Color(0xFF3B82F6))
                            Text(
                                text = (supervisor.assigned_reports + supervisor.resolved_reports).toString(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text("Assigned Jobs", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }

                    AppCard(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                            Text(
                                text = supervisor.resolved_reports.toString(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text("Resolved Jobs", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Contact & Allocation Card
                Text("Contact & Allocation", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), modifier = Modifier.padding(bottom = 8.dp))
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailsInfoRow(icon = Icons.Default.Email, label = "Email Address", value = supervisor.email)
                        HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))
                        DetailsInfoRow(icon = Icons.Default.Map, label = "Assigned Area Locality", value = supervisor.assigned_area ?: "Not Assigned")
                        
                        if (supervisor.latitude != null && supervisor.longitude != null) {
                            HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))
                            DetailsInfoRow(
                                icon = Icons.Default.LocationOn,
                                label = "GPS Coordinates & Radius",
                                value = "Lat: ${"%.4f".format(supervisor.latitude)}, Lng: ${"%.4f".format(supervisor.longitude)} (${supervisor.coverage_radius ?: 10.0} KM)"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { onNavigateToEditSupervisor(supervisorId) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Profile", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    val isActive = supervisor.is_active != false
                    Button(
                        onClick = {
                            toggling = true
                            scope.launch {
                                val res = ApiService.toggleSupervisorStatus(supervisorId)
                                if (res.isSuccess) {
                                    val freshRes = ApiService.getAdminSupervisors()
                                    if (freshRes.isSuccess) {
                                        supervisors = freshRes.getOrNull() ?: emptyList()
                                    }
                                }
                                toggling = false
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                            contentColor = if (isActive) Color(0xFFEF4444) else Color(0xFF16A34A)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !toggling
                    ) {
                        if (toggling) {
                            CircularProgressIndicator(color = if (isActive) Color(0xFFEF4444) else Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                        } else {
                            Text(
                                text = if (isActive) "Deactivate" else "Activate",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun DetailsInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color(0xFF64748B))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B), modifier = Modifier.padding(top = 2.dp))
        }
    }
}
