package com.wastereporting.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wastereporting.components.AppButton
import com.wastereporting.components.AppCard
import com.wastereporting.components.rememberImagePicker
import com.wastereporting.network.ApiService
import com.wastereporting.network.IssueDraft
import kotlinx.coroutines.launch
import android.graphics.BitmapFactory

fun saveImageToGallery(context: android.content.Context, bytes: ByteArray) {
    try {
        val contentResolver = context.contentResolver
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, "EcoCollect_${System.currentTimeMillis()}.jpg")
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES)
                put(android.provider.MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }
        
        val uri = contentResolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(bytes)
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0)
                contentResolver.update(uri, contentValues, null, null)
            }
            android.widget.Toast.makeText(context, "Image saved to Gallery", android.widget.Toast.LENGTH_SHORT).show()
        } else {
            android.widget.Toast.makeText(context, "Failed to save image", android.widget.Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        android.widget.Toast.makeText(context, "Error saving image: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ReviewReportScreen(
    onBack: () -> Unit,
    onSubmit: () -> Unit,
    onEditLocation: () -> Unit = {},
    onEditCategory: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var additionalDetails by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var updateTrigger by remember { mutableStateOf(0) }
    var clickedImageIndex by remember { mutableStateOf<Int?>(null) }

    val imagePicker = rememberImagePicker { bytes ->
        if (bytes != null) {
            IssueDraft.imageBytes = bytes
            IssueDraft.imagesList = IssueDraft.imagesList + bytes
            updateTrigger++ // Force recomposition to update UI
        }
    }

    val allBitmaps = remember(IssueDraft.imagesList, IssueDraft.imageBytes, updateTrigger) {
        val list = IssueDraft.imagesList
        if (list.isNotEmpty()) {
            list.mapNotNull { bytes ->
                try {
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                } catch (e: Exception) {
                    null
                }
            }
        } else {
            IssueDraft.imageBytes?.let { bytes ->
                try {
                    listOfNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap())
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    "Review Report",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp)) // balance
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // Photo Preview Slider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.TopEnd
                ) {
                    if (allBitmaps.isNotEmpty()) {
                        val pagerState = rememberPagerState(pageCount = { allBitmaps.size })
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            Image(
                                bitmap = allBitmaps[page],
                                contentDescription = "Preview Image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { clickedImageIndex = page },
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Dot / Text page indicator
                        if (allBitmaps.size > 1) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(12.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${pagerState.currentPage + 1}/${allBitmaps.size}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Remove button
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .clickable {
                                    val currentIndex = pagerState.currentPage
                                    val mutableList = IssueDraft.imagesList.toMutableList()
                                    if (currentIndex in mutableList.indices) {
                                        mutableList.removeAt(currentIndex)
                                        IssueDraft.imagesList = mutableList
                                        IssueDraft.imageBytes = mutableList.firstOrNull()
                                        updateTrigger++
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove Photo", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Image, contentDescription = "Preview", tint = Color(0xFF94A3B8), modifier = Modifier.size(64.dp))
                        }
                    }

                    // Edit / Add Photo button
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.9f))
                            .clickable { imagePicker.launchGallery() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (allBitmaps.isEmpty()) "Add Photo" else "Add More",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Location
                        ReviewItem(
                            icon = Icons.Default.LocationOn,
                            title = "Location",
                            value = if (IssueDraft.address.isNotEmpty()) IssueDraft.address else "Tap Edit to set location",
                            onEdit = onEditLocation
                        )
                        HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))
                        
                        // Category
                        ReviewItem(
                            icon = Icons.Default.Category,
                            title = "Category",
                            value = if (IssueDraft.category.isNotEmpty()) IssueDraft.category else "Tap Edit to set category",
                            onEdit = onEditCategory
                        )
                        HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))
                        
                        // Additional Details
                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFF1F5F9)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Additional Details", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = additionalDetails,
                                    onValueChange = { additionalDetails = it },
                                    placeholder = { Text("Add any extra information here...", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                                    modifier = Modifier.fillMaxWidth().height(100.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color(0xFFE2E8F0),
                                        focusedBorderColor = Color(0xFF16A34A)
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
                }

                // Submit Button
                Button(
                    onClick = {
                        isSubmitting = true
                        errorMessage = null
                        coroutineScope.launch {
                            val result = ApiService.submitIssue(
                                category = if (IssueDraft.category.isNotEmpty()) IssueDraft.category else "Uncategorized",
                                description = additionalDetails,
                                latitude = if (IssueDraft.latitude != 0.0) IssueDraft.latitude else 12.9716,
                                longitude = if (IssueDraft.longitude != 0.0) IssueDraft.longitude else 77.5946,
                                address = if (IssueDraft.address.isNotEmpty()) IssueDraft.address else "Unknown Location",
                                imagesList = if (IssueDraft.imagesList.isNotEmpty()) IssueDraft.imagesList else listOfNotNull(IssueDraft.imageBytes)
                            )
                            isSubmitting = false
                            if (result.isSuccess) {
                                IssueDraft.clear()
                                onSubmit()
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Failed to submit"
                            }
                        }
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Check, contentDescription = "Submit", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Submit Report", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Image Viewer Drawer (ModalBottomSheet)
        if (clickedImageIndex != null) {
            val rawBitmaps = if (IssueDraft.imagesList.isNotEmpty()) {
                IssueDraft.imagesList.mapNotNull { bytes ->
                    android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
            } else {
                IssueDraft.imageBytes?.let { bytes ->
                    listOfNotNull(android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
                } ?: emptyList()
            }

            com.wastereporting.components.ImageViewerDrawer(
                bitmaps = rawBitmaps,
                initialIndex = clickedImageIndex!!,
                title = "Draft Photo - ${IssueDraft.category ?: "Uncategorized"}",
                description = additionalDetails.ifEmpty { "No description provided." },
                status = "Draft (Reviewing)",
                date = "Draft Report",
                onDismissRequest = { clickedImageIndex = null }
            )
        }
    }
}

@Composable
fun ReviewItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, onEdit: () -> Unit = {}) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFF1F5F9)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Text(value, fontSize = 14.sp, color = Color(0xFF64748B))
        }
        Text("Edit", color = Color(0xFF16A34A), fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onEdit() })
    }
}
