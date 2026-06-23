package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.ContentScale
import com.wastereporting.components.NetworkImage
import com.wastereporting.components.rememberImagePicker
import com.wastereporting.network.ApiService

@Composable
fun AdminEditProfileScreen(
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var adminName by remember { mutableStateOf("") }
    var adminId by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Admin") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }

    var isSaving by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val res = com.wastereporting.network.ApiService.getProfile()
        if (res.isSuccess) {
            val user = res.getOrNull()
            adminName = user?.full_name ?: ""
            adminId = user?.employee_id ?: "ADM-${user?.id ?: ""}"
            email = user?.email ?: ""
            phone = user?.phone ?: ""
            department = user?.department ?: ""
            profileImageUrl = user?.profile_image_url
        }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(16.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color.White)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    "Admin Profile Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
                Text(
                    "Manage administrator account information",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        HorizontalDivider(color = Color(0xFF334155))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Profile Image Section
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .clickable { imagePicker.launchGallery() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isUploadingImage) {
                        CircularProgressIndicator(color = Color(0xFF60A5FA), modifier = Modifier.size(24.dp))
                    } else if (!profileImageUrl.isNullOrEmpty()) {
                        NetworkImage(
                            url = ApiService.getFullImageUrl(profileImageUrl!!),
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.AdminPanelSettings,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.size(60.dp),
                            tint = Color(0xFF60A5FA)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .offset(x = 4.dp, y = 4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B82F6))
                        .clickable { imagePicker.launchGallery() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Change Photo",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = "Change Profile Picture",
                color = Color(0xFF60A5FA),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { imagePicker.launchGallery() }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Form Fields
            AdminTextField(
                label = "Admin Name",
                value = adminName,
                onValueChange = { adminName = it },
                icon = Icons.Default.Person
            )
            Spacer(modifier = Modifier.height(16.dp))

            AdminTextField(
                label = "Admin ID",
                value = adminId,
                onValueChange = { adminId = it },
                icon = Icons.Default.Badge,
                readOnly = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            AdminTextField(
                label = "Email Address",
                value = email,
                onValueChange = { email = it },
                icon = Icons.Default.Email,
                readOnly = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            AdminTextField(
                label = "Phone Number",
                value = phone,
                onValueChange = { phone = it },
                icon = Icons.Default.Phone
            )
            Spacer(modifier = Modifier.height(16.dp))

            AdminTextField(
                label = "Department",
                value = department,
                onValueChange = { department = it },
                icon = Icons.Default.Business
            )
            Spacer(modifier = Modifier.height(16.dp))

            AdminTextField(
                label = "Role",
                value = role,
                onValueChange = { role = it },
                icon = Icons.Default.Security,
                readOnly = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (showSuccessMessage) {
                Surface(
                    color = Color(0xFF064E3B).copy(alpha = 0.3f), // Dark green background
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Text(
                        "Administrator Profile Updated Successfully",
                        color = Color(0xFF34D399), // Light green text
                        modifier = Modifier.padding(12.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            // Action Buttons
            Button(
                onClick = {
                    isSaving = true
                    scope.launch {
                        val res = com.wastereporting.network.ApiService.updateProfile(
                            mapOf(
                                "full_name" to adminName,
                                "phone" to phone,
                                "department" to department
                            )
                        )
                        isSaving = false
                        if (res.isSuccess) {
                            showSuccessMessage = true
                            delay(2000)
                            onBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", color = Color(0xFF94A3B8), fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    readOnly: Boolean = false
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            readOnly = readOnly,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = if (readOnly) Color(0xFF1E293B) else Color(0xFF334155),
                focusedBorderColor = if (readOnly) Color(0xFF1E293B) else Color(0xFF60A5FA),
                focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.5f),
                unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.5f),
                unfocusedTextColor = if (readOnly) Color(0xFF64748B) else Color.White,
                focusedTextColor = if (readOnly) Color(0xFF64748B) else Color.White,
                cursorColor = Color(0xFF60A5FA)
            ),
            singleLine = true,
            leadingIcon = {
                Icon(icon, contentDescription = null, tint = Color(0xFF64748B))
            }
        )
    }
}
