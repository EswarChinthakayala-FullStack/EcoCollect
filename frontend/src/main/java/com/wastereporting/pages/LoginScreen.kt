package com.wastereporting.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wastereporting.components.AppButton
import com.wastereporting.components.AppCard
import com.wastereporting.components.AppInput
import com.wastereporting.network.ApiService
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onNavigateToDashboard: () -> Unit, onNavigateToSignup: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AppCard(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Welcome Back", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Sign in to your EcoReport account", color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                AppInput(
                    label = "Email Address",
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Enter your email"
                )
                
                AppInput(
                    label = "Password",
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Enter your password",
                    isPassword = true
                )
                
                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it }
                        )
                        Text("Remember me", fontSize = 14.sp)
                    }
                    TextButton(onClick = { /* Forgot password */ }) {
                        Text("Forgot Password?", fontSize = 14.sp)
                    }
                }
                
                AppButton(
                    text = if (isLoading) "Signing In..." else "Sign In",
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        coroutineScope.launch {
                            val result = ApiService.login(com.wastereporting.network.LoginRequest(email, password))
                            isLoading = false
                            if (result.isSuccess) {
                                onNavigateToDashboard()
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Login failed"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                
                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Don't have an account? ")
                    TextButton(onClick = onNavigateToSignup, contentPadding = PaddingValues(0.dp)) {
                        Text("Sign up")
                    }
                }
            }
        }
    }
}
