package eu.tutorials.patientmanagementapp.Navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.google.android.gms.location.FusedLocationProviderClient
import eu.tutorials.patientmanagementapp.Navigation.Routes
import eu.tutorials.patientmanagementapp.admin.*
import eu.tutorials.patientmanagementapp.auth.*
import eu.tutorials.patientmanagementapp.user.*

@Composable
fun NavGraphSetup(
    navController: NavHostController,
    fusedLocationClient: FusedLocationProviderClient
) {
    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context.applicationContext))
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val userRole by authViewModel.userRole.collectAsState()

    val startDestination = if (isAuthenticated) {
        when (userRole) {
            "admin" -> Routes.ADMIN_DASHBOARD
            "user" -> Routes.USER_DASHBOARD
            else -> Routes.AUTH_SCREEN
        }
    } else Routes.AUTH_SCREEN

    NavHost(navController = navController, startDestination = startDestination) {

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

        // ── Admin Screens ──
        composable(Routes.ADMIN_DASHBOARD) {
            AdminDashboard(navController = navController, authViewModel = authViewModel)
        }
        composable(Routes.ADMIN_USERS) {
            AdminUsersScreen(navController = navController)
        }
        composable(Routes.ADMIN_PATIENTS) {
            AdminPatientsScreen(navController = navController)
        }
        composable(
            route = Routes.ADMIN_ADD_EDIT_PATIENT,
            arguments = listOf(navArgument("patientId") { type = NavType.StringType })
        ) { back ->
            AdminAddEditPatientScreen(
                navController = navController,
                patientId = back.arguments?.getString("patientId") ?: "new"
            )
        }
        composable(Routes.ADMIN_PRESCRIPTIONS) {
            AdminPrescriptionsScreen(navController = navController)
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
                patientId = back.arguments?.getString("patientId") ?: ""
            )
        }
        composable(Routes.ADMIN_EMERGENCY_ALERTS) {
            AdminEmergencyAlertsScreen(navController = navController)
        }
        composable(Routes.ADMIN_APPOINTMENTS) {
            AdminAppointmentsScreen(navController = navController)
        }
        composable(Routes.ADMIN_PROFILE) {
            AdminProfileScreen(navController = navController, authViewModel = authViewModel)
        }

        // ── User Screens ──
        composable(Routes.USER_DASHBOARD) {
            UserDashboard(navController = navController, authViewModel = authViewModel)
        }
        composable(Routes.USER_PROFILE) {
            UserProfileScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(Routes.USER_PRESCRIPTIONS) {
            UserPrescriptionsScreen(navController = navController)
        }
        composable(Routes.BOOK_APPOINTMENT) {
            BookAppointmentScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(Routes.YOGA_EXERCISES) {
            YogaExercisesScreen(navController = navController)
        }
        composable(
            route = "${Routes.YOGA_DETAIL}/{poseId}"
        ) { backStackEntry ->

            val poseId =
                backStackEntry.arguments?.getString("poseId")?.toIntOrNull() ?: 0

            YogaDetailScreen(
                navController = navController,
                poseId = poseId
            )
        }
        composable(Routes.EMERGENCY) {
            EmergencyScreen(
                navController = navController,
                fusedLocationClient = fusedLocationClient,
                authViewModel = authViewModel
            )
        }
        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(navController = navController)
        }
    }
}