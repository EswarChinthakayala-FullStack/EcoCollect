package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Filter1
import androidx.compose.material.icons.filled.Filter2
import androidx.compose.material.icons.filled.Filter3
import androidx.compose.material.icons.filled.Lightbulb
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
fun FAQDetailsScreen(onBack: () -> Unit, onNeedMoreHelp: () -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
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
                "FAQ Details",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF1E293B),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp)) // balance
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "How do I report an overflowing bin?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Steps
            AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    StepItem(icon = Icons.Default.Filter1, title = "Open the Report tab", desc = "Tap on the '+' icon located in the bottom navigation bar of your app.")
                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))
                    StepItem(icon = Icons.Default.Filter2, title = "Take a clear photo", desc = "Capture an image clearly showing the bin and the surrounding overflowing waste.")
                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))
                    StepItem(icon = Icons.Default.Filter3, title = "Select Category & Submit", desc = "Choose 'Overflowing Bin' from the category list, confirm the GPS location, and hit submit!")
                }
            }

            // Helpful Tips
            AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                Row(modifier = Modifier.padding(16.dp).background(Color(0xFFFFFBEB), RoundedCornerShape(8.dp)).padding(16.dp)) {
                    Icon(Icons.Default.Lightbulb, contentDescription = "Tip", tint = Color(0xFFF59E0B))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Pro Tip", fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                        Text("Ensure your device's location services are turned on so our city trucks can find the exact bin accurately.", fontSize = 14.sp, color = Color(0xFF92400E))
                    }
                }
            }

            // Still need help
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Still need help?", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onNeedMoreHelp,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Contact Support", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun StepItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFEFF6FF)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(desc, color = Color(0xFF64748B), fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}
