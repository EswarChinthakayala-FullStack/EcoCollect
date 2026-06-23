package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.Image
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wastereporting.network.ApiService
import kotlinx.coroutines.launch
import androidx.compose.ui.res.painterResource
import com.wastereporting.R

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
fun CreateAccountScreen(
    onBack: () -> Unit,
    onContinue: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top Bar
        Box(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color(0xFF1E293B))
            }
            Text(
                "Create Account",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF1E293B),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFDCFCE7)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.app_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(20.dp))
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("EcoCollect", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Text("Create an account to continue", fontSize = 14.sp, color = Color(0xFF64748B))

            Spacer(modifier = Modifier.height(24.dp))
        
        SignUpTextField(
            label = "Full Name",
            value = fullName,
            onValueChange = { fullName = it },
            placeholder = "John Doe",
            leadingIcon = Icons.Default.AccountCircle
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        SignUpTextField(
            label = "Email",
            value = email,
            onValueChange = { email = it },
            placeholder = "john@example.com",
            leadingIcon = Icons.Default.Email
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        SignUpTextField(
            label = "Phone Number",
            value = phone,
            onValueChange = { phone = it },
            placeholder = "+1 234 567 8900",
            leadingIcon = Icons.Default.Phone
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        SignUpTextField(
            label = "Date of Birth",
            value = dob,
            onValueChange = { dob = it },
            placeholder = "Select Date of Birth",
            leadingIcon = Icons.Default.CalendarToday,
            onClick = { showDatePicker = true }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        SignUpTextField(
            label = "Password",
            value = password,
            onValueChange = { password = it },
            placeholder = "••••••••",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            passwordVisible = passwordVisible,
            onVisibilityToggle = { passwordVisible = !passwordVisible }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        SignUpTextField(
            label = "Confirm Password",
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = "••••••••",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            passwordVisible = confirmPasswordVisible,
            onVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible }
        )

        Spacer(modifier = Modifier.height(32.dp))
        
        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        Button(
            onClick = {
                // Validations
                if (fullName.isBlank() || email.isBlank() || phone.isBlank() || dob.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                    errorMessage = "All fields are required"
                    return@Button
                }
                val trimmedEmail = email.trim()
                if (!trimmedEmail.endsWith("@gmail.com", ignoreCase = true) || trimmedEmail.length <= 10) {
                    errorMessage = "Email must be a valid @gmail.com address"
                    return@Button
                }
                if (!phone.matches(Regex("^[6-9]\\d{9}$"))) {
                    errorMessage = "Phone must be exactly 10 digits and start with 6, 7, 8, or 9"
                    return@Button
                }
                if (password.length < 8) {
                    errorMessage = "Password must be at least 8 characters"
                    return@Button
                }
                if (password != confirmPassword) {
                    errorMessage = "Passwords do not match"
                    return@Button
                }
                
                isLoading = true
                errorMessage = null
                coroutineScope.launch {
                    val result = ApiService.register(
                        com.wastereporting.network.RegisterRequest(
                            full_name = fullName,
                            email = email,
                            phone = phone,
                            dob = dob,
                            password = password
                        )
                    )
                    isLoading = false
                    if (result.isSuccess) {
                        onContinue(email)
                    } else {
                        errorMessage = result.exceptionOrNull()?.message ?: "Registration failed"
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("Sign Up", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 16.sp)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Already have an account? ", color = Color(0xFF64748B), fontSize = 14.sp)
            Text(
                "Click here",
                color = Color(0xFF2563EB),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onBack() }.padding(4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onVisibilityToggle: () -> Unit = {},
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF475569),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder, color = Color(0xFF94A3B8)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = enabled,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedBorderColor = Color(0xFF2563EB),
                    unfocusedContainerColor = Color(0xFFF8FAFC),
                    focusedContainerColor = Color(0xFFF8FAFC),
                    disabledBorderColor = Color(0xFFE2E8F0),
                    disabledContainerColor = Color(0xFFF1F5F9),
                    disabledTextColor = Color(0xFF64748B)
                ),
                singleLine = true,
                readOnly = onClick != null || !enabled,
                leadingIcon = {
                    Icon(leadingIcon, contentDescription = null, tint = Color(0xFF94A3B8))
                },
                trailingIcon = if (isPassword) {
                    {
                        IconButton(onClick = onVisibilityToggle) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }
                } else null,
                visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None
            )
            
            if (onClick != null) {
                Box(modifier = Modifier.matchParentSize().background(Color.Transparent).clickable { onClick() })
            }
        }
    }
}
