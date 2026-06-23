package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.LocationOn
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
import com.wastereporting.components.AppBadge
import com.wastereporting.components.AppCard

@Composable
fun NearbyIssuesScreen(onBack: () -> Unit, onNavigateToReview: () -> Unit = {}) {
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
                "Nearby Issues",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF1E293B),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            IconButton(onClick = { }) {
                Icon(Icons.Default.FilterAlt, contentDescription = "Filter", tint = Color(0xFF1E293B))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Map Header Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFE2E8F0)), // Map background
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Map", tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                    Text("Tap to view full map", color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                }
                
                // Dots representing issues
                Box(modifier = Modifier.offset(x = (-60).dp, y = (-40).dp).size(12.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                Box(modifier = Modifier.offset(x = (40).dp, y = (20).dp).size(12.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Issues near you",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // List of issues
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                IssueListItem(distance = "0.1 mi", time = "2 hrs ago", onClick = onNavigateToReview)
                IssueListItem(distance = "0.2 mi", time = "2 hrs ago", onClick = onNavigateToReview)
                IssueListItem(distance = "0.3 mi", time = "2 hrs ago", onClick = onNavigateToReview)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun IssueListItem(distance: String, time: String, onClick: () -> Unit = {}) {
    AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { onClick() }) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF8FAFC)), // Placeholder for image
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Warning, contentDescription = "Warning", tint = Color(0xFFCBD5E1), modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Overflowing Bin", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                    Text(distance, fontSize = 12.sp, color = Color(0xFF94A3B8))
                }
                Text("Corner of 5th and Main St.", fontSize = 14.sp, color = Color(0xFF64748B), modifier = Modifier.padding(vertical = 4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppBadge("Reported", variant = "warning")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(time, fontSize = 12.sp, color = Color(0xFF94A3B8))
                }
            }
        }
    }
}
