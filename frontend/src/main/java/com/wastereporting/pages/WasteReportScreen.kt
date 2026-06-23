package com.wastereporting.pages

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.wastereporting.components.AppButton
import com.wastereporting.components.rememberImagePicker
import com.wastereporting.network.IssueDraft

@Composable
fun WasteReportScreen(onImageSelected: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedImages by remember { mutableStateOf<List<ByteArray>>(emptyList()) }

    val imagePicker = rememberImagePicker { bytes ->
        if (bytes != null) {
            selectedImages = selectedImages + bytes
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            imagePicker.launchCamera()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color(0xFF1E293B))
            }
            Text(
                "Report Issue",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF1E293B),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp)) // balance
        }

        Text("What's the issue?", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Take a photo of the waste or issue to help us locate and resolve it quickly.", color = Color(0xFF64748B), fontSize = 14.sp)
        
        Spacer(modifier = Modifier.height(24.dp))

        // Camera Placeholder / Add photo box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (selectedImages.isEmpty()) 280.dp else 140.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
                .clickable {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        imagePicker.launchCamera()
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Camera", tint = Color(0xFF94A3B8), modifier = Modifier.size(if (selectedImages.isEmpty()) 48.dp else 36.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(if (selectedImages.isEmpty()) "Tap to take photo" else "Add another photo", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                if (selectedImages.isEmpty()) {
                    Text("or upload from gallery", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Selected Images list
        if (selectedImages.isNotEmpty()) {
            Text("Selected Photos (${selectedImages.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().height(100.dp)
            ) {
                items(selectedImages.size) { index ->
                    val bytes = selectedImages[index]
                    val bitmap = remember(bytes) {
                        android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    }
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Selected image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        // Remove button
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .clickable {
                                    selectedImages = selectedImages.toMutableList().apply { removeAt(index) }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        // Gallery Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, Color(0xFF16A34A), RoundedCornerShape(12.dp))
                .clickable { imagePicker.launchGallery() }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Image, contentDescription = "Gallery", tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Choose from Gallery", color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Continue Button
        val buttonText = if (selectedImages.isNotEmpty()) "Continue with ${selectedImages.size} Photo(s)" else "Continue Without Photo"
        AppButton(
            text = buttonText,
            onClick = {
                if (selectedImages.isNotEmpty()) {
                    IssueDraft.imagesList = selectedImages
                    IssueDraft.imageBytes = selectedImages.firstOrNull()
                } else {
                    IssueDraft.clear()
                }
                onImageSelected()
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp)) // Bottom padding
    }
}
