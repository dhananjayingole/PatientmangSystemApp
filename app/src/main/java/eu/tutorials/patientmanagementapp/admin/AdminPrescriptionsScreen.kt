package eu.tutorials.patientmanagementapp.admin

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import eu.tutorials.patientmanagementapp.Model.Prescription
import eu.tutorials.patientmanagementapp.Navigation.Routes
import eu.tutorials.patientmanagementapp.viewmodels.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPrescriptionsScreen(
    navController: NavController,
    adminViewModel: AdminViewModel = viewModel()
) {
    val context = LocalContext.current
    val prescriptions by adminViewModel.prescriptions.collectAsState()
    val patients by adminViewModel.patients.collectAsState()
    val isLoading by adminViewModel.isLoading.collectAsState()
    val toast by adminViewModel.toastMessage.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(toast) { toast?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); adminViewModel.clearToast() } }

    val filtered = if (searchQuery.isBlank()) prescriptions
    else prescriptions.filter { it.patientName.contains(searchQuery, true) || it.doctorName.contains(searchQuery, true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prescriptions", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.adminAddEditPrescription()) }) {
                        Icon(Icons.Default.Add, "Add", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1976D2), titleContentColor = Color.White, navigationIconContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                searchQuery, { searchQuery = it },
                Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search by patient or doctor...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true, shape = MaterialTheme.shapes.medium
            )

            if (isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            } else if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Medication, null, Modifier.size(64.dp), tint = Color.Gray)
                        Spacer(Modifier.height(16.dp))
                        Text("No prescriptions found", color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { navController.navigate(Routes.adminAddEditPrescription()) }) { Text("Add Prescription") }
                    }
                }
            } else {
                LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filtered) { prescription ->
                        PrescriptionAdminCard(
                            prescription = prescription,
                            onClick = { navController.navigate(Routes.adminAddEditPrescription(prescription.id, prescription.patientId)) },
                            onDelete = { adminViewModel.deletePrescription(prescription.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrescriptionAdminCard(prescription: Prescription, onClick: () -> Unit, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = MaterialTheme.shapes.medium, elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Medication, null, Modifier.size(40.dp), tint = Color(0xFF4CAF50))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(prescription.patientName, fontWeight = FontWeight.Bold)
                Text("Dr. ${prescription.doctorName}", fontSize = 12.sp, color = Color(0xFF1976D2))
                Text("${prescription.medicines.size} medicine(s) • ${java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(prescription.date))}", fontSize = 11.sp, color = Color.Gray)
                if (prescription.diagnosis.isNotEmpty()) Text("Dx: ${prescription.diagnosis.take(40)}", fontSize = 11.sp, color = Color.Gray)
            }
            Column {
                IconButton(onClick = onClick) { Icon(Icons.Default.Edit, null, tint = Color(0xFF1976D2)) }
                IconButton(onClick = { showDeleteDialog = true }) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
            }
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Prescription") },
            text = { Text("Delete prescription for ${prescription.patientName}?") },
            confirmButton = { TextButton(onClick = { onDelete(); showDeleteDialog = false }) { Text("Delete", color = Color.Red) } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }
}