package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun DailyStatisticsScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

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
                "Daily Statistics",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF1E293B),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp)) // balance for back button
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            // Date Selector
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day", tint = Color(0xFF94A3B8))
                Text("Today, Oct 24", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Day", tint = Color(0xFF94A3B8))
            }

            // Top Metrics
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AppCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Collected", fontSize = 12.sp, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("4.2t", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    }
                }
                AppCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Bins Emptied", fontSize = 12.sp, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("1,240", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    }
                }
            }

            // Chart Section
            AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Collection by Time", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Simulated Area Chart
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                        // Normally this would be a Canvas drawing a path
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .align(Alignment.BottomCenter)
                                .clip(RoundedCornerShape(topStartPercent = 10, topEndPercent = 100))
                                .background(Color(0xFFCBD5E1).copy(alpha = 0.5f)) // Grey fill
                        )
                        // Simulated line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .align(Alignment.TopCenter) // Approximation
                                .offset(y = 30.dp)
                                .background(Color(0xFF16A34A))
                        )
                        
                        // X-axis labels
                        Row(
                            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("6AM", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text("9AM", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text("12PM", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text("3PM", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            Text("6PM", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }
            }

            // Active Vehicles Section
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Active Vehicles", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                Text("12 Active", color = Color(0xFF3B82F6), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                VehicleCard(modifier = Modifier.weight(1f), zone = "Zone A", progress = "85% Complete")
                VehicleCard(modifier = Modifier.weight(1f), zone = "Zone B", progress = "85% Complete")
            }
        }
    }
}

@Composable
fun VehicleCard(modifier: Modifier = Modifier, zone: String, progress: String) {
    AppCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.LocalShipping, contentDescription = "Truck", tint = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(8.dp))
            Text(zone, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Text(progress, fontSize = 10.sp, color = Color(0xFF64748B))
        }
    }
}
