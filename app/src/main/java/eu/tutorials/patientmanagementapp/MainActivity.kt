package eu.tutorials.patientmanagementapp

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.FirebaseApp
import eu.tutorials.patientmanagementapp.Navigation.NavGraphSetup
import eu.tutorials.patientmanagementapp.ui.theme.PatientManagementAppTheme

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        FirebaseApp.initializeApp(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Request location permissions
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            1001
        )

        setContent {
            PatientManagementAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PatientManagementApp(fusedLocationClient)
                }
            }
        }
    }
}

@Composable
fun PatientManagementApp(fusedLocationClient: FusedLocationProviderClient) {
    val navController = rememberNavController()
    NavGraphSetup(navController = navController, fusedLocationClient = fusedLocationClient)
}