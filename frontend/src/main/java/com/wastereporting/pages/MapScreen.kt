package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.wastereporting.components.AppCard
import com.wastereporting.components.MapLibreView

import org.maplibre.android.geometry.LatLng
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE2E8F0))) {
        MapLibreView(
            modifier = Modifier.fillMaxSize()
        ) { map ->
            map.setStyle("https://tiles.openfreemap.org/styles/liberty")
            map.cameraPosition = CameraPosition.Builder()
                .target(LatLng(12.9716, 77.5946))
                .zoom(14.5)
                .build()
            
            // Add native markers for bins, issues, and user location
            map.addMarker(MarkerOptions().position(LatLng(12.9720, 77.5930)).title("Your Location"))
            map.addMarker(MarkerOptions().position(LatLng(12.9716, 77.5946)).title("Smart Bin"))
            map.addMarker(MarkerOptions().position(LatLng(12.9740, 77.5960)).title("Reported Issue - Garbage Pile"))
            map.addMarker(MarkerOptions().position(LatLng(12.9700, 77.5910)).title("Reported Issue - Overflowing Bin"))
        }

        // Top UI Elements
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Search location...") },
                    modifier = Modifier.weight(1f).background(Color.White, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color(0xFF64748B))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CustomFilterChip(selected = true, text = "All")
                CustomFilterChip(selected = false, text = "Bins")
                CustomFilterChip(selected = false, text = "Issues")
                CustomFilterChip(selected = false, text = "Trucks")
            }
        }

        // Bottom Card Info
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)) {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Downtown District", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                        Text("Cleanliness Score: 85/100", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDCFCE7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Layers", tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
        
        // Layers button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 100.dp) // above the bottom card
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Layers, contentDescription = "Layers", tint = Color(0xFF64748B))
        }
    }
}

@Composable
fun CustomFilterChip(selected: Boolean, text: String) {
    val bgColor = if (selected) Color(0xFF16A34A) else Color.White
    val textColor = if (selected) Color.White else Color(0xFF64748B)
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
