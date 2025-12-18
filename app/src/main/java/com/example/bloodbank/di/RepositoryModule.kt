package com.example.bloodbank.di

import com.example.bloodbank.repository.DonationAppointmentRepository
import com.example.bloodbank.repository.FirebaseDonationAppointmentRepository
import com.example.bloodbank.repository.FirebaseUserRepository
import com.example.bloodbank.repository.UserRepository
import com.example.bloodbank.repository.DonationCenterRepository
import com.example.bloodbank.repository.FirebaseDonationCenterRepository
import com.example.bloodbank.repository.DonorHealthRepository
import com.example.bloodbank.repository.FirebaseDonorHealthRepository
import com.example.bloodbank.repository.EmergencyRequestRepository
import com.example.bloodbank.repository.FirebaseEmergencyRequestRepository
import com.example.bloodbank.repository.MessageRepository
import com.example.bloodbank.repository.FirebaseMessageRepository
import com.example.bloodbank.repository.NotificationRepository
import com.example.bloodbank.repository.FirebaseNotificationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        firebaseUserRepository: FirebaseUserRepository
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindDonationAppointmentRepository(
        firebaseDonationAppointmentRepository: FirebaseDonationAppointmentRepository
    ): DonationAppointmentRepository

    @Binds
    @Singleton
    abstract fun bindDonationCenterRepository(
        firebaseDonationCenterRepository: FirebaseDonationCenterRepository
    ): DonationCenterRepository

    @Binds
    @Singleton
    abstract fun bindDonorHealthRepository(
        firebaseDonorHealthRepository: FirebaseDonorHealthRepository
    ): DonorHealthRepository

    @Binds
    @Singleton
    abstract fun bindEmergencyRequestRepository(
        firebaseEmergencyRequestRepository: FirebaseEmergencyRequestRepository
    ): EmergencyRequestRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        firebaseMessageRepository: FirebaseMessageRepository
    ): MessageRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        firebaseNotificationRepository: FirebaseNotificationRepository
    ): NotificationRepository
}