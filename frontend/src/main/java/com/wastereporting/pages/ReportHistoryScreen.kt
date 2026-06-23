package com.wastereporting.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wastereporting.components.AppCard
import com.wastereporting.network.ApiService
import com.wastereporting.network.IssueReport
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportHistoryScreen(
    onBack: () -> Unit,
    onNavigateToReportDetails: (Int) -> Unit = {}
) {
    var issues by remember { mutableStateOf<List<IssueReport>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Search and Filter States
    var searchQuery by remember { mutableStateOf("") }
    
    var tempStatusFilter by remember { mutableStateOf("All") }
    var tempCategoryFilter by remember { mutableStateOf("All") }
    var tempSortBy by remember { mutableStateOf("Newest First") }
    
    var appliedStatusFilter by remember { mutableStateOf("All") }
    var appliedCategoryFilter by remember { mutableStateOf("All") }
    var appliedSortBy by remember { mutableStateOf("Newest First") }

    var showFilterDrawer by remember { mutableStateOf(false) }

    val fetchIssues = {
        isLoading = true
        coroutineScope.launch {
            val result = ApiService.getIssues()
            isLoading = false
            if (result.isSuccess) {
                issues = result.getOrNull() ?: emptyList()
            } else {
                errorMessage = result.exceptionOrNull()?.message ?: "Failed to load history"
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchIssues()
    }

    // Filtered and Sorted list calculation
    val filteredIssues = remember(issues, searchQuery, appliedStatusFilter, appliedCategoryFilter, appliedSortBy) {
        var list = issues.filter { issue ->
            val matchesSearch = issue.title.orEmpty().contains(searchQuery, ignoreCase = true) ||
                    issue.description.orEmpty().contains(searchQuery, ignoreCase = true) ||
                    issue.location.orEmpty().contains(searchQuery, ignoreCase = true) ||
                    issue.address.orEmpty().contains(searchQuery, ignoreCase = true) ||
                    issue.category.contains(searchQuery, ignoreCase = true)
            
            val matchesStatus = appliedStatusFilter == "All" || 
                    issue.status.replace("_", " ").lowercase() == appliedStatusFilter.lowercase()
            
            val matchesCategory = appliedCategoryFilter == "All" || 
                    issue.category.lowercase() == appliedCategoryFilter.lowercase()
            
            matchesSearch && matchesStatus && matchesCategory
        }
        
        list = if (appliedSortBy == "Newest First") {
            list.sortedByDescending { it.id }
        } else {
            list.sortedBy { it.id }
        }
        
        list
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color(0xFF1E293B), modifier = Modifier.size(28.dp))
                }
                Text(
                    "My Reports",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.align(Alignment.Center)
                )
                IconButton(
                    onClick = { fetchIssues() },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color(0xFF1E293B))
                }
            }

            // Search Bar & Filter Button Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search reports...", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF64748B)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF16A34A),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                // Direct Filter Drawer Button
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (appliedStatusFilter != "All" || appliedCategoryFilter != "All" || appliedSortBy != "Newest First") {
                                Color(0xFFE8F5E9)
                            } else {
                                Color.White
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = if (appliedStatusFilter != "All" || appliedCategoryFilter != "All" || appliedSortBy != "Newest First") {
                                Color(0xFF16A34A)
                            } else {
                                Color(0xFFE2E8F0)
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            tempStatusFilter = appliedStatusFilter
                            tempCategoryFilter = appliedCategoryFilter
                            tempSortBy = appliedSortBy
                            showFilterDrawer = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val activeFilterCount = (if (appliedStatusFilter != "All") 1 else 0) + 
                                            (if (appliedCategoryFilter != "All") 1 else 0) +
                                            (if (appliedSortBy != "Newest First") 1 else 0)
                    
                    if (activeFilterCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = Color(0xFF16A34A),
                                    contentColor = Color.White
                                ) {
                                    Text(activeFilterCount.toString(), fontSize = 10.sp)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = Color(0xFF16A34A)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = Color(0xFF64748B)
                        )
                    }
                }
            }

            // Applied Filters Summary Indicators Row
            if (appliedStatusFilter != "All" || appliedCategoryFilter != "All" || appliedSortBy != "Newest First") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Filters:", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    
                    if (appliedStatusFilter != "All") {
                        AppliedFilterTag(appliedStatusFilter) { appliedStatusFilter = "All" }
                    }
                    if (appliedCategoryFilter != "All") {
                        AppliedFilterTag(appliedCategoryFilter) { appliedCategoryFilter = "All" }
                    }
                    if (appliedSortBy != "Newest First") {
                        AppliedFilterTag("Sorted") { appliedSortBy = "Newest First" }
                    }
                }
            }

            // Main Content Area
            if (isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF16A34A))
                }
            } else if (errorMessage != null) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            } else if (filteredIssues.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No reports match filters.",
                            color = Color(0xFF64748B),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Try clearing some filters or searching for something else.",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                ) {
                    items(filteredIssues) { issue ->
                        val statusClean = issue.status.lowercase()
                        val statusColor = when (statusClean) {
                            "completed", "resolved" -> Color(0xFF16A34A)
                            "in_progress", "in progress", "assigned" -> Color(0xFF3B82F6)
                            else -> Color(0xFFE29A0B)
                        }
                        val statusBg = when (statusClean) {
                            "completed", "resolved" -> Color(0xFFDCFCE7)
                            "in_progress", "in progress", "assigned" -> Color(0xFFDBEAFE)
                            else -> Color(0xFFFEF3C7)
                        }
                        val statusIcon = when (statusClean) {
                            "completed", "resolved" -> Icons.Default.CheckCircle
                            "in_progress", "in progress", "assigned" -> Icons.Default.Schedule
                            else -> Icons.Default.Warning
                        }

                        AppCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            onClick = { onNavigateToReportDetails(issue.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(statusBg, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(statusIcon, contentDescription = null, tint = statusColor)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        issue.title.orEmpty().ifEmpty { issue.category },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF1E293B)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        issue.address ?: issue.location ?: "Unknown Location",
                                        fontSize = 14.sp,
                                        color = Color(0xFF64748B),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        ApiService.formatIsoDateTimeToIndian(issue.created_at),
                                        fontSize = 12.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    issue.status.uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = statusColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Side-Drawer Slide-in Overlay
        if (showFilterDrawer) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = true, onClick = { showFilterDrawer = false })
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(300.dp)
                        .align(Alignment.CenterEnd)
                        .clickable(enabled = false, onClick = {}),
                    color = Color.White,
                    shadowElevation = 16.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        // Drawer Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Filters",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color(0xFF1E293B)
                            )
                            IconButton(onClick = { showFilterDrawer = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Scrollable Content
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Status Filter
                            Text(
                                text = "STATUS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                listOf("All", "Pending", "In Progress", "Completed").forEach { status ->
                                    CustomFilterChip(status, tempStatusFilter == status) { tempStatusFilter = status }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Category Filter
                            Text(
                                text = "CATEGORY",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    CustomFilterChip("All", tempCategoryFilter == "All") { tempCategoryFilter = "All" }
                                    CustomFilterChip("Overflowing Bin", tempCategoryFilter == "Overflowing Bin") { tempCategoryFilter = "Overflowing Bin" }
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    CustomFilterChip("Illegal Dumping", tempCategoryFilter == "Illegal Dumping") { tempCategoryFilter = "Illegal Dumping" }
                                    CustomFilterChip("Recycling Issue", tempCategoryFilter == "Recycling Issue") { tempCategoryFilter = "Recycling Issue" }
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    CustomFilterChip("Green Waste", tempCategoryFilter == "Green Waste") { tempCategoryFilter = "Green Waste" }
                                    CustomFilterChip("Bulky Items", tempCategoryFilter == "Bulky Items") { tempCategoryFilter = "Bulky Items" }
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    CustomFilterChip("Other", tempCategoryFilter == "Other") { tempCategoryFilter = "Other" }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Sort Order
                            Text(
                                text = "SORT BY",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                CustomFilterChip("Newest First", tempSortBy == "Newest First") { tempSortBy = "Newest First" }
                                CustomFilterChip("Oldest First", tempSortBy == "Oldest First") { tempSortBy = "Oldest First" }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // Buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    tempStatusFilter = "All"
                                    tempCategoryFilter = "All"
                                    tempSortBy = "Newest First"
                                    appliedStatusFilter = "All"
                                    appliedCategoryFilter = "All"
                                    appliedSortBy = "Newest First"
                                    showFilterDrawer = false
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1E293B))
                            ) {
                                Text("Reset", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    appliedStatusFilter = tempStatusFilter
                                    appliedCategoryFilter = tempCategoryFilter
                                    appliedSortBy = tempSortBy
                                    showFilterDrawer = false
                                },
                                modifier = Modifier.weight(1.5f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                            ) {
                                Text("Apply", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color(0xFFE8F5E9) else Color(0xFFF1F5F9))
            .border(
                width = 1.dp,
                color = if (selected) Color(0xFF16A34A) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color(0xFF16A34A) else Color(0xFF64748B)
        )
    }
}

@Composable
fun AppliedFilterTag(
    text: String,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE2E8F0))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text, fontSize = 11.sp, color = Color(0xFF334155), fontWeight = FontWeight.Medium)
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color(0xFF64748B),
                modifier = Modifier
                    .size(12.dp)
                    .clickable { onRemove() }
            )
        }
    }
}
