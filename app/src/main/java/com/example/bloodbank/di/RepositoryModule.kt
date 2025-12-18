package com.example.bloodbank.di

import com.example.bloodbank.repository.FirebaseUserRepository
import com.example.bloodbank.repository.UserRepository
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
}