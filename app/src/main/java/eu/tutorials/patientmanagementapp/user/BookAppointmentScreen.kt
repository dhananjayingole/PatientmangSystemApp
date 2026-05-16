package eu.tutorials.patientmanagementapp.user

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import eu.tutorials.patientmanagementapp.auth.AuthViewModel
import eu.tutorials.patientmanagementapp.viewmodels.UserViewModel
import kotlinx.coroutines.delay
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAppointmentScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel = viewModel()
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val currentUser by authViewModel.currentUser.collectAsState()
    val userData by userViewModel.userData.collectAsState()
    val toastMessage by userViewModel.toastMessage.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState() // Use ViewModel's loading state

    var selectedDoctor by remember { mutableStateOf("") }
    var selectedSpecialty by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Show toast messages
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            userViewModel.clearToast()
        }
    }

    // Navigate back on success dialog dismiss
    LaunchedEffect(showSuccessDialog) {
        if (showSuccessDialog) {
            delay(2000) // Optional: auto-dismiss after 2 seconds
            showSuccessDialog = false
            navController.navigateUp()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Appointment", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Doctor Selection Dropdown
            var doctorExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = doctorExpanded,
                onExpandedChange = { doctorExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedDoctor,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Doctor") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = doctorExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = MaterialTheme.shapes.medium
                )
                ExposedDropdownMenu(
                    expanded = doctorExpanded,
                    onDismissRequest = { doctorExpanded = false }
                ) {
                    val doctors = listOf(
                        "Dr. Nandu Jadhav" to "Cardiologist",
                        "Dr. Pandu Jadhav" to "Neurologist",
                        "Dr. Krushna Nagapure" to "Gynecologist",
                        "Dr. Devang Masram" to "Dermatologist",
                        "Dr. Rahul Sharma" to "Orthopedic",
                        "Dr. Priya Patil" to "Pediatrician"
                    )
                    doctors.forEach { (doctor, specialty) ->
                        DropdownMenuItem(
                            text = { Column {
                                Text(doctor, fontWeight = FontWeight.Medium)
                                Text(specialty, fontSize = 12.sp, color = Color.Gray)
                            } },
                            onClick = {
                                selectedDoctor = doctor
                                selectedSpecialty = specialty
                                doctorExpanded = false
                            }
                        )
                    }
                }
            }

            // Date Picker
            OutlinedTextField(
                value = selectedDate,
                onValueChange = { },
                label = { Text("Select Date") },
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                selectedDate = "$dayOfMonth/${month + 1}/$year"
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                shape = MaterialTheme.shapes.medium
            )

            // Time Picker
            OutlinedTextField(
                value = selectedTime,
                onValueChange = { },
                label = { Text("Select Time") },
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Default.AccessTime, contentDescription = "Select Time")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                shape = MaterialTheme.shapes.medium
            )

            // Reason
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Reason for Visit") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Submit Button
            Button(
                onClick = {
                    if (selectedDoctor.isNotEmpty() && selectedDate.isNotEmpty() && selectedTime.isNotEmpty()) {
                        if (userData?.name == null && currentUser?.email == null) {
                            errorMessage = "Please complete your profile first"
                            showErrorDialog = true
                            return@Button
                        }

                        userViewModel.bookAppointment(
                            userName = userData?.name ?: currentUser?.email?.split("@")?.first() ?: "User",
                            doctorName = selectedDoctor,
                            specialty = selectedSpecialty,
                            date = selectedDate,
                            time = selectedTime,
                            reason = reason,
                        )
                        showSuccessDialog = true
                    } else {
                        errorMessage = "Please fill all required fields"
                        showErrorDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Confirm Appointment", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    // Success Dialog
    if (showSuccessDialog && !isLoading) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                navController.navigateUp()
            },
            icon = {
                Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = Color(0xFF4CAF50))
            },
            title = { Text("Appointment Booked!") },
            text = { Text("Your appointment has been confirmed. You will receive a notification with further details.") },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    navController.navigateUp()
                }) {
                    Text("OK")
                }
            }
        )
    }

    // Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            icon = {
                Icon(Icons.Default.Error, contentDescription = "Error", tint = Color.Red)
            },
            title = { Text("Booking Failed") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}