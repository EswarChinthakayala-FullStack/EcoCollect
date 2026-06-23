package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wastereporting.components.AppCard

@Composable
fun HelpSupportScreen(
    onBack: () -> Unit,
    onNavigateToChat: () -> Unit = {},
    onNavigateToCall: () -> Unit = {},
    onNavigateToFaq: () -> Unit = {},
    onNavigateToEmail: () -> Unit = {}
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
                "Help & Support",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF1E293B),
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp)) // balance for back button
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Top Cards
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AppCard(modifier = Modifier.weight(1f).clickable { onNavigateToChat() }) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFEFF6FF)), contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Live Chat", tint = Color(0xFF3B82F6))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Live Chat", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Text("Avg response: 5m", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                }
                AppCard(modifier = Modifier.weight(1f).clickable { onNavigateToCall() }) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF0FDF4)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Call, contentDescription = "Call City", tint = Color(0xFF10B981))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Call City", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Text("Mon-Fri, 9am-5pm", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
                }
            }

            Text("Frequently Asked Questions", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B), modifier = Modifier.padding(bottom = 12.dp, start = 4.dp))
            
            AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                Column {
                    FaqItem("How do I report an overflowing bin?", onNavigateToFaq)
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    FaqItem("When is my recycling collection day?", onNavigateToFaq)
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    FaqItem("What items are considered bulky waste?", onNavigateToFaq)
                }
            }

            // Email Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFF16A34A), RoundedCornerShape(12.dp))
                    .clickable { onNavigateToEmail() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = "Email", tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send us an Email", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FaqItem(question: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(question, fontWeight = FontWeight.Medium, color = Color(0xFF334155), modifier = Modifier.weight(1f))
        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Expand", tint = Color(0xFF94A3B8))
    }
}
