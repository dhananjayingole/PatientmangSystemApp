package eu.tutorials.patientmanagementapp.Model

data class EmergencyAlert(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "pending", // pending, responding, resolved
    val respondedBy: String = "",
    val responseTime: Long = 0
)