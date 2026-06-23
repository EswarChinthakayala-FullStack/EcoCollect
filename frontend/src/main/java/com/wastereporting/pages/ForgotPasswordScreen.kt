package com.wastereporting.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.wastereporting.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit,
    onNavigateToReset: (String, String) -> Unit
) {
    var isOtpSent by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var timer by remember { mutableStateOf(30) }
    var isTimerActive by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    BackHandler {
        if (isOtpSent) {
            isOtpSent = false
            otp = ""
            errorMessage = null
        } else {
            onBackToLogin()
        }
    }

    LaunchedEffect(isTimerActive) {
        if (isTimerActive) {
            while (timer > 0) {
                delay(1000)
                timer--
            }
            isTimerActive = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (isOtpSent) {
                        isOtpSent = false
                        otp = ""
                        errorMessage = null
                    } else {
                        onBackToLogin()
                    }
                },
                modifier = Modifier.offset(x = (-12).dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E293B))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

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

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (!isOtpSent) "Forgot Password" else "Verify OTP",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (!isOtpSent) 
                    "Enter your registered email address to receive a verification code."
                else 
                    "We have sent a verification code to $email",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (!isOtpSent) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Email Address", color = Color(0xFF94A3B8)) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF64748B)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedBorderColor = Color(0xFF16A34A),
                        cursorColor = Color(0xFF16A34A)
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            val result = com.wastereporting.network.ApiService.forgotPassword(email)
                            isLoading = false
                            if (result.isSuccess) {
                                isOtpSent = true
                                timer = 30
                                isTimerActive = true
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Failed to send OTP"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF16A34A),
                        disabledContainerColor = Color(0xFFF1F5F9),
                        disabledContentColor = Color(0xFF94A3B8)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = email.isNotBlank() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Send OTP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            } else {
                OutlinedTextField(
                    value = otp,
                    onValueChange = { if (it.length <= 6) otp = it },
                    placeholder = { Text("Enter 6-digit OTP", color = Color(0xFF94A3B8)) },
                    leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color(0xFF64748B)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedBorderColor = Color(0xFF16A34A),
                        cursorColor = Color(0xFF16A34A)
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = {
                        if (otp.length != 6) {
                            errorMessage = "Please enter a valid 6-digit verification code"
                            return@Button
                        }
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            val result = com.wastereporting.network.ApiService.verifyPasswordResetOtp(email, otp)
                            isLoading = false
                            if (result.isSuccess) {
                                onNavigateToReset(email, otp)
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Invalid or expired verification code."
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF16A34A),
                        disabledContainerColor = Color(0xFFF1F5F9),
                        disabledContentColor = Color(0xFF94A3B8)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = otp.length == 6 && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Verify OTP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Didn't receive the code? ",
                        color = Color(0xFF64748B),
                        fontSize = 14.sp
                    )
                    if (timer > 0) {
                        Text(
                            "Resend in ${timer}s",
                            color = Color(0xFF16A34A),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        TextButton(
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                scope.launch {
                                    val result = com.wastereporting.network.ApiService.forgotPassword(email)
                                    isLoading = false
                                    if (result.isSuccess) {
                                        timer = 30
                                        isTimerActive = true
                                    } else {
                                        errorMessage = result.exceptionOrNull()?.message ?: "Failed to resend OTP"
                                    }
                                }
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Resend OTP", color = Color(0xFF16A34A), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            TextButton(
                onClick = onBackToLogin,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text("Back to Login", color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            }
        }
    }
}
