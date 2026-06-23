package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ListAlt
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
import com.wastereporting.components.AppBadge
import com.wastereporting.components.AppCard
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.wastereporting.R

@Composable
fun AdminDashboardScreen(
    onNavigateToReports: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSupervisors: () -> Unit,
    onNavigateToReportDetails: (Int) -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val scrollState = rememberScrollState()
    var adminName by remember { mutableStateOf("Administrator") }
    var totalReports by remember { mutableStateOf(0) }
    var pendingReports by remember { mutableStateOf(0) }
    var completedReports by remember { mutableStateOf(0) }
    var totalSupervisors by remember { mutableStateOf(0) }
    var recentReports by remember { mutableStateOf<List<com.wastereporting.network.IssueReport>>(emptyList()) }

    LaunchedEffect(Unit) {
        val profileRes = com.wastereporting.network.ApiService.getProfile()
        if (profileRes.isSuccess) {
            adminName = profileRes.getOrNull()?.full_name ?: "Administrator"
        }
        val dashboardRes = com.wastereporting.network.ApiService.getAdminDashboard()
        if (dashboardRes.isSuccess) {
            val stats = dashboardRes.getOrNull()
            if (stats != null) {
                totalReports = stats.total_reports
                pendingReports = stats.pending_reports
                completedReports = stats.resolved_reports
                totalSupervisors = stats.total_supervisors
            }
        }
        val reportsRes = com.wastereporting.network.ApiService.getAdminReports()
        if (reportsRes.isSuccess) {
            recentReports = reportsRes.getOrNull()?.take(5) ?: emptyList()
        }
    }

    Scaffold(
        bottomBar = {
            AdminBottomNav(
                currentTab = "Dashboard",
                onTabSelected = { tab ->
                    when (tab) {
                        "Reports" -> onNavigateToReports()
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
                .background(Color(0xFFF1F5F9))
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            // Header (Dark Blue with notification bell)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Color(0xFF0F172A))
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
                            Text("Welcome Back,", color = Color(0xFF94A3B8), fontSize = 14.sp)
                            Text(adminName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("System Overview", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Stats KPI Section (3 cards overlapping the header by 32dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-32).dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminStatCard(
                    count = totalReports.toString(),
                    label = "Total Reports",
                    icon = Icons.Default.Assessment,
                    iconColor = Color(0xFF0F172A),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToReports
                )
                AdminStatCard(
                    count = pendingReports.toString(),
                    label = "Pending",
                    icon = Icons.Default.HourglassEmpty,
                    iconColor = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToReports
                )
                AdminStatCard(
                    count = completedReports.toString(),
                    label = "Completed",
                    icon = Icons.Default.CheckCircle,
                    iconColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToReports
                )
            }

            // Recent Reports Section
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .offset(y = (-16).dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Reports", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Text("View All", color = Color(0xFF3B82F6), fontSize = 14.sp, modifier = Modifier.clickable { onNavigateToReports() })
                }

                if (recentReports.isEmpty()) {
                    Text("No recent reports found.", color = Color(0xFF64748B), modifier = Modifier.padding(vertical = 16.dp))
                } else {
                    recentReports.forEach { report ->
                        AdminRecentReportCard(
                            id = "RPT-${report.id}",
                            type = report.category,
                            status = report.status,
                            date = com.wastereporting.network.ApiService.formatIsoDateTimeToIndian(report.created_at),
                            onClick = { onNavigateToReportDetails(report.id) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun AdminStatCard(
    count: String,
    label: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    AppCard(
        modifier = modifier,
        onClick = onClick
    ) {
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
fun QuickActionCard(title: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    AppCard(modifier = modifier.clickable { onClick() }) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
        }
    }
}

@Composable
fun AdminRecentReportCard(id: String, type: String, status: String, date: String, onClick: () -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = when (status) {
                            "Completed" -> Color(0xFFDCFCE7)
                            "Pending" -> Color(0xFFFEF3C7)
                            else -> Color(0xFFDBEAFE)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (status) {
                        "Completed" -> Icons.Default.CheckCircle
                        "Pending" -> Icons.Default.HourglassEmpty
                        else -> Icons.Default.Schedule
                    },
                    contentDescription = null,
                    tint = when (status) {
                        "Completed" -> Color(0xFF16A34A)
                        "Pending" -> Color(0xFFF59E0B)
                        else -> Color(0xFF3B82F6)
                    }
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(type, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("$id • $date", fontSize = 12.sp, color = Color(0xFF64748B))
            }
            AppBadge(
                text = status,
                variant = when (status) {
                    "Completed" -> "success"
                    "Pending" -> "warning"
                    else -> "info"
                }
            )
        }
    }
}


@Composable
fun AdminBottomNav(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
            label = { Text("Dashboard") },
            selected = currentTab == "Dashboard",
            onClick = { onTabSelected("Dashboard") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF0F172A),
                selectedTextColor = Color(0xFF0F172A),
                indicatorColor = Color(0xFFE2E8F0)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = "Reports") },
            label = { Text("Reports") },
            selected = currentTab == "Reports",
            onClick = { onTabSelected("Reports") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF0F172A),
                selectedTextColor = Color(0xFF0F172A),
                indicatorColor = Color(0xFFE2E8F0)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.People, contentDescription = "Supervisors") },
            label = { Text("Supervisors") },
            selected = currentTab == "Supervisors",
            onClick = { onTabSelected("Supervisors") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF0F172A),
                selectedTextColor = Color(0xFF0F172A),
                indicatorColor = Color(0xFFE2E8F0)
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = currentTab == "Profile",
            onClick = { onTabSelected("Profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF0F172A),
                selectedTextColor = Color(0xFF0F172A),
                indicatorColor = Color(0xFFE2E8F0)
            )
        )
    }
}
