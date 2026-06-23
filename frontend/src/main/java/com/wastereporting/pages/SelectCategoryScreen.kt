package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wastereporting.components.AppButton

import com.wastereporting.network.IssueDraft

@Composable
fun SelectCategoryScreen(onBack: () -> Unit, onContinue: () -> Unit) {
    var selectedCategory by remember { mutableStateOf<String?>(if (IssueDraft.category.isNotEmpty()) IssueDraft.category else null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // bg-slate-50
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color(0xFF1E293B))
            }
            Text(
                "Select Category",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF1E293B),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp)) // balance
        }

        Text("What type of waste?", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Categorizing helps us send the right team.", color = Color(0xFF64748B), fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(32.dp))

        // Grid
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CategoryCard(
                    modifier = Modifier.weight(1f),
                    title = "Overflowing Bin",
                    iconColor = Color(0xFFF59E0B),
                    icon = Icons.Default.DeleteOutline,
                    isSelected = selectedCategory == "Overflowing Bin",
                    onClick = { selectedCategory = "Overflowing Bin" }
                )
                CategoryCard(
                    modifier = Modifier.weight(1f),
                    title = "Illegal Dumping",
                    iconColor = Color(0xFFEF4444),
                    icon = Icons.Default.WarningAmber,
                    isSelected = selectedCategory == "Illegal Dumping",
                    onClick = { selectedCategory = "Illegal Dumping" }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CategoryCard(
                    modifier = Modifier.weight(1f),
                    title = "Recycling Issue",
                    iconColor = Color(0xFF3B82F6),
                    icon = Icons.Outlined.Recycling,
                    isSelected = selectedCategory == "Recycling Issue",
                    onClick = { selectedCategory = "Recycling Issue" }
                )
                CategoryCard(
                    modifier = Modifier.weight(1f),
                    title = "Green Waste",
                    iconColor = Color(0xFF10B981),
                    icon = Icons.Outlined.Eco,
                    isSelected = selectedCategory == "Green Waste",
                    onClick = { selectedCategory = "Green Waste" }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CategoryCard(
                    modifier = Modifier.weight(1f),
                    title = "Bulky Items",
                    iconColor = Color(0xFF8B5CF6),
                    icon = Icons.Default.Inventory,
                    isSelected = selectedCategory == "Bulky Items",
                    onClick = { selectedCategory = "Bulky Items" }
                )
                CategoryCard(
                    modifier = Modifier.weight(1f),
                    title = "Other",
                    iconColor = Color(0xFF64748B),
                    icon = Icons.Default.DeleteOutline, // Just generic icon
                    isSelected = selectedCategory == "Other",
                    onClick = { selectedCategory = "Other" }
                )
            }
        }

        // Continue Button
        AppButton(
            text = "Continue",
            onClick = { 
                if (selectedCategory != null) {
                    IssueDraft.category = selectedCategory!!
                    onContinue() 
                }
            },
            modifier = Modifier.fillMaxWidth(),
            disabled = selectedCategory == null
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun CategoryCard(
    modifier: Modifier = Modifier, 
    title: String, 
    iconColor: Color, 
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFF16A34A) else Color(0xFFF1F5F9)
    val bgColor = if (isSelected) Color(0xFFF0FDF4) else Color.White

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 24.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White), // For inner contrast if needed, but mockup shows just icon
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFF1E293B), textAlign = TextAlign.Center)
        }
    }
}
