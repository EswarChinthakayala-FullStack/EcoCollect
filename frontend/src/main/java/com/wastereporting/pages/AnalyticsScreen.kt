package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wastereporting.components.AppBadge
import com.wastereporting.components.AppCard

@Composable
fun AnalyticsScreen(
    onNavigateToDaily: () -> Unit = {},
    onNavigateToMonthly: () -> Unit = {},
    onNavigateToRecycling: () -> Unit = {},
    onNavigateToInsights: () -> Unit = {},
    onNavigateToImpact: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // bg-slate-50
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Header
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
            Text("Analytics", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF1E293B))
        }

        AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp).clickable { onNavigateToImpact() }) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Waste\nCollected", fontSize = 16.sp, color = Color(0xFF64748B))
                    AppBadge("-5% vs last month", variant = "success")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("1,245", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Text(" tons", fontSize = 16.sp, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Chart Placeholder (Bar chart)
                Row(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    ChartBar(height = 60f, label = "Mon")
                    ChartBar(height = 80f, label = "Tue")
                    ChartBar(height = 100f, label = "Wed", isHighlighted = true)
                    ChartBar(height = 50f, label = "Thu")
                    ChartBar(height = 70f, label = "Fri")
                    ChartBar(height = 40f, label = "Sat")
                    ChartBar(height = 30f, label = "Sun")
                }
            }
        }

        // Grid Cards
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            GridCard(
                modifier = Modifier.weight(1f).clickable { onNavigateToDaily() },
                title = "Daily Stats",
                subtitle = "View today's\ncollection data",
                iconColor = Color(0xFF3B82F6),
                bgColor = Color(0xFFDBEAFE),
                icon = Icons.Default.BarChart
            )
            GridCard(
                modifier = Modifier.weight(1f).clickable { onNavigateToMonthly() },
                title = "Monthly",
                subtitle = "Long-term trends",
                iconColor = Color(0xFF8B5CF6),
                bgColor = Color(0xFFEDE9FE),
                icon = Icons.Default.CalendarToday
            )
        }
        
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            GridCard(
                modifier = Modifier.weight(1f).clickable { onNavigateToRecycling() },
                title = "Recycling",
                subtitle = "Material breakdown",
                iconColor = Color(0xFF10B981),
                bgColor = Color(0xFFD1FAE5),
                icon = Icons.Default.Recycling
            )
            GridCard(
                modifier = Modifier.weight(1f).clickable { onNavigateToInsights() },
                title = "Insights",
                subtitle = "View AI Tips →",
                iconColor = Color(0xFFF59E0B),
                bgColor = Color(0xFFFEF3C7),
                icon = Icons.Default.Insights
            )
        }
    }
}

@Composable
fun ChartBar(height: Float, label: String, isHighlighted: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(height.dp)
                .clip(CircleShape)
                .background(if (isHighlighted) Color(0xFF16A34A) else Color(0xFFE2E8F0))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, fontSize = 12.sp, color = Color(0xFF94A3B8))
    }
}

@Composable
fun GridCard(modifier: Modifier = Modifier, title: String, subtitle: String, iconColor: Color, bgColor: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    AppCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
            Text(subtitle, fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 4.dp))
        }
    }
}
