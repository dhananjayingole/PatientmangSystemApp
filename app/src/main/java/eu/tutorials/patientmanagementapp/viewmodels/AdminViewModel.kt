package eu.tutorials.patientmanagementapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import eu.tutorials.patientmanagementapp.Model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminViewModel : ViewModel() {

    private val db = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _patients = MutableStateFlow<List<Patient>>(emptyList())
    val patients: StateFlow<List<Patient>> = _patients

    private val _prescriptions = MutableStateFlow<List<Prescription>>(emptyList())
    val prescriptions: StateFlow<List<Prescription>> = _prescriptions

    private val _emergencyAlerts = MutableStateFlow<List<EmergencyAlert>>(emptyList())
    val emergencyAlerts: StateFlow<List<EmergencyAlert>> = _emergencyAlerts
    val recentAlerts: StateFlow<List<EmergencyAlert>> = _emergencyAlerts

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments

    private val _stats = MutableStateFlow(AdminStats())
    val stats: StateFlow<AdminStats> = _stats

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    private val listeners = mutableListOf<Pair<Query, ValueEventListener>>()

    init {
        listenUsers()
        listenPatients()
        listenPrescriptions()
        listenEmergencyAlerts()
        listenAppointments()
    }

    // ── Real-time Listeners ──

    private fun listenUsers() {
        val ref = db.child("users")
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                _users.value = snap.children.mapNotNull { it.getValue(User::class.java) }
                updateStats()
            }
            override fun onCancelled(e: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        listeners.add(ref to listener)
    }

    private fun listenPatients() {
        val ref = db.child("patients")
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                _patients.value = snap.children.mapNotNull { it.getValue(Patient::class.java) }
                updateStats()
            }
            override fun onCancelled(e: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        listeners.add(ref to listener)
    }

    private fun listenPrescriptions() {
        val ref = db.child("prescriptions")
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                _prescriptions.value = snap.children
                    .mapNotNull { it.getValue(Prescription::class.java) }
                    .sortedByDescending { it.date }
            }
            override fun onCancelled(e: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        listeners.add(ref to listener)
    }

    private fun listenEmergencyAlerts() {
        val ref = db.child("emergency_alerts")
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                _emergencyAlerts.value = snap.children
                    .mapNotNull { it.getValue(EmergencyAlert::class.java) }
                    .sortedByDescending { it.timestamp }
                updateStats()
            }
            override fun onCancelled(e: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        listeners.add(ref to listener)
    }

    private fun listenAppointments() {
        val ref = db.child("appointments")
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                _appointments.value = snap.children
                    .mapNotNull { it.getValue(Appointment::class.java) }
                    .sortedBy { "${it.date} ${it.time}" }
                updateStats()
            }
            override fun onCancelled(e: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        listeners.add(ref to listener)
    }

    private fun updateStats() {
        val todayStr = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            .format(java.util.Date())
        _stats.value = AdminStats(
            totalUsers = _users.value.size,
            totalPatients = _patients.value.size,
            pendingAlerts = _emergencyAlerts.value.count { it.status == "pending" },
            totalAppointments = _appointments.value.size,
            todayAppointments = _appointments.value.count { it.date == todayStr }
        )
    }

    // ── Patient CRUD ──

    fun savePatient(patient: Patient, isNew: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val id = if (isNew) db.child("patients").push().key ?: return@launch
                else patient.id
                val toSave = patient.copy(id = id, updatedAt = System.currentTimeMillis())
                db.child("patients").child(id).setValue(toSave).await()
                _toastMessage.value = if (isNew) "Patient added successfully!" else "Patient updated!"
            } catch (e: Exception) {
                _toastMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePatient(patientId: String) {
        viewModelScope.launch {
            try {
                db.child("patients").child(patientId).removeValue().await()
                _toastMessage.value = "Patient deleted"
            } catch (e: Exception) {
                _toastMessage.value = "Error: ${e.message}"
            }
        }
    }

    // ── Prescription CRUD ──

    // FIX: savePrescription now looks up patient.userId and stores it as prescription.userId.
    // This is the key fix that allows UserViewModel to query prescriptions by Firebase Auth UID.
    // Previously prescriptions only stored patientId (a push key), which can never match
    // the uid used in the user-side query.
    fun savePrescription(prescription: Prescription, isNew: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val id = if (isNew) db.child("prescriptions").push().key ?: return@launch
                else prescription.id

                // Look up the linked Firebase Auth UID from the patient record
                val patient = _patients.value.find { it.id == prescription.patientId }
                val linkedUserId = patient?.userId ?: ""

                val toSave = prescription.copy(
                    id = id,
                    date = System.currentTimeMillis(),
                    userId = linkedUserId   // FIX: store Auth UID so user can query their own prescriptions
                )
                db.child("prescriptions").child(id).setValue(toSave).await()

                // Notify patient if their account is linked
                if (linkedUserId.isNotEmpty()) {
                    val notifId = db.child("notifications").push().key ?: ""
                    if (notifId.isNotEmpty()) {
                        val notif = Notification(
                            id = notifId,
                            userId = linkedUserId,
                            title = if (isNew) "New Prescription" else "Prescription Updated",
                            message = "Dr. ${prescription.doctorName} has " +
                                    "${if (isNew) "issued" else "updated"} a prescription for you. " +
                                    "${prescription.medicines.size} medicine(s) prescribed.",
                            type = "prescription",
                            timestamp = System.currentTimeMillis()
                        )
                        db.child("notifications").child(notifId).setValue(notif).await()
                    }
                }
                _toastMessage.value = if (isNew) "Prescription saved!" else "Prescription updated!"
            } catch (e: Exception) {
                _toastMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePrescription(prescriptionId: String) {
        viewModelScope.launch {
            try {
                db.child("prescriptions").child(prescriptionId).removeValue().await()
                _toastMessage.value = "Prescription deleted"
            } catch (e: Exception) {
                _toastMessage.value = "Error: ${e.message}"
            }
        }
    }

    // ── User Management ──

    fun deleteUser(uid: String) {
        viewModelScope.launch {
            try {
                db.child("users").child(uid).removeValue().await()
                _toastMessage.value = "User deleted"
            } catch (e: Exception) {
                _toastMessage.value = "Error: ${e.message}"
            }
        }
    }

    // ── Emergency Alerts ──

    fun updateEmergencyStatus(alertId: String, status: String) {
        viewModelScope.launch {
            try {
                val respondedBy = auth.currentUser?.email ?: "admin"
                val updates = mapOf(
                    "status" to status,
                    "respondedBy" to respondedBy,
                    "responseTime" to System.currentTimeMillis()
                )
                db.child("emergency_alerts").child(alertId).updateChildren(updates).await()

                val alert = _emergencyAlerts.value.find { it.id == alertId }
                if (alert != null && alert.userId.isNotEmpty()) {
                    val notifId = db.child("notifications").push().key ?: ""
                    if (notifId.isNotEmpty()) {
                        val message = when (status) {
                            "responding" -> "Help is on the way! Emergency team is responding to your SOS."
                            "resolved"   -> "Your emergency has been resolved. Stay safe."
                            else         -> "Emergency status updated to $status"
                        }
                        val notif = Notification(
                            id = notifId,
                            userId = alert.userId,
                            title = "Emergency Update",
                            message = message,
                            type = "emergency",
                            timestamp = System.currentTimeMillis()
                        )
                        db.child("notifications").child(notifId).setValue(notif).await()
                    }
                }
                _toastMessage.value = "Status updated to $status"
            } catch (e: Exception) {
                _toastMessage.value = "Error: ${e.message}"
            }
        }
    }

    // ── Appointments ──

    fun updateAppointmentStatus(appointmentId: String, status: String, notes: String = "") {
        viewModelScope.launch {
            try {
                db.child("appointments").child(appointmentId).child("status").setValue(status).await()
                if (notes.isNotEmpty()) {
                    db.child("appointments").child(appointmentId).child("notes").setValue(notes).await()
                }

                val appt = _appointments.value.find { it.id == appointmentId }
                if (appt != null && appt.userId.isNotEmpty()) {
                    val notifId = db.child("notifications").push().key ?: ""
                    if (notifId.isNotEmpty()) {
                        val message = when (status) {
                            "confirmed"  -> "Your appointment with ${appt.doctorName} on ${appt.date} at ${appt.time} is confirmed.${if (notes.isNotEmpty()) " Note: $notes" else ""}"
                            "cancelled"  -> "Your appointment with ${appt.doctorName} on ${appt.date} has been cancelled.${if (notes.isNotEmpty()) " Reason: $notes" else ""}"
                            "completed"  -> "Your appointment with ${appt.doctorName} is marked as completed."
                            else         -> "Appointment status updated to $status"
                        }
                        val notif = Notification(
                            id = notifId,
                            userId = appt.userId,
                            title = "Appointment ${status.replaceFirstChar { it.uppercase() }}",
                            message = message,
                            type = "appointment",
                            timestamp = System.currentTimeMillis()
                        )
                        db.child("notifications").child(notifId).setValue(notif).await()
                    }
                }
                _toastMessage.value = "Appointment $status"
            } catch (e: Exception) {
                _toastMessage.value = "Error: ${e.message}"
            }
        }
    }

    // ── Helpers ──

    fun getPatientById(id: String) = _patients.value.find { it.id == id }
    fun getPrescriptionById(id: String) = _prescriptions.value.find { it.id == id }
    fun clearToast() { _toastMessage.value = null }

    override fun onCleared() {
        super.onCleared()
        listeners.forEach { (query, listener) -> query.removeEventListener(listener) }
    }
}

data class AdminStats(
    val totalUsers: Int = 0,
    val totalPatients: Int = 0,
    val pendingAlerts: Int = 0,
    val totalAppointments: Int = 0,
    val todayAppointments: Int = 0
)