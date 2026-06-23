package com.wastereporting.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    borderColor: Color = Color.Transparent,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardModifier = modifier.padding(8.dp).then(
        if (borderColor != Color.Transparent) Modifier.border(1.dp, borderColor, RoundedCornerShape(12.dp)) else Modifier
    )
    val cardColors = CardDefaults.cardColors(containerColor = backgroundColor)
    val cardElevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    val cardShape = RoundedCornerShape(12.dp)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = cardModifier,
            shape = cardShape,
            colors = cardColors,
            elevation = cardElevation
        ) {
            content()
        }
    } else {
        Card(
            modifier = cardModifier,
            shape = cardShape,
            colors = cardColors,
            elevation = cardElevation
        ) {
            content()
        }
    }
}
