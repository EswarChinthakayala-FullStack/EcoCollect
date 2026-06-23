package com.wastereporting.pages

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wastereporting.network.ApiService
import com.wastereporting.components.rememberImagePicker
import kotlinx.coroutines.launch

private fun formatMillis(millis: Long?): String {
    if (millis == null) return ""
    val totalDays = (millis / (1000 * 60 * 60 * 24)).toInt()
    var days = totalDays
    var year = 1970
    while (true) {
        val daysInYear = if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 366 else 365
        if (days >= daysInYear) {
            days -= daysInYear
            year++
        } else {
            break
        }
    }
    val leapYear = (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0))
    val daysInMonth = intArrayOf(31, if (leapYear) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    var month = 0
    while (month < 12) {
        if (days >= daysInMonth[month]) {
            days -= daysInMonth[month]
            month++
        } else {
            break
        }
    }
    val d = days + 1
    val m = month + 1
    return "${if (d < 10) "0$d" else d}-${if (m < 10) "0$m" else m}-$year"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onBack: () -> Unit, onSave: () -> Unit) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var fullName by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var profileImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberImagePicker { bytes ->
        if (bytes != null) profileImageBytes = bytes
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    dob = formatMillis(datePickerState.selectedDateMillis)
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    LaunchedEffect(Unit) {
        val result = ApiService.getProfile()
        isLoading = false
        if (result.isSuccess) {
            val user = result.getOrNull()
            if (user != null) {
                fullName = user.full_name
                email = user.email
                phone = user.phone ?: ""
                dob = user.dob ?: ""
                gender = user.gender ?: ""
                address = user.address ?: ""
                city = user.city ?: ""
                country = user.country ?: ""
                profileImageUrl = user.profile_image_url
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color(0xFF1E293B))
            }
        }

        // Header Card & Avatar
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 80.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Edit Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Update your profile information", fontSize = 14.sp, color = Color(0xFF64748B))
                }
            }

            Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2E8F0))
                        .border(4.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImageBytes != null) {
                        val bitmap = remember(profileImageBytes) {
                            profileImageBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() }
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = "Profile Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Text("Image Set", fontSize = 12.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                        }
                    } else if (!profileImageUrl.isNullOrBlank()) {
                        com.wastereporting.components.NetworkImage(
                            url = profileImageUrl!!,
                            contentDescription = "Profile Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = "Profile", modifier = Modifier.size(64.dp), tint = Color(0xFF94A3B8))
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B82F6))
                        .border(2.dp, Color.White, CircleShape)
                        .clickable { imagePicker.launchGallery() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Edit Photo", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Basic Information
        Text("Basic Information", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(16.dp))
        
        ProfileTextField("Full Name", fullName) { fullName = it }
        Spacer(modifier = Modifier.height(12.dp))
        Column {
            Text("Date of Birth", fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 4.dp))
            Box(modifier = Modifier.clickable { showDatePicker = true }) {
                OutlinedTextField(
                    value = dob,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedBorderColor = Color(0xFF3B82F6),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true,
                    readOnly = true,
                    enabled = false
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        ProfileTextField("Gender", gender) { gender = it }

        Spacer(modifier = Modifier.height(24.dp))

        // Contact Information
        Text("Contact Information", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(16.dp))
        
        ProfileTextField("Email", email, enabled = false) { email = it }
        Spacer(modifier = Modifier.height(12.dp))
        ProfileTextField("Phone Number", phone) { phone = it }

        Spacer(modifier = Modifier.height(24.dp))

        // Address Information
        Text("Address Information", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(16.dp))
        
        ProfileTextField("Street Address", address) { address = it }
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                ProfileTextField("City", city) { city = it }
            }
            Box(modifier = Modifier.weight(1f)) {
                ProfileTextField("Country", country) { country = it }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        Button(
            onClick = {
                if (phone.isNotEmpty() && !phone.matches(Regex("^[6-9]\\d{9}$"))) {
                    errorMessage = "Phone must be exactly 10 digits and start with 6, 7, 8, or 9"
                    return@Button
                }
                isSaving = true
                errorMessage = null
                coroutineScope.launch {
                    val data = mutableMapOf(
                        "full_name" to fullName,
                        "phone" to phone,
                        "gender" to gender,
                        "address" to address
                    )
                    if (dob.isNotEmpty()) {
                        data["dob"] = dob
                    }

                    val result = ApiService.updateProfile(data, profileImageBytes)
                    if (result.isSuccess) {
                        onSave()
                    } else {
                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to update profile"
                        isSaving = false
                    }
                }
            },
            enabled = !isLoading && !isSaving,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
        ) {
            if (isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTextField(
    label: String,
    value: String,
    enabled: Boolean = true,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(label, fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedBorderColor = Color(0xFF3B82F6),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = if (enabled) Color.White else Color(0xFFF1F5F9),
                disabledContainerColor = Color(0xFFF1F5F9),
                disabledBorderColor = Color(0xFFE2E8F0),
                disabledTextColor = Color(0xFF64748B)
            ),
            singleLine = true
        )
    }
}
