package com.example.bloodbank.repository

import com.example.bloodbank.Model.DonationAppointment
import kotlinx.coroutines.flow.Flow

interface DonationAppointmentRepository {
    fun getAppointmentsForUser(userId: String): Flow<List<DonationAppointment>>
    fun scheduleAppointment(appointment: DonationAppointment): Flow<Result<Unit>>
    fun updateAppointmentStatus(appointmentId: String, status: String): Flow<Result<Unit>>
}