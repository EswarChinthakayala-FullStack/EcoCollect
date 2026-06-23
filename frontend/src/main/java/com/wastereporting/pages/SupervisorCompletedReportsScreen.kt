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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wastereporting.components.AppCard
import com.wastereporting.network.ApiService
import com.wastereporting.network.IssueReport
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorCompletedReportsScreen(
    onNavigateToDashboard: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToReportDetails: (Int) -> Unit,
    onBack: () -> Unit
) {
    var issues by remember { mutableStateOf<List<IssueReport>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Search and Filter States
    var searchQuery by remember { mutableStateOf("") }
    
    var tempCategoryFilter by remember { mutableStateOf("All") }
    var tempSortBy by remember { mutableStateOf("Newest First") }
    
    var appliedCategoryFilter by remember { mutableStateOf("All") }
    var appliedSortBy by remember { mutableStateOf("Newest First") }

    var showFilterDrawer by remember { mutableStateOf(false) }

    val fetchIssues = {
        isLoading = true
        coroutineScope.launch {
            val result = ApiService.getSupervisorReports("Completed")
            isLoading = false
            if (result.isSuccess) {
                issues = result.getOrNull() ?: emptyList()
            } else {
                errorMessage = result.exceptionOrNull()?.message ?: "Failed to load reports"
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchIssues()
    }

    // Filtered and Sorted list calculation
    val filteredIssues = remember(issues, searchQuery, appliedCategoryFilter, appliedSortBy) {
        var list = issues.filter { issue ->
            val matchesSearch = issue.title.orEmpty().contains(searchQuery, ignoreCase = true) ||
                    issue.description.orEmpty().contains(searchQuery, ignoreCase = true) ||
                    issue.location.orEmpty().contains(searchQuery, ignoreCase = true) ||
                    issue.address.orEmpty().contains(searchQuery, ignoreCase = true) ||
                    issue.category.contains(searchQuery, ignoreCase = true)
            
            val matchesCategory = appliedCategoryFilter == "All" || 
                    issue.category.lowercase() == appliedCategoryFilter.lowercase()
            
            matchesSearch && matchesCategory
        }
        
        list = if (appliedSortBy == "Newest First") {
            list.sortedByDescending { it.id }
        } else {
            list.sortedBy { it.id }
        }
        
        list
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                SupervisorBottomNav(
                    currentTab = "History",
                    onTabSelected = { tab ->
                        when (tab) {
                            "Reports" -> onNavigateToDashboard()
                            "Profile" -> onNavigateToProfile()
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC))
                    .padding(innerPadding)
            ) {
                // Top Bar
                Box(
                    modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color(0xFF1E293B))
                    }
                    Text(
                        "Completed Reports",
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

                    // Filter Button
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (appliedCategoryFilter != "All" || appliedSortBy != "Newest First") {
                                    Color(0xFFE8F5E9)
                                } else {
                                    Color.White
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = if (appliedCategoryFilter != "All" || appliedSortBy != "Newest First") {
                                    Color(0xFF16A34A)
                                } else {
                                    Color(0xFFE2E8F0)
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                tempCategoryFilter = appliedCategoryFilter
                                tempSortBy = appliedSortBy
                                showFilterDrawer = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val activeFilterCount = (if (appliedCategoryFilter != "All") 1 else 0) +
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
                if (appliedCategoryFilter != "All" || appliedSortBy != "Newest First") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Filters:", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                        
                        if (appliedCategoryFilter != "All") {
                            LocalAppliedFilterTag(appliedCategoryFilter) { appliedCategoryFilter = "All" }
                        }
                        if (appliedSortBy != "Newest First") {
                            LocalAppliedFilterTag("Sorted") { appliedSortBy = "Newest First" }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                ) {
                    item {
                        // Stats Summary based on filtered issues count
                        AppCard(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                            Row(
                                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("This month", color = Color(0xFF64748B), fontSize = 14.sp)
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text("${filteredIssues.size} ", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color(0xFF16A34A))
                                        Text("Completed", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF16A34A), modifier = Modifier.padding(bottom = 2.dp))
                                    }
                                }
                                Icon(Icons.Default.CheckCircle, contentDescription = "Completed", tint = Color(0xFF16A34A), modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                    if (isLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFF16A34A))
                            }
                        }
                    } else if (errorMessage != null) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    } else if (filteredIssues.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No matching completed reports.", color = Color(0xFF64748B), fontSize = 16.sp)
                            }
                        }
                    } else {
                        items(filteredIssues.size) { index ->
                            val issue = filteredIssues[index]
                            val statusBg = Color(0xFFDCFCE7)
                            val statusColor = Color(0xFF16A34A)
                            ReportCard(
                                title = (issue.title ?: "").ifEmpty { "Issue ${issue.category}" },
                                category = "RPT-848${issue.id}",
                                address = issue.location ?: "Unknown Location",
                                time = issue.resolved_at?.take(10) ?: issue.created_at?.take(10) ?: "Unknown Date",
                                distance = "",
                                status = "Completed",
                                statusColor = statusColor,
                                statusBg = statusBg,
                                imageUrl = issue.completion_image_url ?: issue.after_image ?: issue.image_url ?: issue.before_image,
                                onClick = { onNavigateToReportDetails(issue.id) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
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
                                    LocalCustomFilterChip("All", tempCategoryFilter == "All") { tempCategoryFilter = "All" }
                                    LocalCustomFilterChip("Overflowing Bin", tempCategoryFilter == "Overflowing Bin") { tempCategoryFilter = "Overflowing Bin" }
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    LocalCustomFilterChip("Illegal Dumping", tempCategoryFilter == "Illegal Dumping") { tempCategoryFilter = "Illegal Dumping" }
                                    LocalCustomFilterChip("Recycling Issue", tempCategoryFilter == "Recycling Issue") { tempCategoryFilter = "Recycling Issue" }
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    LocalCustomFilterChip("Green Waste", tempCategoryFilter == "Green Waste") { tempCategoryFilter = "Green Waste" }
                                    LocalCustomFilterChip("Bulky Items", tempCategoryFilter == "Bulky Items") { tempCategoryFilter = "Bulky Items" }
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    LocalCustomFilterChip("Other", tempCategoryFilter == "Other") { tempCategoryFilter = "Other" }
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
                                LocalCustomFilterChip("Newest First", tempSortBy == "Newest First") { tempSortBy = "Newest First" }
                                LocalCustomFilterChip("Oldest First", tempSortBy == "Oldest First") { tempSortBy = "Oldest First" }
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
                                    tempCategoryFilter = "All"
                                    tempSortBy = "Newest First"
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
private fun LocalCustomFilterChip(
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
private fun LocalAppliedFilterTag(
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
