package com.wastereporting.pages

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wastereporting.components.AppCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAnalyticsScreen(
    onNavigateToDashboard: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    var totalWaste by remember { mutableStateOf(0) }
    var avgRes by remember { mutableStateOf("0.0") }
    var recycling by remember { mutableStateOf("0") }
    var engagement by remember { mutableStateOf("0") }
    
    var issuesPerMonth by remember { mutableStateOf<List<Int>>(emptyList()) }
    var categories by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    LaunchedEffect(Unit) {
        val res = com.wastereporting.network.ApiService.getAdminAnalytics()
        if (res.isSuccess) {
            val data = res.getOrNull()
            totalWaste = data?.total_waste_collected ?: 0
            avgRes = data?.avg_resolution_time_hrs ?: "0.0"
            recycling = data?.recycling_rate_percent ?: "0"
            engagement = data?.citizen_engagement_score ?: "0"
            
            issuesPerMonth = data?.issues_per_month ?: emptyList()
            categories = data?.category_breakdown ?: emptyMap()
        }
    }

    Scaffold(
        bottomBar = {
            AdminBottomNav(
                currentTab = "Analytics",
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
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("City Analytics", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                            Text("This Month", fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(start = 4.dp))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            
            HorizontalDivider(color = Color(0xFFE2E8F0))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                // KPIs
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AdminAnalyticsKpiCard("Waste Collected", "$totalWaste", "tons", Icons.Default.Delete, Color(0xFF3B82F6), Modifier.weight(1f))
                    AdminAnalyticsKpiCard("Avg Resolution", avgRes, "hrs", Icons.Default.Timer, Color(0xFF10B981), Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AdminAnalyticsKpiCard("Recycling Rate", recycling, "%", Icons.Default.Recycling, Color(0xFF8B5CF6), Modifier.weight(1f))
                    AdminAnalyticsKpiCard("Citizen Eng.", engagement, "pts", Icons.Default.Favorite, Color(0xFFF43F5E), Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Line Chart Card
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Monthly Reports Trend", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color(0xFF94A3B8))
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Fake Line Chart
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val width = size.width
                                val height = size.height
                                
                                // Draw Grid Lines
                                val gridLines = 4
                                for (i in 0..gridLines) {
                                    val y = height - (height * i / gridLines)
                                    drawLine(
                                        color = Color(0xFFF1F5F9),
                                        start = Offset(0f, y),
                                        end = Offset(width, y),
                                        strokeWidth = 2f
                                    )
                                }
                                
                                // Draw Data Line
                                val dataPoints = listOf(0.2f, 0.5f, 0.4f, 0.7f, 0.6f, 0.9f)
                                val path = Path()
                                val stepX = width / (dataPoints.size - 1)
                                
                                dataPoints.forEachIndexed { index, value ->
                                    val x = index * stepX
                                    val y = height - (value * height)
                                    if (index == 0) {
                                        path.moveTo(x, y)
                                    } else {
                                        path.lineTo(x, y)
                                    }
                                }
                                
                                drawPath(
                                    path = path,
                                    color = Color(0xFF3B82F6),
                                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                                )
                                
                                // Draw points
                                dataPoints.forEachIndexed { index, value ->
                                    val x = index * stepX
                                    val y = height - (value * height)
                                    drawCircle(
                                        color = Color.White,
                                        radius = 12f,
                                        center = Offset(x, y)
                                    )
                                    drawCircle(
                                        color = Color(0xFF3B82F6),
                                        radius = 8f,
                                        center = Offset(x, y)
                                    )
                                }
                            }
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Jan", fontSize = 12.sp, color = Color(0xFF64748B))
                            Text("Feb", fontSize = 12.sp, color = Color(0xFF64748B))
                            Text("Mar", fontSize = 12.sp, color = Color(0xFF64748B))
                            Text("Apr", fontSize = 12.sp, color = Color(0xFF64748B))
                            Text("May", fontSize = 12.sp, color = Color(0xFF64748B))
                            Text("Jun", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Breakdown Bar Chart
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Waste Category Distribution", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val totalCategories = categories.values.sum().coerceAtLeast(1)
                        val colors = listOf(Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFF94A3B8), Color(0xFF8B5CF6))
                        
                        if (categories.isEmpty()) {
                            Text("No category data available.", color = Color(0xFF64748B))
                        } else {
                            categories.entries.forEachIndexed { index, entry ->
                                val pct = (entry.value.toFloat() / totalCategories) * 100
                                val color = colors[index % colors.size]
                                CategoryBarItem(entry.key, pct, color, "${pct.toInt()}%")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download Full Report (.PDF)", color = Color.White, fontWeight = FontWeight.Medium)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun AdminAnalyticsKpiCard(title: String, value: String, unit: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier) {
    AppCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Text(" $unit", fontSize = 14.sp, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 4.dp))
            }
            Text(title, fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
fun CategoryBarItem(title: String, percentage: Float, color: Color, label: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, fontSize = 14.sp, color = Color(0xFF1E293B))
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFF1F5F9))) {
            Box(modifier = Modifier.fillMaxWidth(percentage / 100f).height(8.dp).clip(RoundedCornerShape(4.dp)).background(color))
        }
    }
}
