package eu.tutorials.patientmanagementapp.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import eu.tutorials.patientmanagementapp.Model.Medicine
import eu.tutorials.patientmanagementapp.Model.Prescription
import eu.tutorials.patientmanagementapp.viewmodels.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddEditPrescriptionScreen(
    navController: NavController,
    prescriptionId: String,
    patientId: String,
    adminViewModel: AdminViewModel = viewModel()
) {
    val context = LocalContext.current
    val isNew = prescriptionId == "new"
    val existing = if (!isNew) adminViewModel.getPrescriptionById(prescriptionId) else null
    val patients by adminViewModel.patients.collectAsState()
    val isLoading by adminViewModel.isLoading.collectAsState()
    val toast by adminViewModel.toastMessage.collectAsState()

    var selectedPatientId by remember { mutableStateOf(existing?.patientId ?: patientId) }
    var doctorName by remember { mutableStateOf(existing?.doctorName ?: "") }
    var diagnosis by remember { mutableStateOf(existing?.diagnosis ?: "") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var medicines by remember {
        mutableStateOf(
            if (existing?.medicines?.isNotEmpty() == true) existing.medicines.toMutableList()
            else mutableListOf(Medicine())
        )
    }

    LaunchedEffect(toast) {
        toast?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            adminViewModel.clearToast()
            if (it.contains("added") || it.contains("updated")) navController.navigateUp()
        }
    }

    val selectedPatient = patients.find { it.id == selectedPatientId }
    val doctorsList = listOf(
        "Dr. Nandu Jadhav", "Dr. Pandu Jadhav", "Dr. Krushna Nagapure",
        "Dr. Devang Masram", "Dr. Rahul Sharma", "Dr. Priya Patil"
    )
    val timingsList = listOf("Before Meals", "After Meals", "With Meals", "At Bedtime", "Morning", "Evening")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "New Prescription" else "Edit Prescription", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1976D2),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (selectedPatientId.isBlank() || doctorName.isBlank()) {
                        Toast.makeText(context, "Patient and Doctor are required", Toast.LENGTH_SHORT).show()
                        return@ExtendedFloatingActionButton
                    }
                    val validMedicines = medicines.filter { it.name.isNotBlank() }
                    if (validMedicines.isEmpty()) {
                        Toast.makeText(context, "Add at least one medicine", Toast.LENGTH_SHORT).show()
                        return@ExtendedFloatingActionButton
                    }
                    val prescription = Prescription(
                        id = if (!isNew) prescriptionId else "",
                        patientId = selectedPatientId,
                        patientName = selectedPatient?.name ?: "",
                        doctorName = doctorName,
                        diagnosis = diagnosis,
                        notes = notes,
                        medicines = validMedicines,
                        date = System.currentTimeMillis()
                    )
                    adminViewModel.savePrescription(prescription, isNew)
                },
                icon = { Icon(Icons.Default.Save, contentDescription = null) },
                text = { Text(if (isNew) "Save Prescription" else "Update") },
                containerColor = Color(0xFF4CAF50)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Text("Patient & Doctor", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))

                // Patient dropdown
                var patientExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(patientExpanded, { patientExpanded = it }) {
                    OutlinedTextField(
                        value = selectedPatient?.name ?: "Select Patient",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        readOnly = true,
                        label = { Text("Patient *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(patientExpanded) }
                    )
                    ExposedDropdownMenu(patientExpanded, { patientExpanded = false }) {
                        patients.forEach { p ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(p.name, fontWeight = FontWeight.Medium)
                                        Text("${p.age}y • ${p.bloodGroup} • ${p.phoneNumber}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                },
                                onClick = { selectedPatientId = p.id; patientExpanded = false }
                            )
                        }
                    }
                }
            }

            item {
                // Doctor dropdown
                var doctorExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(doctorExpanded, { doctorExpanded = it }) {
                    OutlinedTextField(
                        value = doctorName.ifEmpty { "Select Doctor" },
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        readOnly = true,
                        label = { Text("Doctor *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(doctorExpanded) }
                    )
                    ExposedDropdownMenu(doctorExpanded, { doctorExpanded = false }) {
                        doctorsList.forEach { d ->
                            DropdownMenuItem(text = { Text(d) }, onClick = { doctorName = d; doctorExpanded = false })
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = diagnosis,
                    onValueChange = { diagnosis = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Diagnosis") },
                    minLines = 2
                )
            }

            // Medicines header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Medicines", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Button(
                        onClick = { medicines = (medicines + Medicine()).toMutableList() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add")
                    }
                }
            }

            itemsIndexed(medicines) { index, medicine ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Medicine ${index + 1}",
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1976D2)
                            )
                            if (medicines.size > 1) {
                                IconButton(
                                    onClick = {
                                        medicines = medicines.toMutableList().also { it.removeAt(index) }
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                                }
                            }
                        }

                        // Medicine name
                        OutlinedTextField(
                            value = medicine.name,
                            onValueChange = { newVal ->
                                medicines = medicines.toMutableList().also { it[index] = it[index].copy(name = newVal) }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Medicine Name *") },
                            singleLine = true
                        )

                        // Dosage + Frequency
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = medicine.dosage,
                                onValueChange = { newVal ->
                                    medicines = medicines.toMutableList().also { it[index] = it[index].copy(dosage = newVal) }
                                },
                                modifier = Modifier.weight(1f),
                                label = { Text("Dosage") },
                                placeholder = { Text("e.g. 500mg") },
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = medicine.frequency,
                                onValueChange = { newVal ->
                                    medicines = medicines.toMutableList().also { it[index] = it[index].copy(frequency = newVal) }
                                },
                                modifier = Modifier.weight(1f),
                                label = { Text("Frequency") },
                                placeholder = { Text("e.g. 1-0-1") },
                                singleLine = true
                            )
                        }

                        // Duration + Timing
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = medicine.duration,
                                onValueChange = { newVal ->
                                    medicines = medicines.toMutableList().also { it[index] = it[index].copy(duration = newVal) }
                                },
                                modifier = Modifier.weight(1f),
                                label = { Text("Duration") },
                                placeholder = { Text("e.g. 5 days") },
                                singleLine = true
                            )

                            var timingExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                timingExpanded,
                                { timingExpanded = it },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = medicine.timing.ifEmpty { "Timing" },
                                    onValueChange = {},
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    readOnly = true,
                                    label = { Text("Timing") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(timingExpanded) }
                                )
                                ExposedDropdownMenu(timingExpanded, { timingExpanded = false }) {
                                    timingsList.forEach { t ->
                                        DropdownMenuItem(
                                            text = { Text(t) },
                                            onClick = {
                                                medicines = medicines.toMutableList().also { it[index] = it[index].copy(timing = t) }
                                                timingExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Additional Notes") },
                    placeholder = { Text("Special instructions, dietary advice, etc.") },
                    minLines = 3
                )
            }

            // Space for FAB
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}