package com.example.bloodbank.repository

import com.example.bloodbank.Model.EmergencyRequest
import kotlinx.coroutines.flow.Flow

interface EmergencyRequestRepository {
    fun createEmergencyRequest(emergencyRequest: EmergencyRequest): Flow<Result<Unit>>
    fun getEmergencyRequest(requestId: String): Flow<Result<EmergencyRequest?>>
    fun getAllEmergencyRequests(): Flow<Result<List<EmergencyRequest>>>
    fun updateEmergencyRequestStatus(requestId: String, status: String): Flow<Result<Unit>>
    fun addResponseToEmergencyRequest(requestId: String, donorId: String, accepted: Boolean): Flow<Result<Unit>>
}