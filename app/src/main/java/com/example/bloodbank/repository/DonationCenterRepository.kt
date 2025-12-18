package com.example.bloodbank.repository

import com.example.bloodbank.Model.DonationCenter
import kotlinx.coroutines.flow.Flow

interface DonationCenterRepository {
    fun getAllDonationCenters(): Flow<List<DonationCenter>>
    fun getDonationCenterById(centerId: String): Flow<DonationCenter?>
    suspend fun addDonationCenter(center: DonationCenter): Result<Unit>
}