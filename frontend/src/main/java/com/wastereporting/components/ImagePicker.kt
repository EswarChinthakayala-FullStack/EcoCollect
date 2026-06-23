package com.wastereporting.components

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class ImagePickerLauncher(
    private val onPickGallery: () -> Unit,
    private val onPickCamera: () -> Unit
) {
    fun launchCamera() {
        onPickCamera()
    }
    fun launchGallery() {
        onPickGallery()
    }
}

@Composable
fun rememberImagePicker(onImagePicked: (ByteArray?) -> Unit): ImagePickerLauncher {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val bytes = readBytesFromUri(context, uri)
                onImagePicked(bytes)
            }
        } else {
            onImagePicked(null)
        }
    }

    // Camera Launcher (returns thumbnail Bitmap)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            coroutineScope.launch {
                val bytes = bitmapToByteArray(bitmap)
                onImagePicked(bytes)
            }
        } else {
            onImagePicked(null)
        }
    }

    return remember {
        ImagePickerLauncher(
            onPickGallery = { galleryLauncher.launch("image/*") },
            onPickCamera = { cameraLauncher.launch() }
        )
    }
}

private suspend fun readBytesFromUri(context: Context, uri: Uri): ByteArray? = withContext(Dispatchers.IO) {
    try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.readBytes()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private suspend fun bitmapToByteArray(bitmap: Bitmap): ByteArray = withContext(Dispatchers.IO) {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
    stream.toByteArray()
}
