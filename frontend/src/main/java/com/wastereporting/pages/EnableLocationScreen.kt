package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
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

@Composable
fun EnableLocationScreen(
    onAllow: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.2f))

        // Icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFEFF6FF)), // light blue
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Navigation, contentDescription = "Location", tint = Color(0xFF3B82F6), modifier = Modifier.size(32.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Enable Location",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "We need your location to accurately pinpoint waste reports and show you nearby collection points.",
            fontSize = 14.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Info Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF8FAFC))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Precise Location", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontSize = 14.sp)
                Text("Used only when app is active to tag reports.", color = Color(0xFF64748B), fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.weight(0.3f))

        AppButton(
            text = "Allow Location Access",
            onClick = onAllow,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onSkip) {
            Text("Maybe Later", color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
