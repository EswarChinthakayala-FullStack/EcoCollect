package com.wastereporting.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.wastereporting.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun NetworkImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    loadingPlaceholder: @Composable () -> Unit = {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF16A34A), modifier = Modifier.size(24.dp))
        }
    },
    errorPlaceholder: @Composable () -> Unit = {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Warning, contentDescription = "Error loading image", tint = Color(0xFFEF4444))
        }
    }
) {
    var bitmap by remember(url) { mutableStateOf<Bitmap?>(null) }
    var isError by remember(url) { mutableStateOf(false) }

    LaunchedEffect(url) {
        if (url.isBlank()) {
            isError = true
            return@LaunchedEffect
        }
        val fullUrl = ApiService.getFullImageUrl(url)
        withContext(Dispatchers.IO) {
            try {
                val connection = URL(fullUrl).openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input: InputStream = connection.inputStream
                val bmp = BitmapFactory.decodeStream(input)
                if (bmp != null) {
                    bitmap = bmp
                } else {
                    isError = true
                }
            } catch (e: Exception) {
                isError = true
            }
        }
    }

    val bmp = bitmap
    if (bmp != null) {
        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else if (isError) {
        errorPlaceholder()
    } else {
        loadingPlaceholder()
    }
}
