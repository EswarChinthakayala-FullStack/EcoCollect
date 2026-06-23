package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wastereporting.components.AppBadge
import com.wastereporting.components.AppCard
import com.wastereporting.network.ApiService
import com.wastereporting.network.User
import kotlinx.coroutines.launch

@Composable
fun UserProfileScreen(
    onNavigateToHelp: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    
    var user by remember { mutableStateOf<User?>(null) }
    var totalReports by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val result = ApiService.getProfile()
        if (result.isSuccess) {
            user = result.getOrNull()
        } else {
            errorMessage = result.exceptionOrNull()?.message
        }

        val dashboardResult = ApiService.getCitizenDashboard()
        if (dashboardResult.isSuccess) {
            totalReports = dashboardResult.getOrNull()?.total_reports ?: 0
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // bg-slate-50
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
            Text("Profile", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF1E293B))
        }

        // Avatar
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Color(0xFFE2E8F0)),
            contentAlignment = Alignment.Center
        ) {
            val avatarUrl = user?.profile_image_url
            if (!avatarUrl.isNullOrBlank()) {
                com.wastereporting.components.NetworkImage(
                    url = avatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Icon(Icons.Default.Person, contentDescription = "Avatar", modifier = Modifier.size(48.dp), tint = Color(0xFF94A3B8))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(user?.full_name ?: "Loading...", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        Text(user?.email ?: "", fontSize = 14.sp, color = Color(0xFF64748B))
        Spacer(modifier = Modifier.height(8.dp))
        // Stats Row
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(totalReports.toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Text("Reports Made", fontSize = 12.sp, color = Color(0xFF64748B))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Menu List
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                MenuItem(icon = Icons.Default.Edit, text = "Edit Profile", iconColor = Color(0xFFF59E0B), onClick = onNavigateToEditProfile)
                HorizontalDivider(color = Color(0xFFF1F5F9))
                MenuItem(icon = Icons.AutoMirrored.Filled.HelpOutline, text = "Help & Support", iconColor = Color(0xFF10B981), onClick = onNavigateToHelp)
                HorizontalDivider(color = Color(0xFFF1F5F9))
                MenuItem(icon = Icons.Default.Info, text = "About App", iconColor = Color(0xFF8B5CF6), onClick = onNavigateToAbout)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Logout Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFEF2F2)) // red-50
                .clickable { showLogoutDialog = true }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showLogoutDialog) {
        LogoutDialog(
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@Composable
fun MenuItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, iconColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = text, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
        }
        Icon(Icons.Default.ChevronRight, contentDescription = "Go", tint = Color(0xFF94A3B8))
    }
}

@Composable
fun LogoutDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFFEF2F2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = Color(0xFFEF4444))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Log Out", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF1E293B))
            }
        },
        text = {
            Text(
                "Are you sure you want to log out? You will need to enter your credentials again to access your account.",
                color = Color(0xFF64748B),
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF2F2), contentColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Yes, Log Out", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
            }
        }
    )
}
