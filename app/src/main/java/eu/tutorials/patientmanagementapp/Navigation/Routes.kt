package eu.tutorials.patientmanagementapp.Navigation

object Routes {
    const val AUTH_SCREEN = "auth_screen"

    // Admin
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val ADMIN_USERS = "admin_users"
    const val ADMIN_PATIENTS = "admin_patients"
    const val ADMIN_ADD_EDIT_PATIENT = "admin_add_edit_patient/{patientId}"
    const val ADMIN_PRESCRIPTIONS = "admin_prescriptions"
    const val ADMIN_ADD_EDIT_PRESCRIPTION = "admin_add_edit_prescription/{prescriptionId}/{patientId}"
    const val ADMIN_EMERGENCY_ALERTS = "admin_emergency_alerts"
    const val ADMIN_APPOINTMENTS = "admin_appointments"
    const val ADMIN_PROFILE = "admin_profile"

    // User
    const val USER_DASHBOARD = "user_dashboard"
    const val USER_PROFILE = "user_profile"
    const val USER_PRESCRIPTIONS = "user_prescriptions"
    const val BOOK_APPOINTMENT = "book_appointment"
    const val YOGA_EXERCISES = "yoga_exercises"
    const val YOGA_DETAIL = "yoga_detail"
    const val EMERGENCY = "emergency"
    const val NOTIFICATIONS = "notifications"

    fun adminAddEditPatient(patientId: String = "new") = "admin_add_edit_patient/$patientId"
    fun adminAddEditPrescription(prescriptionId: String = "new", patientId: String = "") =
        "admin_add_edit_prescription/$prescriptionId/$patientId"

    fun yogaDetail(poseId: Int) = "yoga_detail/$poseId"
}