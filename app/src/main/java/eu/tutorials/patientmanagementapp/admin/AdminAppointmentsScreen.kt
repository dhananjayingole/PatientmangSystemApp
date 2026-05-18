package eu.tutorials.patientmanagementapp.admin

import android.widget.Toast
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
import eu.tutorials.patientmanagementapp.Model.Appointment
import eu.tutorials.patientmanagementapp.Navigation.Routes
import eu.tutorials.patientmanagementapp.viewmodels.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAppointmentsScreen(
    navController: NavController,
    adminViewModel: AdminViewModel
) {
    val context      = LocalContext.current
    val appointments by adminViewModel.appointments.collectAsState()
    val patients     by adminViewModel.patients.collectAsState()
    val isLoading    by adminViewModel.isLoading.collectAsState()
    val toast        by adminViewModel.toastMessage.collectAsState()

    var filterStatus by remember { mutableStateOf("all") }
    val filters = listOf("all", "pending", "confirmed", "completed", "cancelled")

    LaunchedEffect(toast) {
        toast?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            adminViewModel.clearToast()
        }
    }

    val filtered = if (filterStatus == "all") appointments
    else appointments.filter { it.status == filterStatus }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointments", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor             = Color(0xFF1976D2),
                    titleContentColor          = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {

            // ── Summary row ──────────────────────────────────────────────────
            val todayStr = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                .format(java.util.Date())
            val todayCount   = appointments.count { it.date == todayStr }
            val pendingCount = appointments.count { it.status == "pending" }

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryChip("Today: $todayCount",          Color(0xFF9C27B0), Modifier.weight(1f))
                SummaryChip("Pending: $pendingCount",      Color(0xFFFFC107), Modifier.weight(1f))
                SummaryChip("Total: ${appointments.size}", Color(0xFF2196F3), Modifier.weight(1f))
            }

            // ── Filter tabs ──────────────────────────────────────────────────
            ScrollableTabRow(
                selectedTabIndex = filters.indexOf(filterStatus),
                edgePadding      = 16.dp,
                containerColor   = Color.White,
                contentColor     = Color(0xFF1976D2)
            ) {
                filters.forEach { f ->
                    Tab(
                        selected = filterStatus == f,
                        onClick  = { filterStatus = f },
                        text     = {
                            Text(
                                text       = f.replaceFirstChar { it.uppercase() },
                                fontWeight = if (filterStatus == f) FontWeight.Bold else FontWeight.Normal,
                                fontSize   = 13.sp
                            )
                        }
                    )
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            } else if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CalendarToday, null, Modifier.size(64.dp), tint = Color.Gray)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No ${if (filterStatus == "all") "" else filterStatus} appointments",
                            color = Color.Gray, fontSize = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered) { appt ->
                        val alreadyPatient = patients.any { it.userId == appt.userId }

                        AppointmentAdminCard(
                            appointment    = appt,
                            alreadyPatient = alreadyPatient,
                            onStatusUpdate = { status, notes ->
                                adminViewModel.updateAppointmentStatus(appt.id, status, notes)
                            },
                            // Uses Routes.adminAddPatientFromAppointment() which matches
                            // ADMIN_ADD_PATIENT_FROM_APPT route in your existing Routes.kt
                            onAddAsPatient = {
                                navController.navigate(
                                    Routes.adminAddPatientFromAppointment(
                                        userName = appt.userName,
                                        userId   = appt.userId
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

// ── Appointment card ──────────────────────────────────────────────────────────

@Composable
fun AppointmentAdminCard(
    appointment    : Appointment,
    alreadyPatient : Boolean,
    onStatusUpdate : (String, String) -> Unit,
    onAddAsPatient : () -> Unit
) {
    var showNoteDialog by remember { mutableStateOf(false) }
    var pendingStatus  by remember { mutableStateOf("") }
    var noteText       by remember { mutableStateOf("") }

    val statusColor = when (appointment.status) {
        "pending"   -> Color(0xFFFFC107)
        "confirmed" -> Color(0xFF4CAF50)
        "completed" -> Color(0xFF2196F3)
        "cancelled" -> Color.Red
        else        -> Color.Gray
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            // ── Header ────────────────────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(appointment.userName,        fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(appointment.doctorName,      fontSize = 13.sp, color = Color(0xFF1976D2))
                    Text(appointment.doctorSpecialty, fontSize = 12.sp, color = Color.Gray)
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text     = appointment.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(Modifier.height(10.dp))

            // ── Date / Time ───────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, null, Modifier.size(16.dp), tint = Color(0xFF1976D2))
                Spacer(Modifier.width(6.dp))
                Text(
                    "${appointment.date}  •  ${appointment.time}",
                    fontSize = 13.sp, fontWeight = FontWeight.Medium
                )
            }

            if (appointment.reason.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Info, null, Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(Modifier.width(6.dp))
                    Text("Reason: ${appointment.reason}", fontSize = 12.sp, color = Color.Gray)
                }
            }

            if (appointment.notes.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Notes, null, Modifier.size(16.dp), tint = Color(0xFF4CAF50))
                    Spacer(Modifier.width(6.dp))
                    Text("Doctor's note: ${appointment.notes}", fontSize = 12.sp, color = Color(0xFF4CAF50))
                }
            }

            // ── Status action buttons ─────────────────────────────────────────
            if (appointment.status != "completed" && appointment.status != "cancelled") {
                Spacer(Modifier.height(12.dp))
                when (appointment.status) {
                    "pending" -> {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick  = { pendingStatus = "confirmed"; showNoteDialog = true },
                                modifier = Modifier.weight(1f),
                                colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Confirm", fontSize = 13.sp)
                            }
                            OutlinedButton(
                                onClick  = { onStatusUpdate("cancelled", "") },
                                modifier = Modifier.weight(1f),
                                colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                border   = androidx.compose.foundation.BorderStroke(1.dp, Color.Red)
                            ) {
                                Icon(Icons.Default.Close, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Cancel", fontSize = 13.sp)
                            }
                        }
                    }
                    "confirmed" -> {
                        Button(
                            onClick  = { pendingStatus = "completed"; showNoteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                        ) {
                            Icon(Icons.Default.Done, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Mark as Completed")
                        }
                    }
                }
            }

            // ── "Add as Patient" button ───────────────────────────────────────
            // Shown on confirmed/completed appointments. Doctor taps this when
            // patient arrives at clinic — name and userId pre-fill automatically.
            if ((appointment.status == "confirmed" || appointment.status == "completed") &&
                appointment.userId.isNotEmpty()
            ) {
                Spacer(Modifier.height(8.dp))
                if (alreadyPatient) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle, null,
                            tint     = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Already registered as patient",
                            fontSize   = 12.sp,
                            color      = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick  = onAddAsPatient,
                        modifier = Modifier.fillMaxWidth(),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1976D2)),
                        border   = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1976D2))
                    ) {
                        Icon(Icons.Default.PersonAdd, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add as Patient", fontSize = 13.sp)
                    }
                }
            }
        }
    }

    // ── Note dialog ───────────────────────────────────────────────────────────
    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title  = { Text(if (pendingStatus == "confirmed") "Confirm Appointment" else "Mark as Completed") },
            text   = {
                Column {
                    Text("Add a doctor's note (optional):", fontSize = 13.sp, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value         = noteText,
                        onValueChange = { noteText = it },
                        modifier      = Modifier.fillMaxWidth(),
                        placeholder   = { Text("e.g. Bring previous reports, fast for 4 hours…") },
                        minLines      = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onStatusUpdate(pendingStatus, noteText)
                        showNoteDialog = false
                        noteText = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text(if (pendingStatus == "confirmed") "Confirm" else "Complete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun SummaryChip(text: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Text(
            text       = text,
            modifier   = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            fontSize   = 12.sp,
            fontWeight = FontWeight.Bold,
            color      = color
        )
    }
}