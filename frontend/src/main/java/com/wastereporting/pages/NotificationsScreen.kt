package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wastereporting.network.ApiService
import com.wastereporting.network.Notification
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf("All") } // "All" or "Unread"
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val fetchNotifications = {
        isLoading = true
        coroutineScope.launch {
            val res = ApiService.getNotifications()
            if (res.isSuccess) {
                // Sort by ID descending to show newest first
                notifications = (res.getOrNull() ?: emptyList()).sortedByDescending { it.id }
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        fetchNotifications()
    }

    val unreadCount = remember(notifications) {
        notifications.count { it.read_status == 0 }
    }

    val displayedNotifications = remember(notifications, selectedTab) {
        if (selectedTab == "Unread") {
            notifications.filter { it.read_status == 0 }
        } else {
            notifications
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                color = Color.White,
                tonalElevation = 4.dp,
                shadowElevation = 4.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color(0xFF1E293B), modifier = Modifier.size(28.dp))
                        }
                        Text(
                            text = "Notifications",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.weight(1f)
                        )
                        if (unreadCount > 0) {
                            TextButton(
                                onClick = {
                                    coroutineScope.launch {
                                        val unreadNotifications = notifications.filter { it.read_status == 0 }
                                        // Optimistically update UI
                                        notifications = notifications.map { it.copy(read_status = 1) }
                                        
                                        var successCount = 0
                                        unreadNotifications.forEach { noti ->
                                            val result = ApiService.markNotificationRead(noti.id)
                                            if (result.isSuccess) successCount++
                                        }
                                        
                                        if (successCount < unreadNotifications.size) {
                                            snackbarHostState.showSnackbar("Failed to mark all as read. Synchronizing...")
                                            fetchNotifications()
                                        } else {
                                            snackbarHostState.showSnackbar("All notifications marked as read")
                                        }
                                    }
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "Mark all read",
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        "Mark all read",
                                        color = Color(0xFF16A34A),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // Tab selector for All vs Unread
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        listOf("All", "Unread").forEach { tab ->
                            val isSelected = selectedTab == tab
                            val count = if (tab == "Unread") unreadCount else notifications.size
                            Column(
                                modifier = Modifier
                                    .clickable { selectedTab = tab }
                                    .padding(end = 24.dp, bottom = 12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = tab,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF16A34A) else Color(0xFF64748B)
                                    )
                                    if (count > 0) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) Color(0xFFDCFCE7) else Color(0xFFF1F5F9))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = count.toString(),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color(0xFF15803D) else Color(0xFF475569)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .height(3.dp)
                                        .width(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color(0xFF16A34A) else Color.Transparent)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF16A34A))
                }
            } else if (displayedNotifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = Color(0xFF94A3B8)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = if (selectedTab == "Unread") "No unread notifications" else "All caught up!",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedTab == "Unread") "You have read all of your notifications." else "We'll notify you when your report status changes.",
                            color = Color(0xFF64748B),
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayedNotifications, key = { it.id }) { notification ->
                        val isRead = notification.read_status == 1

                        val isSuccess = notification.title.contains("Success", ignoreCase = true) || 
                                        notification.title.contains("Completed", ignoreCase = true) ||
                                        notification.title.contains("Resolved", ignoreCase = true) ||
                                        notification.title.contains("Welcome", ignoreCase = true)
                        
                        val isUpdate = notification.title.contains("Dispatched", ignoreCase = true) ||
                                       notification.title.contains("Assigned", ignoreCase = true) ||
                                       notification.title.contains("Progress", ignoreCase = true)

                        val icon = when {
                            isSuccess -> Icons.Default.CheckCircle
                            isUpdate -> Icons.Default.Info
                            else -> Icons.Default.Notifications
                        }

                        val iconColor = when {
                            isRead -> Color(0xFF94A3B8)
                            isSuccess -> Color(0xFF16A34A)
                            isUpdate -> Color(0xFF3B82F6)
                            else -> Color(0xFFEAB308)
                        }

                        val iconBg = when {
                            isRead -> Color(0xFFF1F5F9)
                            isSuccess -> Color(0xFFDCFCE7)
                            isUpdate -> Color(0xFFDBEAFE)
                            else -> Color(0xFFFEF9C3)
                        }

                        val cardBg = if (isRead) Color.White else Color(0xFFF0FDF4)
                        val borderStroke = if (isRead) {
                            Modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                        } else {
                            Modifier.border(1.5.dp, Color(0xFF16A34A).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(borderStroke)
                                .clickable {
                                    if (!isRead) {
                                        // Instantly update local state
                                        notifications = notifications.map {
                                            if (it.id == notification.id) it.copy(read_status = 1) else it
                                        }
                                        coroutineScope.launch {
                                            val result = ApiService.markNotificationRead(notification.id)
                                            if (result.isFailure) {
                                                snackbarHostState.showSnackbar("Failed to update status on server")
                                                fetchNotifications()
                                            }
                                        }
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isRead) 0.dp else 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(iconBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = iconColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = notification.title,
                                            fontWeight = if (isRead) FontWeight.SemiBold else FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF1E293B),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (!isRead) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(start = 8.dp)
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF16A34A))
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    Text(
                                        text = notification.message,
                                        color = Color(0xFF475569),
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    )
                                    
                                    Spacer(modifier = Modifier.height(10.dp))
                                    
                                    Text(
                                        text = ApiService.formatIsoDateTimeToIndian(notification.created_at),
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
