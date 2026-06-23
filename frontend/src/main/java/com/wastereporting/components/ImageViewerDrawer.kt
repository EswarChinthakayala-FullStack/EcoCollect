package com.wastereporting.components

import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wastereporting.network.ApiService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageViewerDrawer(
    urls: List<String>? = null,
    bitmaps: List<Bitmap>? = null,
    initialIndex: Int,
    title: String,
    description: String,
    status: String,
    date: String,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    var selectedIndex by remember { mutableStateOf(initialIndex) }
    
    val totalCount = urls?.size ?: bitmaps?.size ?: 0
    val currentUrl = urls?.getOrNull(selectedIndex) ?: ""
    val currentBitmap = bitmaps?.getOrNull(selectedIndex)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.fillMaxHeight(0.85f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = date,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Download/Save Button
                    IconButton(
                        onClick = {
                            if (urls != null && currentUrl.isNotEmpty()) {
                                try {
                                    val fullUrl = ApiService.getFullImageUrl(currentUrl)
                                    val request = DownloadManager.Request(Uri.parse(fullUrl)).apply {
                                        setTitle("EcoCollect Image")
                                        setDescription("Downloading image for $title")
                                        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                        setDestinationInExternalPublicDir(
                                            Environment.DIRECTORY_DOWNLOADS,
                                            "ecocollect_${System.currentTimeMillis()}.jpg"
                                        )
                                    }
                                    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                    downloadManager.enqueue(request)
                                    Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Failed to start download: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            } else if (bitmaps != null && currentBitmap != null) {
                                try {
                                    val filename = "ecocollect_${System.currentTimeMillis()}.jpg"
                                    var fos: java.io.OutputStream? = null
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                        val resolver = context.contentResolver
                                        val contentValues = android.content.ContentValues().apply {
                                            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
                                            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                                            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                                        }
                                        val imageUri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                                        if (imageUri != null) {
                                            fos = resolver.openOutputStream(imageUri)
                                        }
                                    } else {
                                        val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                                        val image = java.io.File(imagesDir, filename)
                                        fos = java.io.FileOutputStream(image)
                                    }
                                    fos?.use {
                                        currentBitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                                        Toast.makeText(context, "Image saved to Pictures", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Failed to save image: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download Image", tint = Color(0xFF475569))
                    }

                    // Close Button
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF475569))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Large Image Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF8FAFC)),
                contentAlignment = Alignment.Center
            ) {
                if (urls != null && currentUrl.isNotEmpty()) {
                    NetworkImage(
                        url = currentUrl,
                        contentDescription = "Zoomed Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else if (bitmaps != null && currentBitmap != null) {
                    Image(
                        bitmap = currentBitmap.asImageBitmap(),
                        contentDescription = "Local Zoomed Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // If multiple images: show list of small thumbnail previews below
            if (totalCount > 1) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (urls != null) {
                        itemsIndexed(urls) { index, url ->
                            val isSelected = index == selectedIndex
                            val borderModifier = if (isSelected) {
                                Modifier.border(2.5.dp, Color(0xFF16A34A), RoundedCornerShape(10.dp))
                            } else {
                                Modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .then(borderModifier)
                                    .clickable { selectedIndex = index }
                            ) {
                                NetworkImage(
                                    url = url,
                                    contentDescription = "Thumbnail $index",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    } else if (bitmaps != null) {
                        itemsIndexed(bitmaps) { index, bitmap ->
                            val isSelected = index == selectedIndex
                            val borderModifier = if (isSelected) {
                                Modifier.border(2.5.dp, Color(0xFF16A34A), RoundedCornerShape(10.dp))
                            } else {
                                Modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .then(borderModifier)
                                    .clickable { selectedIndex = index }
                            ) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Local Thumbnail $index",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Details/Metadata Card (category, description, status)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status: $status",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (status.lowercase()) {
                                "completed", "resolved" -> Color(0xFF16A34A)
                                "in_progress", "in progress", "assigned" -> Color(0xFF3B82F6)
                                else -> Color(0xFFEAB308)
                            }
                        )
                    }

                    if (description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Description:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = description,
                            fontSize = 13.sp,
                            color = Color(0xFF334155),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
