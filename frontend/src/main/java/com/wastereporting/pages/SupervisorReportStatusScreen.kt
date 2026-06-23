package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.wastereporting.components.AppBadge
import com.wastereporting.components.AppCard

@Composable
fun SupervisorReportStatusScreen(
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            .verticalScroll(scrollState)
    ) {
        // Top Bar
        Box(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color(0xFF1E293B))
            }
            Text(
                "Report Status",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF1E293B),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Green resolved box
        AppCard(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            backgroundColor = Color(0xFFDCFCE7),
            borderColor = Color(0xFF16A34A)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(32.dp))
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text("Your report has been\nresolved!", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF14532D))
                    Text("Today, 11:14 AM • by Supervisor EMP-2042", color = Color(0xFF166534), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Overflowing Public Bin", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))
                Text("RPT-8492 • 124 Main Street", color = Color(0xFF64748B), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }
            AppBadge("Completed", "success")
        }

        Text("Before & After", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B), modifier = Modifier.padding(bottom = 12.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            // Before
            AppCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE2E8F0))
                    ) {
                        AppBadge("Before", "error", modifier = Modifier.padding(8.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("10:30 AM", color = Color(0xFF64748B), fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            // After
            AppCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE2E8F0))
                    ) {
                        AppBadge("After", "success", modifier = Modifier.padding(8.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("11:14 AM", color = Color(0xFF16A34A), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                    Text("Supervisor Notes", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.padding(start = 8.dp))
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(12.dp)
                ) {
                    Text(
                        "\"Bin emptied, surrounding area swept, and overflowing trash collected.\"",
                        color = Color(0xFF475569),
                        fontSize = 14.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }

                Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF8FAFC)).border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)).padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Completed in", color = Color(0xFF64748B), fontSize = 12.sp)
                        Text("44 min", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.padding(top = 4.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(Color(0xFFF8FAFC)).border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)).padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Date", color = Color(0xFF64748B), fontSize = 12.sp)
                        Text("Today, 11:14 AM", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
