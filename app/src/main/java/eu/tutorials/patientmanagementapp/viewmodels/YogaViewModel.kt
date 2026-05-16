package eu.tutorials.patientmanagementapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.tutorials.patientmanagementapp.Model.Category
import eu.tutorials.patientmanagementapp.Model.Pose
import eu.tutorials.patientmanagementapp.api.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class YogaViewModel : ViewModel() {
    private val _yogaResult = MutableStateFlow<NetworkResponse<List<Category>>?>(null)
    val yogaResult: StateFlow<NetworkResponse<List<Category>>?> = _yogaResult

    private val _allPoses = MutableStateFlow<NetworkResponse<List<Pose>>?>(null)
    val allPoses: StateFlow<NetworkResponse<List<Pose>>?> = _allPoses

    fun fetchYogaData() {
        viewModelScope.launch {
            _yogaResult.value = NetworkResponse.Loading
            try {
                val response = RetrofitInstance.yogaApi.getCategories()
                if (response.isSuccessful) {
                    response.body()?.let { categories ->
                        _yogaResult.value = NetworkResponse.Success(categories)
                    } ?: run {
                        _yogaResult.value = NetworkResponse.Error("Response body is empty")
                    }
                } else {
                    _yogaResult.value = NetworkResponse.Error("Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _yogaResult.value = NetworkResponse.Error("Network error: ${e.localizedMessage}")
            }
        }
    }

    // Alternative: Fetch all poses directly
    fun fetchAllPoses() {
        viewModelScope.launch {
            _allPoses.value = NetworkResponse.Loading
            try {
                val response = RetrofitInstance.yogaApi.getAllPoses()
                if (response.isSuccessful) {
                    response.body()?.let { poses ->
                        _allPoses.value = NetworkResponse.Success(poses)
                    } ?: run {
                        _allPoses.value = NetworkResponse.Error("Response body is empty")
                    }
                } else {
                    _allPoses.value = NetworkResponse.Error("Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _allPoses.value = NetworkResponse.Error("Network error: ${e.localizedMessage}")
            }
        }
    }
}