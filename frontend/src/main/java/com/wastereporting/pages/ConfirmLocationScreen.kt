package com.wastereporting.pages

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.wastereporting.components.MapLibreView
import com.wastereporting.network.IssueDraft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

import org.maplibre.android.geometry.LatLng
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.maps.MapLibreMap
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmLocationScreen(onBack: () -> Unit, onConfirm: () -> Unit) {
    val context = LocalContext.current
    var mapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    var currentLat by remember { mutableStateOf(12.9716) }
    var currentLng by remember { mutableStateOf(77.5946) }

    var displayAddress by remember { mutableStateOf("Fetching address...") }
    var displayCity by remember { mutableStateOf("Please wait...") }

    var searchQuery by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<Address>>(emptyList()) }
    var showSuggestions by remember { mutableStateOf(false) }

    // Reverse geocode when map coordinates change
    LaunchedEffect(currentLat, currentLng) {
        val addressResult = withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val list = geocoder.getFromLocation(currentLat, currentLng, 1)
                if (!list.isNullOrEmpty()) list[0] else null
            } catch (e: Exception) {
                null
            }
        }
        if (addressResult != null) {
            val thoroughfare = addressResult.thoroughfare ?: addressResult.subThoroughfare ?: addressResult.featureName ?: "Unnamed Road"
            val locality = addressResult.locality ?: addressResult.subLocality ?: addressResult.subAdminArea ?: "Bangalore"
            val adminArea = addressResult.adminArea ?: ""
            displayAddress = thoroughfare
            displayCity = if (adminArea.isNotEmpty()) "$locality, $adminArea" else locality
        } else {
            displayAddress = "Lat: %.4f".format(currentLat)
            displayCity = "Lng: %.4f".format(currentLng)
        }
    }

    // Debounced Search Suggestions Geocoding
    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank() || searchQuery.length < 3) {
            suggestions = emptyList()
            showSuggestions = false
            return@LaunchedEffect
        }
        delay(500) // 500ms debounce
        val list = withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                geocoder.getFromLocationName(searchQuery, 4) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
        suggestions = list
        showSuggestions = list.isNotEmpty()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        MapLibreView(
            modifier = Modifier.fillMaxSize()
        ) { map ->
            mapInstance = map
            map.setStyle("https://tiles.openfreemap.org/styles/liberty")
            
            // Set initial camera to current location if permission is granted, otherwise Bangalore
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            
            var initialLatLng = LatLng(12.9716, 77.5946)
            if (hasPermission) {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val providers = locationManager.getProviders(true)
                var bestLocation: android.location.Location? = null
                for (provider in providers) {
                    val loc = locationManager.getLastKnownLocation(provider) ?: continue
                    val currentBest = bestLocation
                    if (currentBest == null || loc.accuracy < currentBest.accuracy) {
                        bestLocation = loc
                    }
                }
                val finalLoc = bestLocation
                if (finalLoc != null) {
                    initialLatLng = LatLng(finalLoc.latitude, finalLoc.longitude)
                }
            }
            
            currentLat = initialLatLng.latitude
            currentLng = initialLatLng.longitude
            
            map.cameraPosition = CameraPosition.Builder()
                .target(initialLatLng)
                .zoom(16.0)
                .build()
            
            // Listen to camera moves to track coordinates
            map.addOnCameraMoveListener {
                val target = map.cameraPosition?.target
                if (target != null) {
                    currentLat = target.latitude
                    currentLng = target.longitude
                }
            }
        }

        // Center Pin with tooltip
        Box(
            modifier = Modifier.align(Alignment.Center).offset(y = (-24).dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B).copy(alpha = 0.9f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Move map to adjust", color = Color.White, fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Icon(Icons.Default.LocationOn, contentDescription = "Pin", tint = Color(0xFFEF4444), modifier = Modifier.size(48.dp))
            }
        }

        // Location button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 240.dp) // Above bottom card
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable {
                    // Try to locate user live
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    
                    var targetLatLng = LatLng(12.9716, 77.5946)
                    if (hasPermission) {
                        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        val providers = locationManager.getProviders(true)
                        var bestLocation: android.location.Location? = null
                        for (provider in providers) {
                            val loc = locationManager.getLastKnownLocation(provider) ?: continue
                            val currentBest = bestLocation
                            if (currentBest == null || loc.accuracy < currentBest.accuracy) {
                                bestLocation = loc
                            }
                        }
                        val finalLoc = bestLocation
                        if (finalLoc != null) {
                            targetLatLng = LatLng(finalLoc.latitude, finalLoc.longitude)
                        }
                    }
                    
                    mapInstance?.animateCamera(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder()
                                .target(targetLatLng)
                                .zoom(16.0)
                                .build()
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.NearMe, contentDescription = "My Location", tint = Color(0xFF3B82F6))
        }

        // Top UI
        Column(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter)) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = Color(0xFF1E293B))
                }
                Text(
                    "Confirm Location",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp)) // balance
            }

            // Search Bar
            Box(modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search address...", color = Color(0xFF94A3B8)) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedBorderColor = Color(0xFFE2E8F0),
                        unfocusedContainerColor = Color(0xFFF8FAFC),
                        focusedContainerColor = Color.White
                    ),
                    singleLine = true
                )
            }

            // Suggestions List
            if (showSuggestions && suggestions.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column {
                        suggestions.forEachIndexed { index, address ->
                            val fullAddressName = address.getAddressLine(0) ?: "Unknown Location"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val latLng = LatLng(address.latitude, address.longitude)
                                        mapInstance?.animateCamera(
                                            CameraUpdateFactory.newCameraPosition(
                                                CameraPosition.Builder()
                                                    .target(latLng)
                                                    .zoom(16.0)
                                                    .build()
                                            )
                                        )
                                        showSuggestions = false
                                        searchQuery = "" // Reset or set to fullAddressName
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(fullAddressName, fontSize = 14.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.Medium)
                            }
                            if (index < suggestions.size - 1) {
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                            }
                        }
                    }
                }
            }
        }

        // Bottom Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
                .padding(24.dp)
        ) {
            Column {
                Box(modifier = Modifier.align(Alignment.CenterHorizontally).width(40.dp).height(4.dp).clip(CircleShape).background(Color(0xFFE2E8F0)))
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF0FDF4)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(displayAddress, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                        Text(displayCity, fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        IssueDraft.address = "$displayAddress, $displayCity"
                        IssueDraft.latitude = currentLat
                        IssueDraft.longitude = currentLng
                        onConfirm()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirm Location", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}
