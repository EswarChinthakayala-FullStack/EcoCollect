package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wastereporting.components.AppCard
import com.wastereporting.components.MapLibreView

import org.maplibre.android.geometry.LatLng
import org.maplibre.android.camera.CameraPosition

@Composable
fun IssueHeatmapScreen(onBack: () -> Unit, onNavigateToNavigation: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE2E8F0)) // placeholder map bg
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            MapLibreView(
                modifier = Modifier.fillMaxSize()
            ) { map ->
                map.setStyle("https://tiles.openfreemap.org/styles/liberty", object : org.maplibre.android.maps.Style.OnStyleLoaded {
                    override fun onStyleLoaded(style: org.maplibre.android.maps.Style) {
                        val source = org.maplibre.android.style.sources.GeoJsonSource(
                            "heatmap-source",
                            """
                                {
                                    "type": "FeatureCollection",
                                    "features": [
                                        { "type": "Feature", "geometry": { "type": "Point", "coordinates": [77.5946, 12.9716] } },
                                        { "type": "Feature", "geometry": { "type": "Point", "coordinates": [77.5950, 12.9720] } },
                                        { "type": "Feature", "geometry": { "type": "Point", "coordinates": [77.5940, 12.9710] } },
                                        { "type": "Feature", "geometry": { "type": "Point", "coordinates": [77.5960, 12.9730] } },
                                        { "type": "Feature", "geometry": { "type": "Point", "coordinates": [77.5910, 12.9700] } },
                                        { "type": "Feature", "geometry": { "type": "Point", "coordinates": [77.5890, 12.9690] } }
                                    ]
                                }
                            """.trimIndent()
                        )
                        style.addSource(source)
                        
                        val layer = org.maplibre.android.style.layers.HeatmapLayer("heatmap-layer", "heatmap-source")
                        style.addLayer(layer)
                    }
                })
                
                map.cameraPosition = CameraPosition.Builder()
                    .target(LatLng(12.9716, 77.5946))
                    .zoom(14.0)
                    .build()
            }

            // Top Bar Overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color(0xFF1E293B))
                }
                Text(
                    "Issue Heatmap",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = Color(0xFF1E293B))
                }
            }

            // Bottom Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                        Text("Report Density", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Scale
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Low", fontSize = 12.sp, color = Color(0xFF64748B))
                            Text("High", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF10B981), Color(0xFFF59E0B), Color(0xFFEF4444))
                                    )
                                )
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically, 
                            modifier = Modifier.fillMaxWidth().clickable { onNavigateToNavigation() }.padding(vertical = 4.dp)
                        ) {
                            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFFEE2E2)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("High Activity Area", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                                Text("Downtown District • 12 active reports", fontSize = 12.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                }
            }
        }
    }
}
