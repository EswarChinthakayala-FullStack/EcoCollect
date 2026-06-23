package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.WorkspacePremium
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

@Composable
fun ReportSubmittedScreen(onBackToDashboard: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF16A34A)) // Solid green background
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Checkmark Circle
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(2.dp), // Inner border effect
                contentAlignment = Alignment.Center
            ) {
                // Inner green circle
                Box(
                    modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color.Transparent).padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Success", tint = Color(0xFF16A34A), modifier = Modifier.size(48.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Report Submitted!", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Thank you for helping keep our city\nclean. Our team has been notified.",
            color = Color(0xFFDCFCE7), // light green
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(48.dp))



        Spacer(modifier = Modifier.weight(1f))

        // Back Button
        Button(
            onClick = onBackToDashboard,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF16A34A)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Back to Dashboard", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}
