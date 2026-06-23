package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wastereporting.components.AppCard

@Composable
fun MonthlyReportsScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()
    
    var analytics by remember { mutableStateOf<com.wastereporting.network.AdminAnalyticsResponse?>(null) }
    
    LaunchedEffect(Unit) {
        val res = com.wastereporting.network.ApiService.getAdminAnalytics()
        if (res.isSuccess) {
            analytics = res.getOrNull()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // bg-slate-50
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color(0xFF1E293B))
            }
            Text(
                "Monthly Reports",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF1E293B),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp)) // balance
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            // Month Selector & Download
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Current Month", color = Color(0xFF1E293B), fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Select", tint = Color(0xFF64748B))
                }
                
                // Download Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF16A34A)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White)
                }
            }

            // Waste Volume Trend Chart
            AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Waste Volume Trend", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Live DB Chart
                    Row(
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        if (analytics != null) {
                            analytics!!.weeks.forEach { week ->
                                GroupedBar(w1 = week.general, w2 = week.recycling, label = week.label)
                            }
                        } else {
                            Text("Loading Data...", color = Color.Gray, modifier = Modifier.align(Alignment.CenterVertically))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    // Legend
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFCBD5E1)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("General", fontSize = 10.sp, color = Color(0xFF64748B))
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF16A34A)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Recycling", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                }
            }

            // Key Metrics
            Text("Key Metrics", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B), modifier = Modifier.padding(bottom = 12.dp))
            
            AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Volume", fontSize = 12.sp, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${analytics?.total_volume ?: "0.0"} Tons", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(analytics?.volume_trend ?: "+0.0%", fontWeight = FontWeight.Bold, color = Color(0xFF16A34A), fontSize = 14.sp)
                        Text("vs last month", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                }
            }
            
            AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Recycling Rate", fontSize = 12.sp, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${analytics?.recycling_rate ?: "0.0"}%", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(analytics?.recycling_trend ?: "+0.0%", fontWeight = FontWeight.Bold, color = Color(0xFF16A34A), fontSize = 14.sp)
                        Text("vs last month", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                }
            }
        }
    }
}

@Composable
fun GroupedBar(w1: Float, w2: Float, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
            Box(modifier = Modifier.width(12.dp).height(w1.dp).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)).background(Color(0xFFCBD5E1)))
            Box(modifier = Modifier.width(12.dp).height(w2.dp).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)).background(Color(0xFF16A34A)))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, fontSize = 10.sp, color = Color(0xFF94A3B8))
    }
}
