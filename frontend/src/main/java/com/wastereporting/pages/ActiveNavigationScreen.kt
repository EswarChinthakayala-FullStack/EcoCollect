package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wastereporting.components.AppButton
import com.wastereporting.components.AppCard

@Composable
fun ActiveNavigationScreen(
    onArrived: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE2E8F0)) // map placeholder bg
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            
            // Map Line Placeholder
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.width(6.dp).fillMaxHeight(0.6f).background(Color(0xFF3B82F6)))
                // Destination Pin
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = 100.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                }
            }

            // Top Direction Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF16A34A))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Straight", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("500 ft", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                        Text("Continue straight on Main St.", color = Color(0xFFDCFCE7), fontSize = 14.sp)
                    }
                }
            }

            // Bottom Info Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text("12 min", fontWeight = FontWeight.Bold, fontSize = 32.sp, color = Color(0xFF1E293B))
                                Text("1.2 mi • 10:45 AM arrival", color = Color(0xFF64748B), fontSize = 14.sp)
                            }
                            
                            IconButton(
                                onClick = onClose,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEE2E2))
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFFEF4444))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        AppButton(
                            text = "Arrived at Location",
                            onClick = onArrived,
                            modifier = Modifier.fillMaxWidth(),
                            variant = "outline" // matching mockup
                        )
                    }
                }
            }
        }
    }
}
