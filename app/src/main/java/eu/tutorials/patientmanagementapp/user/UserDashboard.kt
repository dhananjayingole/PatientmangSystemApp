package eu.tutorials.patientmanagementapp.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import eu.tutorials.patientmanagementapp.Model.Appointment
import eu.tutorials.patientmanagementapp.Model.Prescription
import eu.tutorials.patientmanagementapp.Navigation.Routes
import eu.tutorials.patientmanagementapp.auth.AuthViewModel
import eu.tutorials.patientmanagementapp.viewmodels.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboard(
    navController: NavController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentUser by authViewModel.currentUser.collectAsState()
    val userData by userViewModel.userData.collectAsState()
    val recentPrescriptions by userViewModel.recentPrescriptions.collectAsState()
    val upcomingAppointments by userViewModel.upcomingAppointments.collectAsState()

    val menuItems = listOf(
        DashboardMenuItem("My Profile", "View and edit profile", Icons.Default.Person, Routes.USER_PROFILE),
        DashboardMenuItem("My Prescriptions", "View prescribed medicines", Icons.Default.Medication, Routes.USER_PRESCRIPTIONS),
        DashboardMenuItem("Book Appointment", "Schedule doctor visit", Icons.Default.CalendarToday, Routes.BOOK_APPOINTMENT),
        DashboardMenuItem("Yoga Exercises", "Wellness activities", Icons.Default.FitnessCenter, Routes.YOGA_EXERCISES),
        DashboardMenuItem("Emergency", "SOS emergency help", Icons.Default.Warning, Routes.EMERGENCY),
        DashboardMenuItem("Notifications", "View alerts", Icons.Default.Notifications, Routes.NOTIFICATIONS)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))

                // User Profile Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF4CAF50), Color(0xFF2196F3))
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = userData?.name ?: currentUser?.email?.split("@")?.first() ?: "User",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentUser?.email ?: "",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }

                // Navigation Items
                menuItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.title) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(item.route)
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Logout Button
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Logout, contentDescription = null) },
                    label = { Text("Logout", color = Color.Red) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        authViewModel.logout()
                        navController.navigate(Routes.AUTH_SCREEN) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedTextColor = Color.Red,
                        unselectedTextColor = Color.Red
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Welcome back!",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                userData?.name ?: "User",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Quick Actions Grid
                item {
                    Text(
                        text = "Quick Actions",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        QuickActionGridCard(
                            modifier = Modifier.weight(1f),
                            title = "Book Appointment",
                            icon = Icons.Default.CalendarToday,
                            color = Color(0xFF4CAF50),
                            onClick = { navController.navigate(Routes.BOOK_APPOINTMENT) }
                        )
                        QuickActionGridCard(
                            modifier = Modifier.weight(1f),
                            title = "Emergency",
                            icon = Icons.Default.Warning,
                            color = Color.Red,
                            onClick = { navController.navigate(Routes.EMERGENCY) }
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        QuickActionGridCard(
                            modifier = Modifier.weight(1f),
                            title = "Yoga",
                            icon = Icons.Default.FitnessCenter,
                            color = Color(0xFFFF9800),
                            onClick = { navController.navigate(Routes.YOGA_EXERCISES) }
                        )
                        QuickActionGridCard(
                            modifier = Modifier.weight(1f),
                            title = "Prescriptions",
                            icon = Icons.Default.Medication,
                            color = Color(0xFF9C27B0),
                            onClick = { navController.navigate(Routes.USER_PRESCRIPTIONS) }
                        )
                    }
                }

                // Upcoming Appointments
                if (upcomingAppointments.isNotEmpty()) {
                    item {
                        Text(
                            text = "Upcoming Appointments",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                        )
                    }

                    items(upcomingAppointments) { appointment ->
                        AppointmentCard(appointment = appointment)
                    }
                }

                // Recent Prescriptions
                if (recentPrescriptions.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recent Prescriptions",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                        )
                    }

                    items(recentPrescriptions) { prescription ->
                        PrescriptionPreviewCard(
                            prescription = prescription,
                            onClick = { navController.navigate(Routes.USER_PRESCRIPTIONS) }
                        )
                    }
                }
            }
        }
    }
}

data class DashboardMenuItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun QuickActionGridCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = color,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun AppointmentCard(appointment: Appointment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
                    .clip(CircleShape)
                    .background(Color(0xFF2196F3).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = Color(0xFF2196F3)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.doctorName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = appointment.doctorSpecialty,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "${appointment.date} at ${appointment.time}",
                    fontSize = 12.sp,
                    color = Color(0xFF2196F3)
                )
            }

            Surface(
                shape = CircleShape,
                color = when (appointment.status) {
                    "pending" -> Color.Yellow.copy(alpha = 0.2f)
                    "confirmed" -> Color.Green.copy(alpha = 0.2f)
                    else -> Color.Gray.copy(alpha = 0.2f)
                }
            ) {
                Text(
                    text = when (appointment.status) {
                        "pending" -> "Pending"
                        "confirmed" -> "Confirmed"
                        "completed" -> "Completed"
                        else -> "Cancelled"
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (appointment.status) {
                        "pending" -> Color.Yellow
                        "confirmed" -> Color.Green
                        else -> Color.Gray
                    }
                )
            }
        }
    }
}

@Composable
fun PrescriptionPreviewCard(
    prescription: Prescription,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
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
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Medication,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Prescription from Dr. ${prescription.doctorName}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Text(
                    text = "${prescription.medicines.size} medicines • ${java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(prescription.date))}",
                    fontSize = 12.sp,
                    color = Color.Gray
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