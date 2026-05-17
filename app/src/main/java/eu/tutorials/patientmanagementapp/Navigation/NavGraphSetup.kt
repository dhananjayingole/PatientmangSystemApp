package eu.tutorials.patientmanagementapp.Navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.google.android.gms.location.FusedLocationProviderClient
import eu.tutorials.patientmanagementapp.admin.*
import eu.tutorials.patientmanagementapp.auth.*
import eu.tutorials.patientmanagementapp.user.*
import eu.tutorials.patientmanagementapp.viewmodels.AdminViewModel
import eu.tutorials.patientmanagementapp.viewmodels.UserViewModel

@Composable
fun NavGraphSetup(
    navController: NavHostController,
    fusedLocationClient: FusedLocationProviderClient
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context.applicationContext))

    // FIX: Create shared ViewModels at NavGraph scope so all screens share the same instance.
    // Without this, getPatientById() / getPrescriptionById() return null in edit screens
    // because each composable was creating its own separate ViewModel instance.
    val adminViewModel: AdminViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()

    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val userRole by authViewModel.userRole.collectAsState()

    // FIX: Auth race condition — show a spinner while role is still being fetched from Firebase.
    // Before this fix, authenticated users would land on AUTH_SCREEN on cold start because
    // userRole was null at the moment startDestination was computed.
    if (isAuthenticated && userRole == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = when {
        !isAuthenticated -> Routes.AUTH_SCREEN
        userRole == "admin" -> Routes.ADMIN_DASHBOARD
        else -> Routes.USER_DASHBOARD
    }

    NavHost(navController = navController, startDestination = startDestination) {

        // ── Auth ──
        composable(Routes.AUTH_SCREEN) {
            AuthScreen(
                onNavigateToAdmin = {
                    navController.navigate(Routes.ADMIN_DASHBOARD) {
                        popUpTo(Routes.AUTH_SCREEN) { inclusive = true }
                    }
                },
                onNavigateToUser = {
                    navController.navigate(Routes.USER_DASHBOARD) {
                        popUpTo(Routes.AUTH_SCREEN) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        // ── Admin Screens (all use shared adminViewModel) ──

        composable(Routes.ADMIN_DASHBOARD) {
            AdminDashboard(
                navController = navController,
                authViewModel = authViewModel,
                adminViewModel = adminViewModel
            )
        }

        composable(Routes.ADMIN_USERS) {
            AdminUsersScreen(
                navController = navController,
                adminViewModel = adminViewModel
            )
        }

        composable(Routes.ADMIN_PATIENTS) {
            AdminPatientsScreen(
                navController = navController,
                adminViewModel = adminViewModel
            )
        }

        composable(
            route = Routes.ADMIN_ADD_EDIT_PATIENT,
            arguments = listOf(navArgument("patientId") { type = NavType.StringType })
        ) { back ->
            AdminAddEditPatientScreen(
                navController = navController,
                patientId = back.arguments?.getString("patientId") ?: "new",
                adminViewModel = adminViewModel
            )
        }

        // FIX: New route — pre-fills patient form from a confirmed appointment
        composable(
            route = Routes.ADMIN_ADD_PATIENT_FROM_APPT,
            arguments = listOf(
                navArgument("userName") { type = NavType.StringType; defaultValue = "" },
                navArgument("userId") { type = NavType.StringType; defaultValue = "" }
            )
        ) { back ->
            AdminAddEditPatientScreen(
                navController = navController,
                patientId = "new",
                prefillName = back.arguments?.getString("userName") ?: "",
                prefillUserId = back.arguments?.getString("userId") ?: "",
                adminViewModel = adminViewModel
            )
        }

        composable(Routes.ADMIN_PRESCRIPTIONS) {
            AdminPrescriptionsScreen(
                navController = navController,
                adminViewModel = adminViewModel
            )
        }

        composable(
            route = Routes.ADMIN_ADD_EDIT_PRESCRIPTION,
            arguments = listOf(
                navArgument("prescriptionId") { type = NavType.StringType },
                navArgument("patientId") { type = NavType.StringType; defaultValue = "" }
            )
        ) { back ->
            AdminAddEditPrescriptionScreen(
                navController = navController,
                prescriptionId = back.arguments?.getString("prescriptionId") ?: "new",
                patientId = back.arguments?.getString("patientId") ?: "",
                adminViewModel = adminViewModel
            )
        }

        composable(Routes.ADMIN_EMERGENCY_ALERTS) {
            AdminEmergencyAlertsScreen(
                navController = navController,
                adminViewModel = adminViewModel
            )
        }

        composable(Routes.ADMIN_APPOINTMENTS) {
            AdminAppointmentsScreen(
                navController = navController,
                adminViewModel = adminViewModel
            )
        }

        composable(Routes.ADMIN_PROFILE) {
            AdminProfileScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // ── User Screens (all use shared userViewModel) ──

        composable(Routes.USER_DASHBOARD) {
            UserDashboard(
                navController = navController,
                authViewModel = authViewModel,
                userViewModel = userViewModel
            )
        }

        composable(Routes.USER_PROFILE) {
            UserProfileScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Routes.USER_PRESCRIPTIONS) {
            UserPrescriptionsScreen(
                navController = navController,
                userViewModel = userViewModel
            )
        }

        composable(Routes.MY_APPOINTMENTS) {
            MyAppointmentsScreen(
                navController = navController,
                userViewModel = userViewModel
            )
        }

        composable(Routes.BOOK_APPOINTMENT) {
            BookAppointmentScreen(
                navController = navController,
                authViewModel = authViewModel,
                userViewModel = userViewModel
            )
        }

        composable(Routes.YOGA_EXERCISES) {
            YogaExercisesScreen(navController = navController)
        }

        composable(route = "${Routes.YOGA_DETAIL}/{poseId}") { backStackEntry ->
            val poseId = backStackEntry.arguments?.getString("poseId")?.toIntOrNull() ?: 0
            YogaDetailScreen(navController = navController, poseId = poseId)
        }

        composable(Routes.EMERGENCY) {
            EmergencyScreen(
                navController = navController,
                fusedLocationClient = fusedLocationClient,
                authViewModel = authViewModel
            )
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                navController = navController,
                userViewModel = userViewModel
            )
        }
    }
}