package com.example.bloodbank.repository

import com.example.bloodbank.Model.EmergencyRequest
import com.example.bloodbank.Model.EmergencyResponse
import kotlinx.coroutines.flow.Flow

interface EmergencyRequestRepository {
    fun createEmergencyRequest(request: EmergencyRequest): Flow<Result<Unit>>
    fun getEmergencyRequestById(requestId: String): Flow<EmergencyRequest?>
    fun getAllEmergencyRequests(): Flow<List<EmergencyRequest>>
    fun getEmergencyRequestsByUserId(userId: String): Flow<List<EmergencyRequest>>
    fun updateEmergencyRequestStatus(requestId: String, status: String): Flow<Result<Unit>>
    fun respondToEmergencyRequest(requestId: String, response: EmergencyResponse): Flow<Result<Unit>>
    fun acceptDonorForRequest(requestId: String, donorId: String): Flow<Result<Unit>>
}