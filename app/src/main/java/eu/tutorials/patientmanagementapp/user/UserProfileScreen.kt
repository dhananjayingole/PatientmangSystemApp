package eu.tutorials.patientmanagementapp.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import eu.tutorials.patientmanagementapp.admin.ProfileInfoRow
import eu.tutorials.patientmanagementapp.auth.AuthViewModel
import eu.tutorials.patientmanagementapp.viewmodels.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val userData by userViewModel.userData.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(userData?.name ?: "") }
    var editedPhone by remember { mutableStateOf(userData?.phoneNumber ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (isEditing) {
                                userViewModel.updateProfile(editedName, editedPhone)
                            }
                            isEditing = !isEditing
                        }
                    ) {
                        Text(if (isEditing) "Save" else "Edit", color = Color.White)
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2196F3).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = Color(0xFF2196F3)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = editedName,
                            onValueChange = { editedName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = editedPhone,
                            onValueChange = { editedPhone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        ProfileInfoRow(
                            icon = Icons.Default.Person,
                            label = "Full Name",
                            value = userData?.name ?: "Not set"
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        ProfileInfoRow(
                            icon = Icons.Default.Email,
                            label = "Email",
                            value = currentUser?.email ?: "Not available"
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        ProfileInfoRow(
                            icon = Icons.Default.Phone,
                            label = "Phone Number",
                            value = userData?.phoneNumber ?: "Not set"
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        ProfileInfoRow(
                            icon = Icons.Default.Info,
                            label = "Member Since",
                            value = userData?.createdAt?.let {
                                java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                                    .format(java.util.Date(it))
                            } ?: "Not available"
                        )
                    }
                }
            }
        }
    }
}