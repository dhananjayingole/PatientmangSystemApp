package eu.tutorials.patientmanagementapp.Model

data class Patient(
    val id: String = "",
    val name: String = "",
    val age: Int = 0,
    val gender: String = "",
    val bloodGroup: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val emergencyContact: String = "",
    val assignedDoctor: String = "",
    val medicalHistory: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val userId: String = "" // Linked to user account
)