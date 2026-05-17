package eu.tutorials.patientmanagementapp.user

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import eu.tutorials.patientmanagementapp.Model.Appointment
import eu.tutorials.patientmanagementapp.viewmodels.UserViewModel

// ─── Status helpers ───────────────────────────────────────────────────────────

private enum class AppointmentTab(val label: String) {
    ALL("All"), UPCOMING("Upcoming"), PAST("Past"), CANCELLED("Cancelled")
}

private val Appointment.isUpcoming: Boolean
    get() = status == "pending" || status == "confirmed"

private val Appointment.isPast: Boolean
    get() = status == "completed"

private val Appointment.isCancelled: Boolean
    get() = status == "cancelled"

private fun statusColor(status: String) = when (status) {
    "confirmed"  -> Color(0xFF1976D2)
    "completed"  -> Color(0xFF388E3C)
    "cancelled"  -> Color(0xFFD32F2F)
    else         -> Color(0xFFF57C00) // pending
}

private fun statusIcon(status: String): ImageVector = when (status) {
    "confirmed"  -> Icons.Default.CheckCircle
    "completed"  -> Icons.Default.TaskAlt
    "cancelled"  -> Icons.Default.Cancel
    else         -> Icons.Default.Schedule
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppointmentsScreen(
    navController: NavController,
    userViewModel: UserViewModel
) {
    val allAppointments by userViewModel.allAppointments.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()

    var selectedTab by remember { mutableStateOf(AppointmentTab.ALL) }

    val displayed = remember(allAppointments, selectedTab) {
        when (selectedTab) {
            AppointmentTab.ALL       -> allAppointments
            AppointmentTab.UPCOMING  -> allAppointments.filter { it.isUpcoming }
            AppointmentTab.PAST      -> allAppointments.filter { it.isPast }
            AppointmentTab.CANCELLED -> allAppointments.filter { it.isCancelled }
        }.sortedByDescending { it.createdAt }
    }

    Scaffold(
        topBar = {
            Column {
                // Gradient header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF1565C0), Color(0xFF1976D2))
                            )
                        )
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                        }
                        Spacer(Modifier.width(4.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "My Appointments",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            Text(
                                "${allAppointments.size} total",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Tab row
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = Color(0xFF1976D2),
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = Color.White
                        )
                    }
                ) {
                    AppointmentTab.entries.forEach { tab ->
                        val count = when (tab) {
                            AppointmentTab.ALL       -> allAppointments.size
                            AppointmentTab.UPCOMING  -> allAppointments.count { it.isUpcoming }
                            AppointmentTab.PAST      -> allAppointments.count { it.isPast }
                            AppointmentTab.CANCELLED -> allAppointments.count { it.isCancelled }
                        }
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(tab.label, fontSize = 13.sp)
                                    if (count > 0) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (selectedTab == tab)
                                                Color.White.copy(alpha = 0.25f)
                                            else
                                                Color.White.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                "$count",
                                                modifier = Modifier.padding(
                                                    horizontal = 6.dp, vertical = 1.dp
                                                ),
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF1976D2))
                }
            }

            displayed.isEmpty() -> {
                EmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    tab = selectedTab
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Optional section header for upcoming
                    if (selectedTab == AppointmentTab.ALL) {
                        val upcoming = displayed.filter { it.isUpcoming }
                        val past = displayed.filter { it.isPast || it.isCancelled }

                        if (upcoming.isNotEmpty()) {
                            item {
                                SectionHeader("Upcoming", upcoming.size, Color(0xFF1976D2))
                            }
                            items(upcoming, key = { it.id }) { appt ->
                                AppointmentCard(appt)
                            }
                        }
                        if (past.isNotEmpty()) {
                            item {
                                SectionHeader("History", past.size, Color.Gray)
                            }
                            items(past, key = { it.id }) { appt ->
                                AppointmentCard(appt)
                            }
                        }
                    } else {
                        items(displayed, key = { it.id }) { appt ->
                            AppointmentCard(appt)
                        }
                    }

                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

// ─── Section header ───────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String, count: Int, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            Modifier
                .size(4.dp, 18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            title,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = color
        )
        Spacer(Modifier.width(6.dp))
        Text("($count)", fontSize = 13.sp, color = Color.Gray)
    }
}

@Composable
private fun InfoChip(icon: ImageVector, text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
        Text(text, fontSize = 12.sp, color = color)
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null, tint = Color(0xFF1976D2), modifier = Modifier.size(16.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 13.sp, color = Color(0xFF212121))
        }
    }
}

// ─── Empty state ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(modifier: Modifier = Modifier, tab: AppointmentTab) {
    val (icon, message) = when (tab) {
        AppointmentTab.UPCOMING  -> Icons.Default.EventAvailable to "No upcoming appointments.\nBook one to get started."
        AppointmentTab.PAST      -> Icons.Default.History        to "No completed appointments yet."
        AppointmentTab.CANCELLED -> Icons.Default.EventBusy      to "No cancelled appointments."
        AppointmentTab.ALL       -> Icons.Default.CalendarToday  to "You have no appointments yet.\nBook one to get started."
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFE3F2FD),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon, null,
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Text(
                message,
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}