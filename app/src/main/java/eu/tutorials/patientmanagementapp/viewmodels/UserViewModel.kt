package eu.tutorials.patientmanagementapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import eu.tutorials.patientmanagementapp.Model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    // FIX: Use a property that always returns current UID (not captured at init time)
    private val uid get() = auth.currentUser?.uid ?: ""

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData

    private val _prescriptions = MutableStateFlow<List<Prescription>>(emptyList())
    val prescriptions: StateFlow<List<Prescription>> = _prescriptions
    val recentPrescriptions: StateFlow<List<Prescription>> = _prescriptions

    private val _upcomingAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val upcomingAppointments: StateFlow<List<Appointment>> = _upcomingAppointments

    // FIX: Added allAppointments for the new MyAppointmentsScreen (shows history too)
    private val _allAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val allAppointments: StateFlow<List<Appointment>> = _allAppointments

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    val unreadCount: StateFlow<Int> = _notifications
        .map { list -> list.count { !it.isRead } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val listeners = mutableListOf<Pair<Query, ValueEventListener>>()

    // FIX: Use addAuthStateListener instead of checking uid at init time.
    // Previous code captured uid at construction, which was empty on cold start
    // (Firebase Auth hadn't finished initialising). Now we wait for the auth state.
    init {
        auth.addAuthStateListener { firebaseAuth ->
            val currentUid = firebaseAuth.currentUser?.uid ?: ""
            if (currentUid.isNotEmpty()) {
                // Remove old listeners before re-attaching (handles sign-out / sign-in cycles)
                clearListeners()
                fetchUserData()
                listenPrescriptions()
                listenAppointments()
                listenNotifications()
            } else {
                clearListeners()
                _userData.value = null
                _prescriptions.value = emptyList()
                _upcomingAppointments.value = emptyList()
                _allAppointments.value = emptyList()
                _notifications.value = emptyList()
            }
        }
    }

    fun fetchUserData() {
        val currentUid = uid
        if (currentUid.isEmpty()) return
        viewModelScope.launch {
            try {
                val snap = db.child("users").child(currentUid).get().await()
                _userData.value = snap.getValue(User::class.java)
            } catch (e: Exception) {
                // ignore — UI shows cached state
            }
        }
    }

    // FIX: The root cause of "prescriptions never show":
    // Old code queried:  orderByChild("patientId").equalTo(uid)
    // But patientId stores the Patient document ID (a Firebase push key like "-NxAbc123"),
    // NOT the user's Firebase Auth UID.
    // New code queries:  orderByChild("userId").equalTo(uid)
    // This works because AdminViewModel.savePrescription() now stores userId = patient.userId
    // (the Firebase Auth UID) on every prescription document.
    private fun listenPrescriptions() {
        val currentUid = uid
        if (currentUid.isEmpty()) return
        val ref = db.child("prescriptions").orderByChild("userId").equalTo(currentUid)
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

    private fun listenAppointments() {
        val currentUid = uid
        if (currentUid.isEmpty()) return
        val ref = db.child("appointments").orderByChild("userId").equalTo(currentUid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val all = snap.children
                    .mapNotNull { it.getValue(Appointment::class.java) }
                    .sortedByDescending { it.createdAt }

                // allAppointments: full history including past/cancelled
                _allAppointments.value = all

                // upcomingAppointments: only active ones for dashboard display
                _upcomingAppointments.value = all
                    .filter { it.status == "pending" || it.status == "confirmed" }
                    .sortedBy { "${it.date} ${it.time}" }
            }
            override fun onCancelled(e: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        listeners.add(ref to listener)
    }

    private fun listenNotifications() {
        val currentUid = uid
        if (currentUid.isEmpty()) return
        val ref = db.child("notifications").orderByChild("userId").equalTo(currentUid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                _notifications.value = snap.children
                    .mapNotNull { it.getValue(Notification::class.java) }
                    .sortedByDescending { it.timestamp }
            }
            override fun onCancelled(e: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        listeners.add(ref to listener)
    }

    fun updateProfile(name: String, phone: String) {
        val currentUid = uid
        if (currentUid.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                db.child("users").child(currentUid).child("name").setValue(name).await()
                db.child("users").child(currentUid).child("phoneNumber").setValue(phone).await()
                fetchUserData()
                _toastMessage.value = "Profile updated!"
            } catch (e: Exception) {
                _toastMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun bookAppointment(
        userName: String,
        doctorName: String,
        specialty: String,
        date: String,
        time: String,
        reason: String
    ) {
        val currentUid = uid
        if (currentUid.isEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val apptId = db.child("appointments").push().key ?: return@launch
                val appt = Appointment(
                    id = apptId,
                    userId = currentUid,
                    userName = userName,
                    doctorName = doctorName,
                    doctorSpecialty = specialty,
                    date = date,
                    time = time,
                    reason = reason
                )
                db.child("appointments").child(apptId).setValue(appt).await()

                try {
                    pushNotification(
                        userId = currentUid,
                        title = "Appointment Requested",
                        message = "Your appointment with $doctorName on $date at $time has been submitted. Awaiting confirmation.",
                        type = "appointment"
                    )
                } catch (e: Exception) {
                    // Notification failure should not fail the booking
                }
                _toastMessage.value = "Appointment booked successfully!"
            } catch (e: Exception) {
                _toastMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendEmergencyAlert(
        userId: String,
        userName: String,
        userPhone: String,
        latitude: Double,
        longitude: Double,
        address: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val alertId = db.child("emergency_alerts").push().key ?: return@launch
                val alert = EmergencyAlert(
                    id = alertId,
                    userId = userId,
                    userName = userName,
                    userPhone = userPhone,
                    latitude = latitude,
                    longitude = longitude,
                    address = address,
                    timestamp = System.currentTimeMillis()
                )
                db.child("emergency_alerts").child(alertId).setValue(alert).await()
                pushNotification(
                    userId = userId,
                    title = "Emergency Alert Sent",
                    message = "Your SOS has been received. Emergency team has been alerted. Stay calm, help is on the way.",
                    type = "emergency"
                )
                _toastMessage.value = "Emergency alert sent!"
            } catch (e: Exception) {
                _toastMessage.value = "Failed to send alert: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun pushNotification(userId: String, title: String, message: String, type: String) {
        val id = db.child("notifications").push().key ?: return
        val notif = Notification(
            id = id,
            userId = userId,
            title = title,
            message = message,
            type = type,
            timestamp = System.currentTimeMillis()
        )
        db.child("notifications").child(id).setValue(notif).await()
    }

    fun markNotificationRead(notificationId: String) {
        db.child("notifications").child(notificationId).child("isRead").setValue(true)
    }

    fun markAllNotificationsRead() {
        _notifications.value.filter { !it.isRead }.forEach { markNotificationRead(it.id) }
    }

    fun clearToast() { _toastMessage.value = null }

    private fun clearListeners() {
        listeners.forEach { (query, listener) -> query.removeEventListener(listener) }
        listeners.clear()
    }

    override fun onCleared() {
        super.onCleared()
        clearListeners()
    }
}