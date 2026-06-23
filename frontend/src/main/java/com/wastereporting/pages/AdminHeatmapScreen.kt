package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wastereporting.components.MapLibreView

import org.maplibre.android.geometry.LatLng
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.maps.MapLibreMap

@Composable
fun AdminHeatmapScreen(
    onBack: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All Issues") }
    val filters = listOf("All Issues", "Critical", "Moderate", "Completed")
    
    var hotspots by remember { mutableStateOf<List<com.wastereporting.network.HeatmapPoint>>(emptyList()) }
    var recommendations by remember { mutableStateOf<List<String>>(emptyList()) }
    var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }

    val geoJsonData = remember(hotspots) {
        val features = hotspots.joinToString(",") { point ->
            """
            {
                "type": "Feature",
                "geometry": {
                    "type": "Point",
                    "coordinates": [${point.lng}, ${point.lat}]
                },
                "properties": {
                    "weight": ${point.weight},
                    "status": "${point.status}"
                }
            }
            """
        }
        """
        {
            "type": "FeatureCollection",
            "features": [$features]
        }
        """.trimIndent()
    }

    LaunchedEffect(Unit) {
        val res = com.wastereporting.network.ApiService.getAdminHeatmap()
        if (res.isSuccess) {
            val data = res.getOrNull()
            hotspots = data?.hotspots ?: emptyList()
            recommendations = data?.ai_recommendations ?: emptyList()
        }
    }

    LaunchedEffect(hotspots, mapInstance) {
        val map = mapInstance ?: return@LaunchedEffect
        map.getStyle(object : org.maplibre.android.maps.Style.OnStyleLoaded {
            override fun onStyleLoaded(style: org.maplibre.android.maps.Style) {
                val source = style.getSource("admin-heatmap-source") as? org.maplibre.android.style.sources.GeoJsonSource
                source?.setGeoJson(geoJsonData)
            }
        })
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE2E8F0))) {
        MapLibreView(
            modifier = Modifier.fillMaxSize()
        ) { map ->
            mapInstance = map
            map.setStyle("https://tiles.openfreemap.org/styles/liberty", object : org.maplibre.android.maps.Style.OnStyleLoaded {
                override fun onStyleLoaded(style: org.maplibre.android.maps.Style) {
                    val source = org.maplibre.android.style.sources.GeoJsonSource("admin-heatmap-source", geoJsonData)
                    style.addSource(source)
                    
                    val layer = org.maplibre.android.style.layers.HeatmapLayer("admin-heatmap-layer", "admin-heatmap-source")
                    style.addLayer(layer)
                }
            })
            map.cameraPosition = CameraPosition.Builder()
                .target(LatLng(12.9716, 77.5946))
                .zoom(13.0)
                .build()
        }

        // Top UI Layer
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Live Heatmap Monitoring", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("City Center District", fontSize = 12.sp, color = Color(0xFF64748B))
                }
                IconButton(onClick = { }, modifier = Modifier.size(36.dp).background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color(0xFF0F172A))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Chips
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                filters.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFF0F172A) else Color.White)
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) Color.White else Color(0xFF64748B),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Bottom AI Insights Panel
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = Color(0xFF8B5CF6), modifier = Modifier.size(20.dp))
                    Text(" AI Insights & Recommendations", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Active Hotspots", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text("3 Critical Zones", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    }
                    Column {
                        Text("Cleanliness", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text("78%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                    Column {
                        Text("ETA to Clear", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text("4.5 Hrs", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                    }
                }
                
                HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFFEF2F2)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444))
                    }
                    Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                        Text("AI Recommendations", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        recommendations.forEach { rec ->
                            Text("• $rec", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                        if (recommendations.isEmpty()) {
                            Text("No current recommendations.", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply Optimization Route", color = Color.White, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
