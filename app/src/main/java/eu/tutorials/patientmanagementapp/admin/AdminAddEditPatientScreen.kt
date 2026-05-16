package eu.tutorials.patientmanagementapp.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import eu.tutorials.patientmanagementapp.Model.Patient
import eu.tutorials.patientmanagementapp.viewmodels.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddEditPatientScreen(
    navController: NavController,
    patientId: String,
    adminViewModel: AdminViewModel = viewModel()
) {
    val context = LocalContext.current
    val isNew = patientId == "new"
    val existing = if (!isNew) adminViewModel.getPatientById(patientId) else null

    var name by remember { mutableStateOf(existing?.name ?: "") }
    var age by remember { mutableStateOf(existing?.age?.toString() ?: "") }
    var gender by remember { mutableStateOf(existing?.gender ?: "Male") }
    var bloodGroup by remember { mutableStateOf(existing?.bloodGroup ?: "A+") }
    var phone by remember { mutableStateOf(existing?.phoneNumber ?: "") }
    var address by remember { mutableStateOf(existing?.address ?: "") }
    var emergencyContact by remember { mutableStateOf(existing?.emergencyContact ?: "") }
    var assignedDoctor by remember { mutableStateOf(existing?.assignedDoctor ?: "") }
    var medicalHistory by remember { mutableStateOf(existing?.medicalHistory ?: "") }
    var userId by remember { mutableStateOf(existing?.userId ?: "") }

    val isLoading by adminViewModel.isLoading.collectAsState()
    val toast by adminViewModel.toastMessage.collectAsState()

    LaunchedEffect(toast) {
        toast?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            adminViewModel.clearToast()
            if (it.contains("added") || it.contains("updated")) navController.navigateUp()
        }
    }

    val genders = listOf("Male", "Female", "Other")
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    val doctors = listOf("Dr. Nandu Jadhav", "Dr. Pandu Jadhav", "Dr. Krushna Nagapure", "Dr. Devang Masram", "Dr. Rahul Sharma", "Dr. Priya Patil")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "Add Patient" else "Edit Patient", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1976D2), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Full Name *") }, singleLine = true)
            OutlinedTextField(age, { age = it }, Modifier.fillMaxWidth(), label = { Text("Age *") }, singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
            OutlinedTextField(phone, { phone = it }, Modifier.fillMaxWidth(), label = { Text("Phone Number") }, singleLine = true)
            OutlinedTextField(address, { address = it }, Modifier.fillMaxWidth(), label = { Text("Address") }, minLines = 2)
            OutlinedTextField(emergencyContact, { emergencyContact = it }, Modifier.fillMaxWidth(), label = { Text("Emergency Contact") }, singleLine = true)

            // Gender Dropdown
            var genderExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(genderExpanded, { genderExpanded = it }) {
                OutlinedTextField(gender, {}, Modifier.fillMaxWidth().menuAnchor(), readOnly = true, label = { Text("Gender") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(genderExpanded) })
                ExposedDropdownMenu(genderExpanded, { genderExpanded = false }) {
                    genders.forEach { g -> DropdownMenuItem({ Text(g) }, { gender = g; genderExpanded = false }) }
                }
            }

            // Blood Group Dropdown
            var bgExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(bgExpanded, { bgExpanded = it }) {
                OutlinedTextField(bloodGroup, {}, Modifier.fillMaxWidth().menuAnchor(), readOnly = true, label = { Text("Blood Group") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(bgExpanded) })
                ExposedDropdownMenu(bgExpanded, { bgExpanded = false }) {
                    bloodGroups.forEach { bg -> DropdownMenuItem({ Text(bg) }, { bloodGroup = bg; bgExpanded = false }) }
                }
            }

            // Doctor Dropdown
            var doctorExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(doctorExpanded, { doctorExpanded = it }) {
                OutlinedTextField(assignedDoctor, {}, Modifier.fillMaxWidth().menuAnchor(), readOnly = true, label = { Text("Assigned Doctor") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(doctorExpanded) })
                ExposedDropdownMenu(doctorExpanded, { doctorExpanded = false }) {
                    doctors.forEach { d -> DropdownMenuItem({ Text(d) }, { assignedDoctor = d; doctorExpanded = false }) }
                }
            }

            OutlinedTextField(medicalHistory, { medicalHistory = it }, Modifier.fillMaxWidth(), label = { Text("Medical History") }, minLines = 3)
            OutlinedTextField(userId, { userId = it }, Modifier.fillMaxWidth(), label = { Text("Linked User ID (optional)") }, singleLine = true)

            Button(
                onClick = {
                    if (name.isBlank() || age.isBlank()) { Toast.makeText(context, "Name and Age are required", Toast.LENGTH_SHORT).show(); return@Button }
                    val patient = Patient(
                        id = if (!isNew) patientId else "",
                        name = name, age = age.toIntOrNull() ?: 0, gender = gender,
                        bloodGroup = bloodGroup, phoneNumber = phone, address = address,
                        emergencyContact = emergencyContact, assignedDoctor = assignedDoctor,
                        medicalHistory = medicalHistory, userId = userId
                    )
                    adminViewModel.savePatient(patient, isNew)
                },
                Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(Color(0xFF1976D2))
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
                else Text(if (isNew) "Add Patient" else "Update Patient", fontWeight = FontWeight.Bold)
            }
        }
    }
}