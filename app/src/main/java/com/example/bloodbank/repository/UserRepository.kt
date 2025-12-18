package com.example.bloodbank.repository

import com.example.bloodbank.Model.User
import kotlinx.coroutines.flow.Flow

/**
 * A sealed class representing the result of an operation.
 * Can be either a success with data or an error with an exception.
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
}

/**
 * Interface for user-related data operations.
 * Abstracts the data source, allowing for flexible implementation (e.g., Firebase, local DB).
 */
interface UserRepository {
    /**
     * Retrieves the current authenticated user's unique identifier.
     * @return The UID of the current user, or null if no user is authenticated.
     */
    fun getCurrentUserUid(): String?

    /**
     * Retrieves a specific user's details as a Flow.
     * The Flow emits null if the user is not found or if there's an error.
     * @param userId The unique identifier of the user to retrieve.
     * @return A Flow emitting the User object or null.
     */
    fun getUserDetails(userId: String): Flow<User?>

    /**
     * Retrieves a specific user's type (e.g., "donor", "recipient") as a Flow.
     * @param userId The unique identifier of the user.
     * @return A Flow emitting the user's type string or null.
     */
    fun getUserType(userId: String): Flow<String?>

    /**
     * Retrieves a list of all users marked as "donor" as a Flow.
     * @return A Flow emitting a list of User objects who are donors.
     */
    fun readDonors(): Flow<List<User>>

    /**
     * Retrieves a list of all users marked as "recipient" as a Flow.
     * @return A Flow emitting a list of User objects who are recipients.
     */
    fun readRecipients(): Flow<List<User>>

    /**
     * Signs out the current user from the authentication system.
     */
    fun logout()
}