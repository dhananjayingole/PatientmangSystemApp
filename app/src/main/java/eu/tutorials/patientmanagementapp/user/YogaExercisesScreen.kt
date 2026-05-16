package eu.tutorials.patientmanagementapp.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import eu.tutorials.patientmanagementapp.Model.Pose
import eu.tutorials.patientmanagementapp.Navigation.Routes
import eu.tutorials.patientmanagementapp.viewmodels.NetworkResponse
import eu.tutorials.patientmanagementapp.viewmodels.YogaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YogaExercisesScreen(
    navController: NavController,
    yogaViewModel: YogaViewModel = viewModel()
) {
    val yogaResult by yogaViewModel.allPoses.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yoga & Exercises", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
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
            when (val result = yogaResult) {
                is NetworkResponse.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is NetworkResponse.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Flatten all poses from all categories

                        items(result.data) { pose ->
                            YogaExerciseCard(
                                pose = pose,
                                onClick = {
                                    navController.navigate("${Routes.YOGA_DETAIL}/${pose.id}")
                                }
                            )
                        }
                    }
                }
                is NetworkResponse.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = result.message,
                                color = Color.Red,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { yogaViewModel.fetchYogaData() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                null -> {
                    LaunchedEffect(Unit) {
                        yogaViewModel.fetchAllPoses()                    }
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun YogaExerciseCard(
    pose: Pose,
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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Handle null URL
            if (pose.urlPng != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(pose.urlPng)
                        .crossfade(true)
                        .build(),
                    contentDescription = pose.englishName ?: "Yoga pose",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                // Placeholder for missing image
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pose.englishName ?: "Unknown Pose",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = pose.sanskritName ?: "",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = pose.categoryName ?: "",
                    fontSize = 11.sp,
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