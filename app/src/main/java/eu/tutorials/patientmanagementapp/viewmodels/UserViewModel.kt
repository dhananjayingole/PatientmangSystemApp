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

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    val unreadCount: StateFlow<Int> = _notifications
        .map { list -> list.count { !it.isRead } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val listeners = mutableListOf<Pair<Query, ValueEventListener>>()

    init {
        if (uid.isNotEmpty()) {
            fetchUserData()
            listenPrescriptions()
            listenAppointments()
            listenNotifications()
        }
    }

    fun fetchUserData() {
        viewModelScope.launch {
            try {
                val snap = db.child("users").child(uid).get().await()
                _userData.value = snap.getValue(User::class.java)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    private fun listenPrescriptions() {
        if (uid.isEmpty()) return
        val ref = db.child("prescriptions").orderByChild("patientId").equalTo(uid)
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
        if (uid.isEmpty()) return
        val ref = db.child("appointments").orderByChild("userId").equalTo(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                _upcomingAppointments.value = snap.children
                    .mapNotNull { it.getValue(Appointment::class.java) }
                    .filter { it.status == "pending" || it.status == "confirmed" }
                    .sortedBy { "${it.date} ${it.time}" }
            }
            override fun onCancelled(e: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        listeners.add(ref to listener)
    }

    private fun listenNotifications() {
        if (uid.isEmpty()) return
        val ref = db.child("notifications").orderByChild("userId").equalTo(uid)
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
        viewModelScope.launch {
            _isLoading.value = true
            try {
                db.child("users").child(uid).child("name").setValue(name).await()
                db.child("users").child(uid).child("phoneNumber").setValue(phone).await()
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
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val apptId = db.child("appointments").push().key ?: return@launch
                val appt = Appointment(
                    id = apptId,
                    userId = uid,
                    userName = userName,
                    doctorName = doctorName,
                    doctorSpecialty = specialty,
                    date = date,
                    time = time,
                    reason = reason
                )
                db.child("appointments").child(apptId).setValue(appt).await()

                // Self notification
                try {
                    pushNotification(
                        userId = uid,
                        title = "Appointment Requested",
                        message = "Your appointment with $doctorName on $date at $time has been submitted. Awaiting confirmation.",
                        type = "appointment"
                    )
                } catch(e: Exception){
//                    log but don't fail the booking.
                }
                _toastMessage.value = "Appointment booked!"
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

                // Confirm to user
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

    override fun onCleared() {
        super.onCleared()
        listeners.forEach { (query, listener) -> query.removeEventListener(listener) }
    }
}