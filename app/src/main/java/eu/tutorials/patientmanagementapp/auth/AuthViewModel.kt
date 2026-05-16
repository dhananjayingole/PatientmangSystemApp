package eu.tutorials.patientmanagementapp.auth

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import eu.tutorials.patientmanagementapp.Model.User
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(private val context: Context) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference("users")

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _shouldNavigate = MutableStateFlow(false)
    val shouldNavigate: StateFlow<Boolean> = _shouldNavigate

    init {
        auth.currentUser?.let { user ->
            _currentUser.value = user
            _isAuthenticated.value = true
            checkUserRole()
        }
    }

    private fun checkUserRole() {
        auth.currentUser?.uid?.let { uid ->
            viewModelScope.launch {
                try {
                    val snap = database.child(uid).get().await()
                    val role = snap.getValue(User::class.java)?.role ?: "user"
                    _userRole.value = role
                    _shouldNavigate.value = true
                } catch (e: Exception) {
                    _userRole.value = "user"
                    _shouldNavigate.value = true
                }
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) { _errorMessage.value = "Fill all fields"; return }
        viewModelScope.launch {
            _isLoading.value = true
            _shouldNavigate.value = false
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                result.user?.let { user ->
                    _currentUser.value = user
                    _isAuthenticated.value = true
                    val snap = database.child(user.uid).get().await()
                    val role = snap.getValue(User::class.java)?.role ?: "user"
                    _userRole.value = role
                    updateFcmToken(user.uid)
                    database.child(user.uid).child("lastLogin").setValue(System.currentTimeMillis())
                    _shouldNavigate.value = true
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Login failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signUp(email: String, password: String, confirmPassword: String, name: String, phone: String, role: String) {
        when {
            email.isBlank() || password.isBlank() || name.isBlank() -> { _errorMessage.value = "Fill all fields"; return }
            password != confirmPassword -> { _errorMessage.value = "Passwords do not match"; return }
            password.length < 6 -> { _errorMessage.value = "Password must be ≥ 6 chars"; return }
        }
        viewModelScope.launch {
            _isLoading.value = true
            _shouldNavigate.value = false
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                result.user?.let { user ->
                    val fcmToken = try { FirebaseMessaging.getInstance().token.await() } catch (e: Exception) { "" }
                    val newUser = User(uid = user.uid, email = email, name = name, role = role, phoneNumber = phone, fcmToken = fcmToken)
                    database.child(user.uid).setValue(newUser).await()
                    _currentUser.value = user
                    _isAuthenticated.value = true
                    _userRole.value = role
                    _shouldNavigate.value = true
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Sign up failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun updateFcmToken(uid: String) {
        viewModelScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                database.child(uid).child("fcmToken").setValue(token)
            } catch (e: Exception) { /* ignore */ }
        }
    }

    fun logout() {
        auth.signOut()
        _currentUser.value = null
        _userRole.value = null
        _isAuthenticated.value = false
        _shouldNavigate.value = false
    }

    fun clearError() { _errorMessage.value = null }
    fun resetNavigateFlag() { _shouldNavigate.value = false }
    fun isAdmin() = _userRole.value == "admin"
}