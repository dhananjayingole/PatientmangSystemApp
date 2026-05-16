package eu.tutorials.patientmanagementapp.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import eu.tutorials.patientmanagementapp.Model.Patient
import eu.tutorials.patientmanagementapp.viewmodels.AdminViewModel
import kotlin.collections.filter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPatientsScreen(
    navController: NavController,
    adminViewModel: AdminViewModel = viewModel()
) {
    val patients by adminViewModel.patients.collectAsState()
    val isLoading by adminViewModel.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedPatient by remember { mutableStateOf<Patient?>(null) }
    var showDetailsDialog by remember { mutableStateOf(false) }

    val filteredPatients = if (searchQuery.isEmpty()) patients
    else patients.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Patients", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search patients...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredPatients.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No patients found", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredPatients) { patient ->
                        PatientAdminCard(
                            patient = patient,
                            onClick = {
                                selectedPatient = patient
                                showDetailsDialog = true
                            },
                            onDelete = { adminViewModel.deletePatient(patient.id) }
                        )
                    }
                }
            }
        }
    }

    // Patient Details Dialog
    if (showDetailsDialog && selectedPatient != null) {
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },
            title = { Text(selectedPatient!!.name) },
            text = {
                Column {
                    DetailRow("Age", "${selectedPatient!!.age} years")
                    DetailRow("Gender", selectedPatient!!.gender)
                    DetailRow("Blood Group", selectedPatient!!.bloodGroup)
                    DetailRow("Phone", selectedPatient!!.phoneNumber)
                    DetailRow("Emergency Contact", selectedPatient!!.emergencyContact)
                    DetailRow("Doctor", selectedPatient!!.assignedDoctor)
                    DetailRow("Medical History", selectedPatient!!.medicalHistory.take(100))
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetailsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label: ",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value.ifEmpty { "Not specified" },
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun PatientAdminCard(
    patient: Patient,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = patient.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${patient.age} years • ${patient.gender} • ${patient.bloodGroup}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Doctor: ${patient.assignedDoctor}",
                    fontSize = 12.sp,
                    color = Color(0xFF1976D2)
                )
            }

            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Patient") },
            text = { Text("Are you sure you want to delete ${patient.name}'s record?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}