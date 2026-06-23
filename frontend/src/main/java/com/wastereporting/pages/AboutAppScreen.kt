package com.wastereporting.pages

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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wastereporting.components.AppCard
import com.wastereporting.R

enum class AboutSubScreen {
    TermsOfService, PrivacyPolicy, Licenses
}

@Composable
fun AboutAppScreen(onBack: () -> Unit) {
    var activeSubScreen by remember { mutableStateOf<AboutSubScreen?>(null) }

    when (activeSubScreen) {
        AboutSubScreen.TermsOfService -> TermsOfServiceScreen(onBack = { activeSubScreen = null })
        AboutSubScreen.PrivacyPolicy -> PrivacyPolicyScreen(onBack = { activeSubScreen = null })
        AboutSubScreen.Licenses -> OpenSourceLicensesScreen(onBack = { activeSubScreen = null })
        null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC)) // bg-slate-50
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color(0xFF1E293B))
                    }
                    Text(
                        "About App",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(48.dp)) // balance for back button
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    // App Logo and Version
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF16A34A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.app_logo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(64.dp).clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text("EcoCollect", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF1E293B))
                    Text("Version 2.4.1 (Build 492)", color = Color(0xFF64748B), fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(48.dp))

                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            AboutLinkItem(icon = Icons.Default.Description, text = "Terms of Service") {
                                activeSubScreen = AboutSubScreen.TermsOfService
                            }
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            AboutLinkItem(icon = Icons.Default.Policy, text = "Privacy Policy") {
                                activeSubScreen = AboutSubScreen.PrivacyPolicy
                            }
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            AboutLinkItem(icon = Icons.Default.Eco, text = "Open Source Licenses") {
                                activeSubScreen = AboutSubScreen.Licenses
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Footer
                    Text(
                        "Developed in partnership with the City\nWaste Management Department to build a\ncleaner, smarter city.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        "© 2024 EcoCollect Systems Inc.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AboutLinkItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = text, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
        }
        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Go to screen", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
    }
}

// ─────────────────────────────────────────────
// SUB-SCREENS
// ─────────────────────────────────────────────

@Composable
fun TermsOfServiceScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color(0xFF1E293B))
            }
            Text(
                "Terms of Service",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF1E293B),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Last updated: June 22, 2026",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            PolicySection(
                title = "1. Acceptance of Terms",
                content = "By accessing and using the EcoCollect mobile application, you agree to comply with and be bound by these Terms of Service. If you do not agree to these terms, please do not use the application."
            )
            PolicySection(
                title = "2. User Accounts & Registration",
                content = "To submit reports or track collections, you must register an account. You agree to provide accurate information and keep your login credentials secure. You are fully responsible for all activity on your account."
            )
            PolicySection(
                title = "3. Reporting Guidelines",
                content = "All waste incident reports submitted must contain truthful, accurate information, precise GPS coordinates, and real incident images. Submitting false reports, inappropriate images, or abusive comments is strictly prohibited and will result in permanent account suspension."
            )
            PolicySection(
                title = "4. Code of Conduct",
                content = "You agree not to disrupt or attempt to gain unauthorized access to our FastAPI backend services. Abusive behavior towards assigned supervisors or city administrative staff via contact options is a direct violation of these terms."
            )
            PolicySection(
                title = "5. Limitation of Liability",
                content = "EcoCollect Systems and the partner city departments strive for rapid response times, but do not guarantee immediate clearance of reported waste incidents. We are not liable for any environmental hazards or damages occurring prior to collection."
            )
            PolicySection(
                title = "6. Modifications to Service",
                content = "We reserve the right to modify or terminate any part of the EcoCollect service or these Terms of Service at any time. Continued use of the application following updates constitutes your acceptance of the updated terms."
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color(0xFF1E293B))
            }
            Text(
                "Privacy Policy",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF1E293B),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Last updated: June 22, 2026",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            PolicySection(
                title = "1. Information We Collect",
                content = "We collect your registration details (name, email, phone number, and city) along with GPS location coordinates when submitting a waste report. We also request camera and storage permissions to allow attaching incident photos."
            )
            PolicySection(
                title = "2. How We Use Your Data",
                content = "Your location and reports are shared with assigned supervisors to facilitate waste pickup. Real-time GPS coordinates are collected from supervisors in the background to calculate optimal routes via OSRM and render live tracker updates for citizens."
            )
            PolicySection(
                title = "3. Third-Party Libraries",
                content = "We integrate MapLibre SDK for street map visualizations and OSRM (Open Source Routing Machine) to calculate driving distances. These services do not receive or store your personal user profile details."
            )
            PolicySection(
                title = "4. Encryption & Security",
                content = "All communication between the Android application and our backend endpoints is encrypted in transit using TLS. User authentication tokens are stored securely in local device preferences."
            )
            PolicySection(
                title = "5. User Control & Data Deletion",
                content = "You can request the deletion of your account and related historical reports at any time by contacting our support desk. Report history may be retained in anonymized format for city analytics."
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun OpenSourceLicensesScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color(0xFF1E293B))
            }
            Text(
                "Open Source Licenses",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF1E293B),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "EcoCollect is built using the following open-source software libraries. We thank their developers for their contribution to the community.",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                lineHeight = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LicenseItem(
                name = "MapLibre Native Android SDK",
                license = "BSD 2-Clause",
                description = "High-performance vector map rendering engine used to draw live tracking pathways and pin locations."
            )
            LicenseItem(
                name = "Jetpack Compose",
                license = "Apache 2.0",
                description = "Android's modern toolkit for building premium, responsive, and native user interfaces."
            )
            LicenseItem(
                name = "Ktor Client",
                license = "Apache 2.0",
                description = "Asynchronous HTTP client for Kotlin used to execute requests and fetch real-time updates from our FastAPI servers."
            )
            LicenseItem(
                name = "FastAPI",
                license = "MIT License",
                description = "Modern, fast web framework for building APIs with Python on our central server."
            )
            LicenseItem(
                name = "SQLAlchemy",
                license = "MIT License",
                description = "SQL Toolkit and Object Relational Mapper for Python database synchronization."
            )
            LicenseItem(
                name = "Multiplatform Settings",
                license = "Apache 2.0",
                description = "A Kotlin Multiplatform library for saving simple key-value data, providing persistent cache for citizen credentials."
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PolicySection(title: String, content: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF16A34A) // brand green
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                fontSize = 13.sp,
                color = Color(0xFF475569),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun LicenseItem(name: String, license: String, description: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(license, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(description, fontSize = 13.sp, color = Color(0xFF475569), lineHeight = 18.sp)
        }
    }
}
