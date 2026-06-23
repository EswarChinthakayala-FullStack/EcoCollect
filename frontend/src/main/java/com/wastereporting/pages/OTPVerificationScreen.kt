package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.res.painterResource
import com.wastereporting.R

@Composable
fun OTPVerificationScreen(
    email: String,
    onBack: () -> Unit,
    onVerifySuccess: () -> Unit
) {
    var otp by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

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
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

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
                "Verify your email",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "We've sent a 6-digit verification code to\n$email",
                fontSize = 15.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Real OTP integration enabled

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 6) otp = it },
                placeholder = { Text("Enter 6-digit code", color = Color(0xFF94A3B8)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF8FAFC),
                    focusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedIndicatorColor = Color(0xFFE2E8F0),
                    focusedIndicatorColor = Color(0xFF2563EB)
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 24.sp, letterSpacing = 8.sp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    if (otp.length != 6) {
                        errorMessage = "Please enter a valid 6-digit code"
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        val verifyResult = com.wastereporting.network.ApiService.verifyOtp(email, otp)
                        isLoading = false
                        if (verifyResult.isSuccess) {
                            onVerifySuccess()
                        } else {
                            errorMessage = verifyResult.exceptionOrNull()?.message ?: "Verification failed"
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
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Verify Code", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Didn't receive the code?", color = Color(0xFF64748B), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Resend Code",
                color = Color(0xFF2563EB),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    scope.launch {
                        com.wastereporting.network.ApiService.sendOtp(
                            com.wastereporting.network.SendOtpRequest(email = email, full_name = "", phone = "", dob = "", password = "")
                        )
                    }
                }.padding(8.dp)
            )
        }
    }
}
