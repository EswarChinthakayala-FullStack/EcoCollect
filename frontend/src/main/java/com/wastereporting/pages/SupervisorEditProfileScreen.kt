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

@Composable
fun SupervisorEditProfileScreen(
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var fullName by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var assignedArea by remember { mutableStateOf("") }

    var isSaving by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val res = com.wastereporting.network.ApiService.getProfile()
        if (res.isSuccess) {
            val user = res.getOrNull()
            fullName = user?.full_name ?: ""
            employeeId = user?.employee_id ?: ""
            email = user?.email ?: ""
            phone = user?.phone ?: ""
            assignedArea = user?.assigned_area ?: ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color(0xFF1E293B))
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    "Edit Profile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF1E293B)
                )
                Text(
                    "Update your supervisor information",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        HorizontalDivider(color = Color(0xFFE2E8F0))

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
                        .background(Color(0xFFDBEAFE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(60.dp),
                        tint = Color(0xFF3B82F6)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .offset(x = 4.dp, y = 4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2563EB))
                        .clickable { /* Handle photo upload */ },
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

            Text("Change Photo", color = Color(0xFF2563EB), fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.clickable { })

            Spacer(modifier = Modifier.height(32.dp))

            // Form Fields
            SignUpTextField(
                label = "Full Name",
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = "Full Name",
                leadingIcon = Icons.Default.Person
            )
            Spacer(modifier = Modifier.height(16.dp))

            SignUpTextField(
                label = "Employee ID",
                value = employeeId,
                onValueChange = { employeeId = it },
                placeholder = "Employee ID",
                leadingIcon = Icons.Default.Badge,
                enabled = false
            )
            Spacer(modifier = Modifier.height(16.dp))

            SignUpTextField(
                label = "Email Address",
                value = email,
                onValueChange = { email = it },
                placeholder = "Email Address",
                leadingIcon = Icons.Default.Email,
                enabled = false
            )
            Spacer(modifier = Modifier.height(16.dp))

            SignUpTextField(
                label = "Phone Number",
                value = phone,
                onValueChange = { phone = it },
                placeholder = "Phone Number",
                leadingIcon = Icons.Default.Phone
            )
            Spacer(modifier = Modifier.height(16.dp))

            SignUpTextField(
                label = "Assigned Area",
                value = assignedArea,
                onValueChange = { assignedArea = it },
                placeholder = "Assigned Area",
                leadingIcon = Icons.Default.LocationOn,
                enabled = false
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (showSuccessMessage) {
                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Text(
                        "Profile Updated Successfully",
                        color = Color(0xFF16A34A),
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
                                "full_name" to fullName,
                                "phone" to phone,
                                "assigned_area" to assignedArea
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", color = Color(0xFF64748B), fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
