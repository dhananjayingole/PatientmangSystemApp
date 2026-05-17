package eu.tutorials.patientmanagementapp.Model

data class Prescription(
    val id: String = "",
    val patientId: String = "",
    val userId: String = "",          // FIX: Firebase Auth UID — used for user-side queries
    val patientName: String = "",
    val doctorName: String = "",
    val date: Long = System.currentTimeMillis(),
    val medicines: List<Medicine> = emptyList(),
    val diagnosis: String = "",
    val notes: String = "",
    val followUpDate: Long = 0,
    val createdBy: String = ""
)

data class Medicine(
    val name: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val duration: String = "",
    val timing: String = ""  // e.g. "Before Meals", "After Meals"
)