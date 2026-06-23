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
import com.wastereporting.components.AppCard

@Composable
fun SupervisorSettingsScreen(
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    var pushNotifications by remember { mutableStateOf(true) }
    var locationAlerts by remember { mutableStateOf(true) }
    var darkMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Top Bar
        Box(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color(0xFF1E293B))
            }
            Text(
                "Settings",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF1E293B),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Text("Notifications", fontSize = 14.sp, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 12.dp, start = 8.dp))

        AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingToggleRow(
                    icon = Icons.Default.NotificationsActive,
                    title = "Push Notifications",
                    subtitle = "Report updates & alerts",
                    checked = pushNotifications,
                    onCheckedChange = { pushNotifications = it }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))
                SettingToggleRow(
                    icon = Icons.Default.LocationOn,
                    title = "Location Alerts",
                    subtitle = "Nearby collection trucks",
                    checked = locationAlerts,
                    onCheckedChange = { locationAlerts = it }
                )
            }
        }

        Text("App Preferences", fontSize = 14.sp, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 12.dp, start = 8.dp))

        AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingToggleRow(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Mode",
                    subtitle = "",
                    checked = darkMode,
                    onCheckedChange = { darkMode = it }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF1F5F9))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                        }
                        Text("Privacy Settings", color = Color(0xFF1E293B), fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 16.dp))
                    }
                    Text("Manage", color = Color(0xFF94A3B8), fontSize = 14.sp, modifier = Modifier.clickable { /* TODO */ })
                }
            }
        }
    }
}

@Composable
fun SettingToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(title, color = Color(0xFF1E293B), fontWeight = FontWeight.Medium)
                if (subtitle.isNotEmpty()) {
                    Text(subtitle, color = Color(0xFF64748B), fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF10B981))
        )
    }
}
