package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.LocalHospital
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
fun ContactSupportScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // bg-slate-50
            .verticalScroll(scrollState)
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
                "Call City",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF1E293B),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp)) // balance
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Illustration Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEFF6FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color(0xFF3B82F6).copy(alpha = 0.5f))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Main Helpline Card
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Waste Management Helpline", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Available Mon-Fri, 9:00 AM - 5:00 PM", fontSize = 14.sp, color = Color(0xFF64748B))
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text("1-800-CITY-WASTE", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF16A34A), letterSpacing = 2.sp)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { /* Handle call action */ },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Call Now", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Emergency Card
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFFEF2F2)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.LocalHospital, contentDescription = "Emergency", tint = Color(0xFFEF4444))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Emergency Services", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                        Text("For immediate hazardous material spills.", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                    TextButton(onClick = { /* Handle 911 */ }) {
                        Text("Call 911", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
