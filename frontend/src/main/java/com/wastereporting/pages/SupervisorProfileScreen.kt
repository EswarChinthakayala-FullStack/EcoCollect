package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Logout
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
import com.wastereporting.components.AppCard
import com.wastereporting.components.NetworkImage
import com.wastereporting.network.ApiService
import com.wastereporting.network.User

@Composable
fun SupervisorProfileScreen(
    onNavigateToDashboard: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onLogout: () -> Unit
) {
    val scrollState = rememberScrollState()
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val result = ApiService.getProfile()
        if (result.isSuccess) {
            user = result.getOrNull()
        } else {
            errorMessage = result.exceptionOrNull()?.message
        }
        isLoading = false
    }

    Scaffold(
        bottomBar = {
            SupervisorBottomNav(
                currentTab = "Profile",
                onTabSelected = { tab ->
                    when (tab) {
                        "Reports" -> onNavigateToDashboard()
                        "History" -> onNavigateToHistory()
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC))
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF3B82F6))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC))
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                        .background(Color(0xFF3B82F6))
                        .padding(top = 48.dp, bottom = 100.dp, start = 24.dp, end = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Supervisor Profile", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Profile Card Overlapping
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-80).dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier.size(96.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(96.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFDBEAFE)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val avatarUrl = user?.profile_image_url
                                    if (!avatarUrl.isNullOrBlank()) {
                                        NetworkImage(
                                            url = avatarUrl,
                                            contentDescription = "Avatar",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    } else {
                                        Icon(Icons.Default.Security, contentDescription = "Avatar", tint = Color(0xFF3B82F6), modifier = Modifier.size(48.dp))
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .background(Color(0xFF16A34A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("I", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp) // Online indicator
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(user?.full_name ?: "", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF1E293B))
                            
                            val displayRole = when (user?.role?.lowercase()) {
                                "supervisor" -> "Field Supervisor"
                                "admin" -> "Admin"
                                "citizen" -> "Citizen"
                                else -> user?.role ?: "Field Supervisor"
                            }
                            Text(displayRole, color = Color(0xFF64748B), fontSize = 14.sp)
                            
                            val empId = user?.employee_id
                            if (!empId.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFFEFF6FF))
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(empId, color = Color(0xFF3B82F6), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            val assignedArea = user?.assigned_area
                            if (!assignedArea.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFFF1F5F9))
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = "Assigned Area",
                                        tint = Color(0xFF3B82F6),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Area: $assignedArea", color = Color(0xFF475569), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            ProfileInfoRow(Icons.Default.LocationOn, "Assigned Area", user?.assigned_area ?: "Not Assigned")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))
                            ProfileInfoRow(Icons.Default.Phone, "Phone", user?.phone ?: "Not Provided")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))
                            ProfileInfoRow(Icons.Default.Email, "Email", user?.email ?: "")
                        }
                    }

                    // Quick Actions
                    AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Quick Actions", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.padding(bottom = 16.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().clickable { onNavigateToAbout() }.padding(vertical = 12.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = "About App", tint = Color(0xFF3B82F6))
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("About Application", color = Color(0xFF1E293B), fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8))
                            }
                        }
                    }
                    
                    Button(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Logout", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        SupervisorLogoutDialog(
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@Composable
fun SupervisorLogoutDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
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

@Composable
fun ProfileInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(label, color = Color(0xFF64748B), fontSize = 12.sp)
            Text(value, color = Color(0xFF1E293B), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
