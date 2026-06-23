package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.WaterDrop
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
fun EnvironmentalImpactScreen(onBack: () -> Unit) {
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
                "Environmental Impact",
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
            // Eco Score Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF16A34A))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Community Eco Score", color = Color(0xFFDCFCE7), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("A-", fontWeight = FontWeight.Bold, fontSize = 64.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Top 15% of cities nationwide", color = Color(0xFFDCFCE7), fontSize = 12.sp)
                }
                
                // Background decoration (Leaf outline) could be added here
                Icon(Icons.Default.Forest, contentDescription = null, tint = Color.White.copy(alpha = 0.1f), modifier = Modifier.size(120.dp).align(Alignment.BottomEnd).offset(x = 30.dp, y = 30.dp))
            }

            Text("This Month's Savings", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B), modifier = Modifier.padding(bottom = 16.dp))
            
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SavingsCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Air,
                    iconColor = Color(0xFF3B82F6),
                    value = "2.4t",
                    label = "CO2 Prevented"
                )
                SavingsCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.WaterDrop,
                    iconColor = Color(0xFF06B6D4),
                    value = "15k",
                    label = "Gallons Water Saved"
                )
            }
            
            AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFDCFCE7)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Forest, contentDescription = "Trees", tint = Color(0xFF16A34A))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("142 Trees", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Equivalent trees planted based on paper recycling efforts.", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }
            }

            // Improve Button
            AppButton(
                text = "How to improve?",
                onClick = { },
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
            )
        }
    }
}

@Composable
fun SavingsCard(modifier: Modifier = Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color, value: String, label: String) {
    AppCard(modifier = modifier) {
        Column(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(iconColor.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 12.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
        }
    }
}
