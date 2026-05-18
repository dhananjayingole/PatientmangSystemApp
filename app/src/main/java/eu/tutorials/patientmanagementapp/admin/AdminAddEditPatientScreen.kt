package eu.tutorials.patientmanagementapp.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import eu.tutorials.patientmanagementapp.Model.Patient
import eu.tutorials.patientmanagementapp.viewmodels.AdminViewModel

/**
 * Add / Edit Patient screen.
 *
 * New flow:
 *   1. User books an appointment  →  userId is saved on the Appointment.
 *   2. Admin confirms the appointment.
 *   3. When the patient arrives, doctor taps "Add as Patient" on the appointment card.
 *   4. This screen opens with [prefillName] = appointment.userName
 *                                 and [prefillUserId] = appointment.userId.
 *   5. Doctor fills in age, blood group, etc. and saves.
 *   6. Patient record now has userId set automatically — no manual entry needed.
 *   7. Any prescription later saved for this patient will inherit the userId,
 *      so it will automatically appear on the patient's "My Prescriptions" screen.
 *
 * The userId field is intentionally hidden from the UI to avoid confusion;
 * it is carried silently in state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddEditPatientScreen(
    navController  : NavController,
    patientId      : String,
    prefillName    : String = "",   // pre-filled from appointment
    prefillUserId  : String = "",   // auto-linked from appointment
    adminViewModel : AdminViewModel
) {
    val context  = LocalContext.current
    val isNew    = patientId == "new"
    val existing = if (!isNew) adminViewModel.getPatientById(patientId) else null

    // Pre-fill from appointment when creating from appointment flow;
    // fall back to existing record values when editing.
    var name             by remember { mutableStateOf(existing?.name             ?: prefillName) }
    var age              by remember { mutableStateOf(existing?.age?.toString()  ?: "") }
    var gender           by remember { mutableStateOf(existing?.gender           ?: "Male") }
    var bloodGroup       by remember { mutableStateOf(existing?.bloodGroup       ?: "A+") }
    var phone            by remember { mutableStateOf(existing?.phoneNumber      ?: "") }
    var address          by remember { mutableStateOf(existing?.address          ?: "") }
    var emergencyContact by remember { mutableStateOf(existing?.emergencyContact ?: "") }
    var assignedDoctor   by remember { mutableStateOf(existing?.assignedDoctor   ?: "") }
    var medicalHistory   by remember { mutableStateOf(existing?.medicalHistory   ?: "") }

    // userId is carried silently — not shown in the UI
    val linkedUserId = existing?.userId?.takeIf { it.isNotEmpty() } ?: prefillUserId

    val isLoading by adminViewModel.isLoading.collectAsState()
    val toast     by adminViewModel.toastMessage.collectAsState()

    LaunchedEffect(toast) {
        toast?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            adminViewModel.clearToast()
            if (it.contains("added") || it.contains("updated")) navController.navigateUp()
        }
    }

    val genders     = listOf("Male", "Female", "Other")
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    val doctors     = listOf(
        "Dr. Nandu Jadhav", "Dr. Pandu Jadhav", "Dr. Krushna Nagapure",
        "Dr. Devang Masram", "Dr. Rahul Sharma", "Dr. Priya Patil"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isNew) "Add Patient" else "Edit Patient",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor        = Color(0xFF1976D2),
                    titleContentColor     = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Info banner when coming from appointment flow ─────────────────
            if (prefillUserId.isNotEmpty() || linkedUserId.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.OnlinePrediction,
                            contentDescription = null,
                            tint     = Color(0xFF1976D2),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Patient account linked automatically. " +
                                    "Prescriptions will appear on their app.",
                            fontSize = 12.sp,
                            color    = Color(0xFF1565C0)
                        )
                    }
                }
            }

            // ── Fields ───────────────────────────────────────────────────────

            OutlinedTextField(
                name, { name = it },
                Modifier.fillMaxWidth(),
                label      = { Text("Full Name *") },
                singleLine = true
            )

            OutlinedTextField(
                age, { age = it },
                Modifier.fillMaxWidth(),
                label          = { Text("Age *") },
                singleLine     = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                )
            )

            OutlinedTextField(
                phone, { phone = it },
                Modifier.fillMaxWidth(),
                label          = { Text("Phone Number") },
                singleLine     = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                )
            )

            OutlinedTextField(
                address, { address = it },
                Modifier.fillMaxWidth(),
                label    = { Text("Address") },
                minLines = 2
            )

            OutlinedTextField(
                emergencyContact, { emergencyContact = it },
                Modifier.fillMaxWidth(),
                label          = { Text("Emergency Contact") },
                singleLine     = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                )
            )

            // ── Gender dropdown ──────────────────────────────────────────────
            var genderExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(genderExpanded, { genderExpanded = it }) {
                OutlinedTextField(
                    gender, {},
                    Modifier.fillMaxWidth().menuAnchor(),
                    readOnly      = true,
                    label         = { Text("Gender") },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(genderExpanded) }
                )
                ExposedDropdownMenu(genderExpanded, { genderExpanded = false }) {
                    genders.forEach { g ->
                        DropdownMenuItem({ Text(g) }, { gender = g; genderExpanded = false })
                    }
                }
            }

            // ── Blood group dropdown ─────────────────────────────────────────
            var bgExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(bgExpanded, { bgExpanded = it }) {
                OutlinedTextField(
                    bloodGroup, {},
                    Modifier.fillMaxWidth().menuAnchor(),
                    readOnly     = true,
                    label        = { Text("Blood Group") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(bgExpanded) }
                )
                ExposedDropdownMenu(bgExpanded, { bgExpanded = false }) {
                    bloodGroups.forEach { bg ->
                        DropdownMenuItem({ Text(bg) }, { bloodGroup = bg; bgExpanded = false })
                    }
                }
            }

            // ── Doctor dropdown ──────────────────────────────────────────────
            var doctorExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(doctorExpanded, { doctorExpanded = it }) {
                OutlinedTextField(
                    assignedDoctor, {},
                    Modifier.fillMaxWidth().menuAnchor(),
                    readOnly     = true,
                    label        = { Text("Assigned Doctor") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(doctorExpanded) }
                )
                ExposedDropdownMenu(doctorExpanded, { doctorExpanded = false }) {
                    doctors.forEach { d ->
                        DropdownMenuItem({ Text(d) }, { assignedDoctor = d; doctorExpanded = false })
                    }
                }
            }

            OutlinedTextField(
                medicalHistory, { medicalHistory = it },
                Modifier.fillMaxWidth(),
                label    = { Text("Medical History") },
                minLines = 3
            )

            // ── Save button ──────────────────────────────────────────────────
            Button(
                onClick = {
                    if (name.isBlank() || age.isBlank()) {
                        Toast.makeText(context, "Name and Age are required", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val patient = Patient(
                        id               = if (!isNew) patientId else "",
                        name             = name.trim(),
                        age              = age.toIntOrNull() ?: 0,
                        gender           = gender,
                        bloodGroup       = bloodGroup,
                        phoneNumber      = phone.trim(),
                        address          = address.trim(),
                        emergencyContact = emergencyContact.trim(),
                        assignedDoctor   = assignedDoctor,
                        medicalHistory   = medicalHistory.trim(),
                        userId           = linkedUserId   // auto-linked, not entered manually
                    )
                    adminViewModel.savePatient(patient, isNew)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled  = !isLoading,
                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(
                        if (isNew) "Add Patient" else "Update Patient",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}