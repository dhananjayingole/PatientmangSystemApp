package eu.tutorials.patientmanagementapp.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import eu.tutorials.patientmanagementapp.Model.Notification
import eu.tutorials.patientmanagementapp.viewmodels.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel()
) {
    val notifications by userViewModel.notifications.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { userViewModel.markAllNotificationsRead() }
                    ) {
                        Text("Mark all read", color = Color.White, fontSize = 12.sp)
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
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.NotificationsOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No notifications", fontSize = 16.sp, color = Color.Gray)
                        Text("You're all caught up!", fontSize = 14.sp, color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notifications) { notification ->
                        NotificationCard(
                            notification = notification,
                            onMarkRead = { userViewModel.markNotificationRead(notification.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: Notification,
    onMarkRead: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(if (notification.isRead) 1.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) Color(0xFFFAFAFA) else Color(0xFFE3F2FD)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Icon based on notification type
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(
                        when (notification.type) {
                            "emergency" -> Color.Red.copy(alpha = 0.2f)
                            "appointment" -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                            "prescription" -> Color(0xFF2196F3).copy(alpha = 0.2f)
                            else -> Color.Gray.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (notification.type) {
                        "emergency" -> Icons.Default.Warning
                        "appointment" -> Icons.Default.CalendarToday
                        "prescription" -> Icons.Default.Medication
                        else -> Icons.Default.Notifications
                    },
                    contentDescription = null,
                    tint = when (notification.type) {
                        "emergency" -> Color.Red
                        "appointment" -> Color(0xFF4CAF50)
                        "prescription" -> Color(0xFF2196F3)
                        else -> Color.Gray
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = notification.message,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 2
                )
                Text(
                    text = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(notification.timestamp)),
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }

            if (!notification.isRead) {
                Button(
                    onClick = onMarkRead,
                    modifier = Modifier.align(Alignment.Top),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3).copy(alpha = 0.1f)),
                    elevation = null
                ) {
                    Text("Mark read", fontSize = 11.sp, color = Color(0xFF2196F3))
                }
            }
        }
    }
}