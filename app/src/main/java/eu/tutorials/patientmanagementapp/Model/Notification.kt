package eu.tutorials.patientmanagementapp.Model

data class Notification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "", // appointment, emergency, prescription, general
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)