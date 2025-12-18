fun createEmergencyRequest(emergencyRequest: EmergencyRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            emergencyRequestRepository.createEmergencyRequest(emergencyRequest).collectLatest { result ->
                _requestCreationResult.value = result
                _isLoading.value = false
                if (result is Result.Error) _error.value = result.exception
            }
        }
    }

    fun loadEmergencyRequest(requestId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            emergencyRequestRepository.getEmergencyRequest(requestId).collectLatest { result ->
                _isLoading.value = false
                when (result) {
                    is Result.Success -> _currentEmergencyRequest.value = result.data
                    is Result.Error -> _error.value = result.exception
                }
            }
        }
    }

    fun loadAllEmergencyRequests() {
        viewModelScope.launch {
            _isLoading.value = true
            emergencyRequestRepository.getAllEmergencyRequests().collectLatest { result ->
                _isLoading.value = false
                when (result) {
                    is Result.Success -> _emergencyRequests.value = result.data
                    is Result.Error -> _error.value = result.exception
                }
            }
        }
    }

    fun loadEmergencyRequestsByRequester() {
        viewModelScope.launch {
            _isLoading.value = true
            val currentUserId = userRepository.getCurrentUserUid()
            if (currentUserId == null) {
                _error.value = Exception("User not logged in.")
                _isLoading.value = false
                return@launch
            }
            emergencyRequestRepository.getEmergencyRequestsByRequester(currentUserId).collectLatest { result ->
                _isLoading.value = false
                when (result) {
                    is Result.Success -> _userEmergencyRequests.value = result.data
                    is Result.Error -> _error.value = result.exception
                }
            }
        }
    }

    fun updateRequestStatus(requestId: String, status: String) {
        viewModelScope.launch {
            _isLoading.value = true
            emergencyRequestRepository.updateEmergencyRequestStatus(requestId, status).collectLatest { result ->
                _statusUpdateResult.value = result
                _isLoading.value = false
                if (result is Result.Error) _error.value = result.exception
            }
        }
    }

    fun addResponse(requestId: String, donorId: String, accepted: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            emergencyRequestRepository.addResponseToEmergencyRequest(requestId, donorId, accepted).collectLatest { result ->
                _addResponseResult.value = result
                _isLoading.value = false
                if (result is Result.Error) _error.value = result.exception
            }
        }
    }

    fun loadEmergencyRequestsForDonorMatching(bloodGroup: String) {
        viewModelScope.launch {
            _isLoading.value = true
            emergencyRequestRepository.getEmergencyRequestsForDonorMatching(bloodGroup).collectLatest { result ->
                _isLoading.value = false
                when (result) {
                    is Result.Success -> _emergencyRequests.value = result.data // Assuming this is for a general list of requests a donor can see
                    is Result.Error -> _error.value = result.exception
                }
            }
        }
    }

    fun resetRequestCreationResult() {
        _requestCreationResult.value = null
    }

    fun resetStatusUpdateResult() {
        _statusUpdateResult.value = null
    }

    fun resetAddResponseResult() {
        _addResponseResult.value = null
    }
}
```

### Step 8: Refactor `EmergencyRequestActivity.java` to `EmergencyRequestActivity.kt` and integrate MVVM

This is a major refactoring that transforms the activity into a lean View, observing the ViewModel.

**Action:** Delete `app/src/main/java/com/example/bloodbank/EmergencyRequestActivity.java` and create `app/src/main/java/com/example/bloodbank/EmergencyRequestActivity.kt`.

**Content for `EmergencyRequestActivity.kt` (this will be a simplified version focusing on the MVVM integration):**

```kotlin
package com.example.bloodbank

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bloodbank.Model.EmergencyRequest
import com.example.bloodbank.repository.Result
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EmergencyRequestActivity : AppCompatActivity() {

    private val emergencyRequestViewModel: EmergencyRequestViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels() // To get current user UID

    private lateinit var etPatientName: EditText
    private lateinit var etBloodGroup: EditText
    private lateinit var etHospitalName: EditText
    private lateinit var etHospitalAddress: EditText
    private lateinit var etContactNumber: EditText
    private lateinit var etRequiredDate: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnSubmitRequest: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_request) // Assuming this layout exists

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Create Emergency Request"

        etPatientName = findViewById(R.id.etPatientName)
        etBloodGroup = findViewById(R.id.etBloodGroup)
        etHospitalName = findViewById(R.id.etHospitalName)
        etHospitalAddress = findViewById(R.id.etHospitalAddress)
        etContactNumber = findViewById(R.id.etContactNumber)
        etRequiredDate = findViewById(R.id.etRequiredDate)
        etDescription = findViewById(R.id.etDescription)
        btnSubmitRequest = findViewById(R.id.btnSubmitRequest)
        progressBar = findViewById(R.id.progressBar)

        btnSubmitRequest.setOnClickListener {
            createRequest()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    emergencyRequestViewModel.isLoading.collect { isLoading ->
                        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                        btnSubmitRequest.isEnabled = !isLoading
                    }
                }
                launch {
                    emergencyRequestViewModel.requestCreationResult.collect { result ->
                        result?.let {
                            when (it) {
                                is Result.Success -> {
                                    Toast.makeText(this@EmergencyRequestActivity, "Request created successfully!", Toast.LENGTH_LONG).show()
                                    finish() // Go back to previous screen
                                }
                                is Result.Error -> {
                                    Toast.makeText(this@EmergencyRequestActivity, "Error: ${it.exception.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                            emergencyRequestViewModel.resetRequestCreationResult() // Consume the event
                        }
                    }
                }
                launch {
                    emergencyRequestViewModel.error.collect { error ->
                        error?.let {
                            Toast.makeText(this@EmergencyRequestActivity, "Operation failed: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun createRequest() {
        val patientName = etPatientName.text.toString().trim()
        val bloodGroup = etBloodGroup.text.toString().trim()
        val hospitalName = etHospitalName.text.toString().trim()
        val hospitalAddress = etHospitalAddress.text.toString().trim()
        val contactNumber = etContactNumber.text.toString().trim()
        val requiredDate = etRequiredDate.text.toString().trim()
        val description = etDescription.text.toString().trim()

        if (patientName.isEmpty() || bloodGroup.isEmpty() || hospitalName.isEmpty() ||
            hospitalAddress.isEmpty() || contactNumber.isEmpty() || requiredDate.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val requesterId = mainViewModel.currentUser.value?.id // Get current user ID from MainViewModel

        if (requesterId == null) {
            Toast.makeText(this, "User not logged in or ID not available.", Toast.LENGTH_SHORT).show()
            return
        }

        val newRequest = EmergencyRequest(
            patientName = patientName,
            bloodGroup = bloodGroup,
            hospitalName = hospitalName,
            hospitalAddress = hospitalAddress,
            contactNumber = contactNumber,
            requiredDate = requiredDate,
            description = description,
            requesterId = requesterId,
            status = "pending"
        )
        emergencyRequestViewModel.createEmergencyRequest(newRequest)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
```

This completes a significant chunk of the refactoring process for emergency requests. It migrates a data model, creates its repository, and then refactors an Activity to MVVM, using the new repository and ViewModel.

The next step would be to update the `INCOMPLETE_SUGGESTIONS.md` to reflect these accomplishments and then continue with other parts of the application.