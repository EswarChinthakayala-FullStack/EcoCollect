package com.wastereporting.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: String = "primary", // primary, secondary, outline
    disabled: Boolean = false
) {
    val containerColor = when (variant) {
        "secondary" -> Color(0xFFF3F4F6)
        "outline" -> Color.Transparent
        else -> Color(0xFF10B981) // primary green
    }
    
    val contentColor = when (variant) {
        "secondary" -> Color(0xFF374151)
        "outline" -> Color(0xFF10B981)
        else -> Color.White
    }

    Button(
        onClick = onClick,
        enabled = !disabled,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f)
        )
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}
