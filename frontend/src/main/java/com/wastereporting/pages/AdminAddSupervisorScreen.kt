package com.wastereporting.pages

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import com.wastereporting.network.ApiService
import com.wastereporting.network.SupervisorRegisterRequest
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wastereporting.components.AppCard

suspend fun resolveAreaNameToCoordinates(context: android.content.Context, areaName: String): Pair<Double, Double>? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    try {
        val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
        val addresses = geocoder.getFromLocationName(areaName, 1)
        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            Pair(address.latitude, address.longitude)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

suspend fun fetchCurrentLocationCoordinates(context: android.content.Context): Triple<String, Double, Double>? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    try {
        val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
        if (androidx.core.app.ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return@withContext null
        }

        val providers = locationManager.getProviders(true)
        var bestLocation: android.location.Location? = null
        for (provider in providers) {
            val loc = locationManager.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                bestLocation = loc
            }
        }

        if (bestLocation != null) {
            val lat = bestLocation.latitude
            val lng = bestLocation.longitude
            val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            val name = if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val locality = address.locality ?: address.subLocality ?: address.subAdminArea ?: ""
                val thoroughfare = address.thoroughfare ?: address.subThoroughfare ?: ""
                if (thoroughfare.isNotEmpty() && locality.isNotEmpty()) "$thoroughfare, $locality"
                else if (locality.isNotEmpty()) locality
                else "Downtown District"
            } else {
                "Downtown District"
            }
            Triple(name, lat, lng)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

@Composable
fun AdminAddSupervisorScreen(
    onBack: () -> Unit,
    onSupervisorAdded: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("SUP-${(100..999).random()}") }
    var password by remember { mutableStateOf("") }
    var assignedArea by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var coverageRadius by remember { mutableStateOf("10") }

    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scope.launch {
                val details = fetchCurrentLocationCoordinates(context)
                if (details != null) {
                    assignedArea = details.first
                    latitude = details.second.toString()
                    longitude = details.third.toString()
                    errorMessage = null
                } else {
                    errorMessage = "Failed to detect live location. Please ensure location services are enabled."
                }
            }
        } else {
            errorMessage = "Location permission is required to detect coordinates."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(top = 16.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.padding(end = 8.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
                }
                Text("Add Supervisor", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            }
        }
        
        HorizontalDivider(color = Color(0xFFE2E8F0))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Profile Photo Upload Card
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                            .border(2.dp, Color(0xFFE2E8F0), CircleShape)
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = "Upload", tint = Color(0xFF94A3B8), modifier = Modifier.size(32.dp))
                            Text("Upload", fontSize = 12.sp, color = Color(0xFF94A3B8), modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                    Text("Profile Photo", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B), modifier = Modifier.padding(top = 16.dp))
                    Text("JPG, PNG or GIF (Max 2MB)", fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Personal Information
            Text("Personal Information", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), modifier = Modifier.padding(bottom = 12.dp))
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Full Name *") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email Address *") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Employment Details
            Text("Employment Details", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), modifier = Modifier.padding(bottom = 12.dp))
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = employeeId,
                        onValueChange = { employeeId = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Employee ID *") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = assignedArea,
                            onValueChange = { assignedArea = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Assigned Locality Name *") },
                            placeholder = { Text("e.g. Connaught Place, Delhi") },
                            leadingIcon = { Icon(Icons.Default.Map, contentDescription = null, tint = Color(0xFF64748B)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        var isResolving by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = {
                                if (assignedArea.isBlank()) return@IconButton
                                isResolving = true
                                scope.launch {
                                    val coords = resolveAreaNameToCoordinates(context, assignedArea)
                                    if (coords != null) {
                                        latitude = coords.first.toString()
                                        longitude = coords.second.toString()
                                        errorMessage = null
                                    } else {
                                        errorMessage = "Could not resolve GPS for '$assignedArea'"
                                    }
                                    isResolving = false
                                }
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEFF6FF))
                                .border(1.dp, Color(0xFFDBEAFE), RoundedCornerShape(12.dp)),
                            enabled = !isResolving
                        ) {
                            if (isResolving) {
                                CircularProgressIndicator(color = Color(0xFF3B82F6), modifier = Modifier.size(18.dp))
                            } else {
                                Icon(Icons.Default.Search, contentDescription = "Resolve GPS", tint = Color(0xFF3B82F6))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            if (hasPermission) {
                                scope.launch {
                                    val details = fetchCurrentLocationCoordinates(context)
                                    if (details != null) {
                                        assignedArea = details.first
                                        latitude = details.second.toString()
                                        longitude = details.third.toString()
                                        errorMessage = null
                                    } else {
                                        errorMessage = "Failed to detect live location. Please ensure location services are enabled."
                                    }
                                }
                            } else {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF), contentColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Detect Live Location", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = latitude,
                            onValueChange = { latitude = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Latitude *") },
                            placeholder = { Text("e.g. 28.6139") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = longitude,
                            onValueChange = { longitude = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Longitude *") },
                            placeholder = { Text("e.g. 77.2090") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = coverageRadius,
                        onValueChange = { coverageRadius = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Coverage Radius (KM) *") },
                        placeholder = { Text("e.g. 10") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Initial Password *") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = {
                    if (fullName.isBlank() || email.isBlank() || employeeId.isBlank() || assignedArea.isBlank() || latitude.isBlank() || longitude.isBlank() || password.isBlank()) {
                        errorMessage = "Please fill all required fields"
                        return@Button
                    }
                    val latVal = latitude.toDoubleOrNull()
                    val lngVal = longitude.toDoubleOrNull()
                    val radVal = coverageRadius.toDoubleOrNull()
                    if (latVal == null || lngVal == null || radVal == null) {
                        errorMessage = "Please enter valid numeric values for coordinates and radius"
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        val result = ApiService.supervisorRegister(
                            SupervisorRegisterRequest(
                                full_name = fullName,
                                employee_id = employeeId,
                                email = email,
                                phone = phone.takeIf { it.isNotBlank() },
                                assigned_area = assignedArea,
                                latitude = latVal,
                                longitude = lngVal,
                                coverage_radius = radVal,
                                password = password
                            )
                        )
                        isLoading = false
                        if (result.isSuccess) {
                            onSupervisorAdded()
                        } else {
                            errorMessage = result.exceptionOrNull()?.message ?: "Failed to create supervisor"
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Create Supervisor Account", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
