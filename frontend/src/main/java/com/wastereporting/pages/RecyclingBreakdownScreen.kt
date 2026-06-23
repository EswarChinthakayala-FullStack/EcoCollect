package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
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
import com.wastereporting.components.AppButton
import com.wastereporting.components.AppCard

@Composable
fun RecyclingBreakdownScreen(onBack: () -> Unit, onNavigateToInsights: () -> Unit) {
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
                "Recycling Breakdown",
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
            AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Simulated Donut Chart
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .border(24.dp, Color(0xFF16A34A), CircleShape), // Green outer
                        contentAlignment = Alignment.Center
                    ) {
                        // Normally this would be a canvas with arcs
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(12.dp, Color(0xFF3B82F6).copy(alpha = 0.5f), CircleShape) // Blue inner
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("42%", fontWeight = FontWeight.Bold, fontSize = 36.sp, color = Color(0xFF1E293B))
                            Text("Recycled", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // Legend
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        LegendItem(color = Color(0xFFF59E0B), label = "Paper & Cardboard", percentage = "45%")
                        LegendItem(color = Color(0xFF3B82F6), label = "Plastics", percentage = "30%")
                        LegendItem(color = Color(0xFF10B981), label = "Glass", percentage = "15%")
                        LegendItem(color = Color(0xFF64748B), label = "Metals", percentage = "10%")
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    AppButton(
                        text = "View AI Recommendations",
                        onClick = onNavigateToInsights,
                        variant = "outline",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, percentage: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, color = Color(0xFF1E293B), fontSize = 14.sp)
        }
        Text(percentage, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontSize = 14.sp)
    }
}
