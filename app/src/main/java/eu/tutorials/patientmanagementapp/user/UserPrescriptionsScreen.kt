package eu.tutorials.patientmanagementapp.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import eu.tutorials.patientmanagementapp.Model.Medicine
import eu.tutorials.patientmanagementapp.Model.Prescription
import eu.tutorials.patientmanagementapp.viewmodels.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPrescriptionsScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel()
) {
    val prescriptions by userViewModel.prescriptions.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    var selectedPrescription by remember { mutableStateOf<Prescription?>(null) }
    var showDetailsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Prescriptions", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
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
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (prescriptions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No prescriptions found", fontSize = 16.sp, color = Color.Gray)
                        Text("Prescriptions will appear here", fontSize = 14.sp, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(prescriptions) { prescription ->
                        PrescriptionCard(
                            prescription = prescription,
                            onClick = {
                                selectedPrescription = prescription
                                showDetailsDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Prescription Details Dialog
    if (showDetailsDialog && selectedPrescription != null) {
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },
            title = {
                Column {
                    Text("Prescription Details", fontWeight = FontWeight.Bold)
                    Text(
                        text = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                            .format(java.util.Date(selectedPrescription!!.date)),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Doctor Info
                    Text(
                        text = "Prescribed by: Dr. ${selectedPrescription!!.doctorName}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Diagnosis
                    if (selectedPrescription!!.diagnosis.isNotEmpty()) {
                        Text(
                            text = "Diagnosis",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = selectedPrescription!!.diagnosis,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Medicines
                    Text(
                        text = "Medicines Prescribed",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    selectedPrescription!!.medicines.forEach { medicine ->
                        MedicineCard(medicine = medicine)
                    }

                    // Notes
                    if (selectedPrescription!!.notes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Additional Notes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = selectedPrescription!!.notes,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    // Follow-up
                    if (selectedPrescription!!.followUpDate > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Follow-up Date",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                                .format(java.util.Date(selectedPrescription!!.followUpDate)),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
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
fun PrescriptionCard(
    prescription: Prescription,
    onClick: () -> Unit
) {
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
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(Color(0xFF4CAF50).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Medication,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Dr. ${prescription.doctorName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${prescription.medicines.size} medicines prescribed",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                        .format(java.util.Date(prescription.date)),
                    fontSize = 12.sp,
                    color = Color(0xFF4CAF50)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun MedicineCard(medicine: Medicine) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = medicine.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = medicine.dosage,
                    fontSize = 12.sp,
                    color = Color(0xFF4CAF50)
                )
            }
            Text(
                text = "${medicine.frequency} • ${medicine.duration}",
                fontSize = 12.sp,
                color = Color.Gray
            )
            if (medicine.timing.isNotEmpty()) {
                Text(
                    text = "Take: ${medicine.timing}",
                    fontSize = 11.sp,
                    color = Color(0xFF2196F3)
                )
            }
        }
    }
}