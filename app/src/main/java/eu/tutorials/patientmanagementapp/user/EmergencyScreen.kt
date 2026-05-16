package eu.tutorials.patientmanagementapp.user

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import eu.tutorials.patientmanagementapp.R
import eu.tutorials.patientmanagementapp.auth.AuthViewModel
import eu.tutorials.patientmanagementapp.viewmodels.UserViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(
    navController: NavController,
    fusedLocationClient: FusedLocationProviderClient,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser by authViewModel.currentUser.collectAsState()
    val userData by userViewModel.userData.collectAsState()

    var isSendingAlert by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }
    var address by remember { mutableStateOf("") }
    var isLocationEnabled by remember { mutableStateOf(false) }

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    val locationPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                isLocationEnabled = isLocationEnabled()
                if (isLocationEnabled) {
                    getCurrentLocation(fusedLocationClient, context) { lat, lon, addr ->
                        latitude = lat
                        longitude = lon
                        address = addr
                        showLocationDialog = true
                    }
                } else {
                    Toast.makeText(context, "Please enable location services", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Location permission required for emergency alerts", Toast.LENGTH_LONG).show()
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency SOS", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD32F2F),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFD32F2F), Color(0xFFB71C1C))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Emergency Icon
                Card(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color(0xFFD32F2F)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "EMERGENCY",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "Tap the button below to send an emergency alert",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Emergency Button
                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            isLocationEnabled = isLocationEnabled()
                            if (isLocationEnabled) {
                                getCurrentLocation(fusedLocationClient, context) { lat, lon, addr ->
                                    latitude = lat
                                    longitude = lon
                                    address = addr
                                    showLocationDialog = true
                                }
                            } else {
                                Toast.makeText(context, "Please enable location services", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "SOS",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Emergency Numbers
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Emergency Contacts",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        EmergencyContactRow("Ambulance", "108")
                        EmergencyContactRow("Police", "100")
                        EmergencyContactRow("Fire", "101")
                        EmergencyContactRow("Hospital", "1800-123-4567")
                    }
                }
            }
        }
    }

    // Location Confirmation Dialog
    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text("Confirm Emergency Alert", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Your current location will be shared with the admin for emergency assistance.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF5F5F5)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "📍 Location:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(text = address.take(100), fontSize = 12.sp, color = Color.Gray)
                            Text(text = "📌 Coordinates: $latitude, $longitude", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLocationDialog = false
                        isSendingAlert = true
                        scope.launch {
                            userViewModel.sendEmergencyAlert(
                                userId = currentUser?.uid ?: "",
                                userName = userData?.name ?: currentUser?.email?.split("@")?.first() ?: "User",
                                userPhone = userData?.phoneNumber ?: "",
                                latitude = latitude,
                                longitude = longitude,
                                address = address
                            )
                            isSendingAlert = false
                            Toast.makeText(context, "Emergency alert sent! Help is on the way.", Toast.LENGTH_LONG).show()
                            navController.navigateUp()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    if (isSendingAlert) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Text("Send Emergency Alert")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EmergencyContactRow(name: String, number: String) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL)
                intent.data = android.net.Uri.parse("tel:$number")
                context.startActivity(intent)
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = number, fontSize = 14.sp, color = Color(0xFF1976D2))
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Call",
                modifier = Modifier.size(16.dp),
                tint = Color(0xFF4CAF50)
            )
        }
    }
}

@RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
fun getCurrentLocation(
    fusedLocationClient: FusedLocationProviderClient,
    context: Context,
    onResult: (Double, Double, String) -> Unit
) {
    val locationRequest = LocationRequest.Builder(
        LocationRequest.PRIORITY_HIGH_ACCURACY,
        10000
    ).build()

    fusedLocationClient.getCurrentLocation(
        LocationRequest.PRIORITY_HIGH_ACCURACY,
        null
    ).addOnSuccessListener { location ->
        if (location != null) {
            val latitude = location.latitude
            val longitude = location.longitude
            val address = getAddressFromLatLng(context, latitude, longitude)
            onResult(latitude, longitude, address)
        } else {
            Toast.makeText(context, "Unable to get location", Toast.LENGTH_SHORT).show()
        }
    }.addOnFailureListener {
        Toast.makeText(context, "Failed to get location: ${it.message}", Toast.LENGTH_SHORT).show()
    }
}

fun getAddressFromLatLng(context: Context, latitude: Double, longitude: Double): String {
    val geocoder = Geocoder(context, Locale.getDefault())
    return try {
        val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
        if (!addresses.isNullOrEmpty()) {
            addresses[0].getAddressLine(0) ?: "Address not found"
        } else {
            "Address not found"
        }
    } catch (e: Exception) {
        e.printStackTrace()
        "Error fetching address"
    }
}