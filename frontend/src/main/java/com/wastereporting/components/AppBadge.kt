package com.wastereporting.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppBadge(text: String, variant: String = "info", modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (variant) {
        "info" -> Pair(Color(0xFFDBEAFE), Color(0xFF2563EB)) // Blue
        "warning" -> Pair(Color(0xFFFEF3C7), Color(0xFFD97706)) // Amber
        "success" -> Pair(Color(0xFFD1FAE5), Color(0xFF059669)) // Green
        else -> Pair(Color(0xFFF3F4F6), Color(0xFF374151)) // Gray
    }

    Text(
        text = text,
        color = textColor,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .background(bgColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
