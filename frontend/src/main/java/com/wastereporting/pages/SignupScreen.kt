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

@Composable
fun SignupScreen(onNavigateToDashboard: () -> Unit, onNavigateToLogin: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AppCard(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Create Account", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Join EcoReport and help keep our city clean", color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                AppInput(
                    label = "Full Name",
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Enter your full name"
                )
                
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
                    placeholder = "Create a password",
                    isPassword = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                AppButton(
                    text = "Sign Up",
                    onClick = onNavigateToDashboard,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Already have an account? ")
                    TextButton(onClick = onNavigateToLogin, contentPadding = PaddingValues(0.dp)) {
                        Text("Sign in")
                    }
                }
            }
        }
    }
}
