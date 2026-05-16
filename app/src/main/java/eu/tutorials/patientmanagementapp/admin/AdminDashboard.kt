package eu.tutorials.patientmanagementapp.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import eu.tutorials.patientmanagementapp.Model.EmergencyAlert
import eu.tutorials.patientmanagementapp.Navigation.Routes
import eu.tutorials.patientmanagementapp.auth.AuthViewModel
import eu.tutorials.patientmanagementapp.viewmodels.AdminViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    navController: NavController,
    authViewModel: AuthViewModel,
    adminViewModel: AdminViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val stats by adminViewModel.stats.collectAsState()
    val recentAlerts by adminViewModel.recentAlerts.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val toast by adminViewModel.toastMessage.collectAsState()

    LaunchedEffect(toast) { toast?.let { adminViewModel.clearToast() } }

    val menuItems = listOf(
        MenuItem("Appointments", "Today's schedule", Icons.Default.CalendarToday, Routes.ADMIN_APPOINTMENTS),
        MenuItem("Patients", "Manage patients", Icons.Default.Folder, Routes.ADMIN_PATIENTS),
        MenuItem("Prescriptions", "Manage prescriptions", Icons.Default.Medication, Routes.ADMIN_PRESCRIPTIONS),
        MenuItem("Users", "Manage all users", Icons.Default.People, Routes.ADMIN_USERS),
        MenuItem("Emergency Alerts", "SOS alerts", Icons.Default.Warning, Routes.ADMIN_EMERGENCY_ALERTS),
        MenuItem("Profile", "Admin profile", Icons.Default.Person, Routes.ADMIN_PROFILE)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(Color(0xFF1A237E), Color(0xFF0D47A1))))
                        .padding(24.dp)
                ) {
                    Column {
                        Icon(Icons.Default.AdminPanelSettings, null, Modifier.size(48.dp), tint = Color.White)
                        Spacer(Modifier.height(8.dp))
                        Text(currentUser?.email?.split("@")?.first() ?: "Admin", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Administrator", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                menuItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, null) },
                        label = { Text(item.title) },
                        selected = false,
                        onClick = { scope.launch { drawerState.close() }; navController.navigate(item.route) },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Logout, null, tint = Color.Red) },
                    label = { Text("Logout", color = Color.Red) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        authViewModel.logout()
                        navController.navigate(Routes.AUTH_SCREEN) { popUpTo(0) { inclusive = true } }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, "Menu")
                        }
                    },
                    actions = {
                        if (stats.pendingAlerts > 0) {
                            BadgedBox(badge = { Badge { Text(stats.pendingAlerts.toString()) } }) {
                                IconButton(onClick = { navController.navigate(Routes.ADMIN_EMERGENCY_ALERTS) }) {
                                    Icon(Icons.Default.Warning, "Alerts", tint = Color.White)
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1976D2),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Stats Row 1
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(Modifier.weight(1f), "Total Patients", stats.totalPatients.toString(), Icons.Default.Folder, Color(0xFF2196F3))
                        StatCard(Modifier.weight(1f), "Today's Appts", stats.todayAppointments.toString(), Icons.Default.CalendarToday, Color(0xFF9C27B0))
                    }
                }
                // Stats Row 2
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(Modifier.weight(1f), "SOS Pending", stats.pendingAlerts.toString(), Icons.Default.Warning, Color(0xFFFF5722))
                        StatCard(Modifier.weight(1f), "Total Users", stats.totalUsers.toString(), Icons.Default.People, Color(0xFF4CAF50))
                    }
                }

                // Quick Actions
                item {
                    Text("Quick Actions", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuickActionCard(Modifier.weight(1f), "Add Patient", Icons.Default.PersonAdd, Color(0xFF4CAF50)) {
                            navController.navigate(Routes.adminAddEditPatient())
                        }
                        QuickActionCard(Modifier.weight(1f), "Appointments", Icons.Default.CalendarToday, Color(0xFF9C27B0)) {
                            navController.navigate(Routes.ADMIN_APPOINTMENTS)
                        }
                        QuickActionCard(Modifier.weight(1f), "Prescriptions", Icons.Default.Medication, Color(0xFF2196F3)) {
                            navController.navigate(Routes.ADMIN_PRESCRIPTIONS)
                        }
                    }
                }

                // Recent Emergency Alerts
                if (recentAlerts.isNotEmpty()) {
                    item { Text("Recent SOS Alerts", fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                    items(minOf(recentAlerts.size, 3)) { i ->
                        EmergencyAlertCard(recentAlerts[i]) { navController.navigate(Routes.ADMIN_EMERGENCY_ALERTS) }
                    }
                }
            }
        }
    }
}

data class MenuItem(val title: String, val subtitle: String, val icon: ImageVector, val route: String)

@Composable
fun StatCard(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Card(modifier, shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(4.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
                Text(title, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(icon, null, Modifier.size(36.dp), tint = color.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun QuickActionCard(modifier: Modifier, title: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(modifier.clickable(onClick = onClick), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(color.copy(alpha = 0.1f))) {
        Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, Modifier.size(36.dp), tint = color)
            Spacer(Modifier.height(6.dp))
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = color,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
fun EmergencyAlertCard(alert: EmergencyAlert, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, null, Modifier.size(32.dp), tint = Color.Red)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(alert.userName, fontWeight = FontWeight.Bold)
                Text(alert.address.take(50), fontSize = 12.sp, color = Color.Gray)
            }
            Text(
                when(alert.status) { "pending" -> "PENDING"; "responding" -> "RESPONDING"; else -> "RESOLVED" },
                color = when(alert.status) { "pending" -> Color.Red; "responding" -> Color(0xFFFFC107); else -> Color.Green },
                fontSize = 11.sp, fontWeight = FontWeight.Bold
            )
        }
    }
}