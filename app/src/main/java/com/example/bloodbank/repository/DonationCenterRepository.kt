package com.example.bloodbank.repository

import com.example.bloodbank.Model.DonationCenter
import kotlinx.coroutines.flow.Flow

interface DonationCenterRepository {
    fun getDonationCenters(): Flow<Result<List<DonationCenter>>>
    fun addDonationCenter(center: DonationCenter): Flow<Result<Unit>>
    fun updateDonationCenter(center: DonationCenter): Flow<Result<Unit>>
    fun deleteDonationCenter(centerId: String): Flow<Result<Unit>>
}