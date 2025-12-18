package com.example.bloodbank.repository

import com.example.bloodbank.Model.DonorHealth
import kotlinx.coroutines.flow.Flow

interface DonorHealthRepository {
    fun getDonorHealth(donorId: String): Flow<DonorHealth?>
    fun updateDonorHealth(donorId: String, donorHealth: DonorHealth): Flow<Result<Unit>>
    fun createDonorHealth(donorId: String, donorHealth: DonorHealth): Flow<Result<Unit>>
}