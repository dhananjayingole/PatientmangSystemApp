package eu.tutorials.patientmanagementapp.Model

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "user", // "admin" or "user"
    val phoneNumber: String = "",
    val profileImageUrl: String = "",
    val fcmToken: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastLogin: Long = System.currentTimeMillis()
)

enum class UserRole {
    ADMIN, USER
}