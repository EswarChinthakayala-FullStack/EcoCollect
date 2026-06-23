package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
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

@Composable
fun SmartInsightsScreen(onBack: () -> Unit, onReturnToDashboard: () -> Unit) {
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
                "Smart Insights",
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
            // Intro Yellow Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFEF9C3)) // light yellow
                    .border(1.dp, Color(0xFFFEF08A), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Lightbulb, contentDescription = "Tip", tint = Color(0xFFCA8A04))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "AI has analyzed your community's waste patterns and generated 3 key recommendations.",
                        color = Color(0xFFA16207),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }

            // Red Card
            InsightCard(
                title = "High Contamination Rate",
                description = "Zone B recycling bins show 25% non-recyclable materials.",
                recommendation = "Launch targeted educational campaign in Zone B about proper plastic sorting.",
                icon = Icons.Default.Warning,
                iconColor = Color(0xFFDC2626)
            )

            // Blue Card
            InsightCard(
                title = "Route Optimization Opportunity",
                description = "Friday collections in North District are consistently under 40% capacity.",
                recommendation = "Shift North District collections to bi-weekly to save fuel and reduce emissions.",
                icon = Icons.Default.Lightbulb,
                iconColor = Color(0xFF2563EB)
            )

            // Green Card (No recommendation)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Positive", tint = Color(0xFF16A34A))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Positive Trend Identified", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Organic waste collection increased by 15% this month.", fontSize = 14.sp, color = Color(0xFF64748B))
                    }
                }
            }

            // Return Button
            AppButton(
                text = "Return to Dashboard",
                onClick = onReturnToDashboard,
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
            )
        }
    }
}

@Composable
fun InsightCard(
    title: String,
    description: String,
    recommendation: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(icon, contentDescription = null, tint = iconColor)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, fontSize = 14.sp, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Recommendation:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1E293B))
                Text(recommendation, fontSize = 12.sp, color = Color(0xFF64748B))
            }
        }
    }
}
