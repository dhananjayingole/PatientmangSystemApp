package eu.tutorials.patientmanagementapp.Model

data class Appointment(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val doctorName: String = "",
    val doctorSpecialty: String = "",
    val date: String = "",
    val time: String = "",
    val reason: String = "",
    val status: String = "pending", // pending, confirmed, completed, cancelled,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)