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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import com.wastereporting.components.AppCard
import com.wastereporting.components.NetworkImage
import com.wastereporting.components.rememberImagePicker
import com.wastereporting.network.ApiService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(
    onNavigateToDashboard: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSupervisors: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var fullName by remember { mutableStateOf("Chief Administrator") }
    var email by remember { mutableStateOf("admin@ecocollect.city") }
    var phone by remember { mutableStateOf("+1 (555) 000-0001") }
    var employeeId by remember { mutableStateOf("ADM-001") }
    var department by remember { mutableStateOf("City Administration") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    
    var isLoading by remember { mutableStateOf(true) }
    var isUploadingImage by remember { mutableStateOf(false) }
    
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var passwordChangeLoading by remember { mutableStateOf(false) }
    var passwordChangeError by remember { mutableStateOf<String?>(null) }
    var passwordChangeSuccess by remember { mutableStateOf<String?>(null) }

    fun refreshProfile() {
        scope.launch {
            val res = ApiService.getProfile()
            if (res.isSuccess) {
                val user = res.getOrNull()
                if (user != null) {
                    fullName = user.full_name
                    email = user.email
                    phone = user.phone ?: ""
                    employeeId = user.employee_id ?: "ADM-${user.id}"
                    department = user.department ?: "City Administration"
                    profileImageUrl = user.profile_image_url
                }
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshProfile()
    }

    val imagePicker = rememberImagePicker { bytes ->
        if (bytes != null) {
            isUploadingImage = true
            scope.launch {
                val res = ApiService.updateProfile(emptyMap(), bytes)
                if (res.isSuccess) {
                    val updatedUser = res.getOrNull()
                    profileImageUrl = updatedUser?.profile_image_url
                }
                isUploadingImage = false
            }
        }
    }

    Scaffold(
        bottomBar = {
            AdminBottomNav(
                currentTab = "Profile",
                onTabSelected = { tab ->
                    when (tab) {
                        "Dashboard" -> onNavigateToDashboard()
                        "Reports" -> onNavigateToReports()
                        "Supervisors" -> onNavigateToSupervisors()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            // Header Profile Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(Color(0xFF0F172A))
                    .padding(top = 48.dp, bottom = 48.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier.clickable { imagePicker.launchGallery() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isUploadingImage) {
                                CircularProgressIndicator(color = Color(0xFF0F172A), modifier = Modifier.size(24.dp))
                            } else if (!profileImageUrl.isNullOrEmpty()) {
                                NetworkImage(
                                    url = ApiService.getFullImageUrl(profileImageUrl!!),
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                               )
                            } else {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(60.dp))
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3B82F6))
                                .border(2.dp, Color(0xFF0F172A), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Edit Picture", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(fullName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(email, fontSize = 14.sp, color = Color(0xFF94A3B8))

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF3B82F6).copy(alpha = 0.2f))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text("Super Admin Access", color = Color(0xFF93C5FD), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF3B82F6))
                }
            } else {
                Column(modifier = Modifier.padding(16.dp).offset(y = (-32).dp)) {

                    // Personal Information Card
                    AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Personal Information", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontSize = 16.sp, modifier = Modifier.padding(bottom = 16.dp))

                            AdminInfoRow(Icons.Default.Person, "Full Name", fullName)
                            HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 10.dp))
                            AdminInfoRow(Icons.Default.Email, "Email", email)
                            HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 10.dp))
                            AdminInfoRow(Icons.Default.Phone, "Phone", phone.ifBlank { "Not Provided" })
                            HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 10.dp))
                            AdminInfoRow(Icons.Default.LocationOn, "Department", department)
                        }
                    }

                    // Actions Card
                    AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Account Actions", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.padding(bottom = 16.dp))

                            AdminProfileOption(
                                icon = Icons.Default.Edit,
                                title = "Edit Profile",
                                subtitle = "Update personal and role info",
                                color = Color(0xFF3B82F6),
                                onClick = onNavigateToEditProfile
                            )
                            HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))
                            AdminProfileOption(
                                icon = Icons.Default.Lock,
                                title = "Change Password",
                                subtitle = "Update your security credentials",
                                color = Color(0xFF8B5CF6),
                                onClick = {
                                    currentPassword = ""
                                    newPassword = ""
                                    confirmPassword = ""
                                    passwordChangeError = null
                                    passwordChangeSuccess = null
                                    showChangePasswordDialog = true
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Logout Button
                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Logout Session", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }

    // Change Password Bottom Sheet Drawer
    if (showChangePasswordDialog) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { if (!passwordChangeLoading) showChangePasswordDialog = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Change Password",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF1E293B)
                    )
                    IconButton(
                        onClick = { if (!passwordChangeLoading) showChangePasswordDialog = false }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                Text(
                    text = "Update your account credentials to keep your profile secure.",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )

                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                )

                if (passwordChangeError != null) {
                    Text(
                        text = passwordChangeError!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (passwordChangeSuccess != null) {
                    Text(
                        text = passwordChangeSuccess!!,
                        color = Color(0xFF10B981),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showChangePasswordDialog = false },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        enabled = !passwordChangeLoading
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
                                passwordChangeError = "All fields are required"
                                return@Button
                            }
                            if (newPassword != confirmPassword) {
                                passwordChangeError = "Passwords do not match"
                                return@Button
                            }
                            passwordChangeLoading = true
                            passwordChangeError = null
                            passwordChangeSuccess = null
                            scope.launch {
                                val res = ApiService.changePassword(currentPassword, newPassword)
                                passwordChangeLoading = false
                                if (res.isSuccess) {
                                    passwordChangeSuccess = "Password updated successfully"
                                    kotlinx.coroutines.delay(1500)
                                    showChangePasswordDialog = false
                                } else {
                                    passwordChangeError = res.exceptionOrNull()?.message ?: "Failed to update password"
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !passwordChangeLoading
                    ) {
                        if (passwordChangeLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                        } else {
                            Text("Update Password", color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun AdminInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE2E8F0)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(label, fontSize = 11.sp, color = Color(0xFF94A3B8))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
        }
    }
}

@Composable
fun AdminProfileOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color)
        }
        Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
            Text(subtitle, fontSize = 12.sp, color = Color(0xFF64748B))
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8))
    }
}
