package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
fun CollectionSummaryScreen(
    onBack: () -> Unit,
    onReturnToDashboard: () -> Unit
) {
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
                "Collection Summary",
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Success Icon
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .border(4.dp, Color(0xFFDCFCE7), CircleShape)
                    .background(Color(0xFFF0FDF4)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = "Success", tint = Color(0xFF16A34A), modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Collection Complete",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "The recycling collection for your area was successfully completed today at 11:45 AM.",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                    Text("Collection Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B), modifier = Modifier.padding(bottom = 16.dp))
                    
                    DetailRow("Vehicle", "#402 (Recycling)")
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailRow("Time Completed", "11:45 AM")
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailRow("Bins Emptied", "42 in your zone")
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
            
            Text("Rate this service", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(Icons.Default.Star, contentDescription = "1 star", tint = Color(0xFFF59E0B), modifier = Modifier.size(32.dp))
                Icon(Icons.Default.Star, contentDescription = "2 stars", tint = Color(0xFFF59E0B), modifier = Modifier.size(32.dp))
                Icon(Icons.Default.Star, contentDescription = "3 stars", tint = Color(0xFFF59E0B), modifier = Modifier.size(32.dp))
                Icon(Icons.Default.Star, contentDescription = "4 stars", tint = Color(0xFFF59E0B), modifier = Modifier.size(32.dp))
                Icon(Icons.Default.StarBorder, contentDescription = "5 stars", tint = Color(0xFFCBD5E1), modifier = Modifier.size(32.dp))
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(48.dp))

            AppButton(
                text = "Return to Dashboard",
                onClick = onReturnToDashboard,
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color(0xFF64748B), fontSize = 14.sp)
        Text(value, color = Color(0xFF1E293B), fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}
