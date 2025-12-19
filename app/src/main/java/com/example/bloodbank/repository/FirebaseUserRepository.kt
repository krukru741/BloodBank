package com.example.bloodbank.repository

import com.example.bloodbank.DatabaseHelper
import com.example.bloodbank.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class FirebaseUserRepository @Inject constructor(
    private val databaseHelper: DatabaseHelper,
    private val firebaseAuth: FirebaseAuth
) : UserRepository {

    override fun getCurrentUserUid(): String? {
        return firebaseAuth.currentUser?.uid
    }

    override fun getUserDetails(userId: String): Flow<User?> = callbackFlow {
        val userRef = databaseHelper.getUsersReference().child(userId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    trySend(user)
                } else {
                    trySend(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException()) // Close the flow with an exception on cancellation
            }
        }

        userRef.addValueEventListener(listener)
        awaitClose {
            userRef.removeEventListener(listener)
        }
    }

    override fun getUserType(userId: String): Flow<String?> = callbackFlow {
        val userTypeRef = databaseHelper.getUsersReference().child(userId).child("type")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(String::class.java))
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        userTypeRef.addValueEventListener(listener)
        awaitClose {
            userTypeRef.removeEventListener(listener)
        }
    }

    override fun readDonors(): Flow<List<User>> = callbackFlow {
        val donorsQuery = databaseHelper.getUsersReference().orderByChild("type").equalTo("donor")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val donorList = mutableListOf<User>()
                for (dataSnapshot in snapshot.children) {
                    val user = dataSnapshot.getValue(User::class.java)
                    user?.let { donorList.add(it) }
                }
                trySend(donorList)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        donorsQuery.addValueEventListener(listener)
        awaitClose {
            donorsQuery.removeEventListener(listener)
        }
    }

    override fun readRecipients(): Flow<List<User>> = callbackFlow {
        val recipientsQuery = databaseHelper.getUsersReference().orderByChild("type").equalTo("recipient")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recipientList = mutableListOf<User>()
                for (dataSnapshot in snapshot.children) {
                    val user = dataSnapshot.getValue(User::class.java)
                    user?.let { recipientList.add(it) }
                }
                trySend(recipientList)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        recipientsQuery.addValueEventListener(listener)
        awaitClose {
            recipientsQuery.removeEventListener(listener)
        }
    }

    override fun addUser(user: User): Flow<Result<Unit>> = callbackFlow {
        val userId = user.id ?: run {
            trySend(Result.Error(Exception("User ID cannot be null")))
            close()
            return@callbackFlow
        }

        databaseHelper.getUsersReference().child(userId).setValue(user)
            .addOnSuccessListener {
                trySend(Result.Success(Unit))
                close()
            }
            .addOnFailureListener { exception ->
                trySend(Result.Error(exception))
                close()
            }

        awaitClose { }
    }

    override fun logout() {
        firebaseAuth.signOut()
    }
}