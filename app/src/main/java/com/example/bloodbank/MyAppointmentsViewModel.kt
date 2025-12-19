package com.example.bloodbank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloodbank.Model.DonationAppointment
import com.example.bloodbank.repository.DonationAppointmentRepository
import com.example.bloodbank.repository.Result
import com.example.bloodbank.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyAppointmentsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val donationAppointmentRepository: DonationAppointmentRepository
) : ViewModel() {

    private val _appointments = MutableStateFlow<List<DonationAppointment>>(emptyList())
    val appointments: StateFlow<List<DonationAppointment>> = _appointments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error.asSharedFlow()

    init {
        loadAppointments()
    }

    fun loadAppointments() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = userRepository.getCurrentUserUid()

            if (userId == null) {
                _error.emit("User not logged in.")
                _isLoading.value = false
                return@launch
            }

            donationAppointmentRepository.getAppointmentsForUser(userId)
                .catch { e ->
                    _error.emit("Failed to load appointments: ${e.message}")
                    _isLoading.value = false
                }
                .collectLatest { appointmentList ->
                    _appointments.value = appointmentList.sortedByDescending { it.appointmentDate }
                    _isLoading.value = false
                }
        }
    }

    fun updateAppointmentStatus(appointmentId: String, status: String) {
        viewModelScope.launch {
            _isLoading.value = true
            donationAppointmentRepository.updateAppointmentStatus(appointmentId, status)
                .collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            // Status updated successfully, appointments flow will automatically refresh
                            _isLoading.value = false
                        }
                        is Result.Error -> {
                            _error.emit("Failed to update appointment status: ${result.exception.message}")
                            _isLoading.value = false
                        }
                    }
                }
        }
    }

    /**
     * Clear the current error message.
     */
    fun clearError() {
        // SharedFlow doesn't hold state, but we can provide this for consistency
        // with other ViewModels if needed, or simply do nothing as SharedFlow
        // events are "fire and forget".
    }
}