package com.example.bloodbank

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bloodbank.Adapter.CompatibleUserAdapter
import com.example.bloodbank.Model.CompatibleUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

/**
 * CompatibleUsersActivity - Display list of compatible blood donors.
 * Simple Firebase query activity, no complex ViewModel needed.
 */
class CompatibleUsersActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CompatibleUserAdapter
    
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val usersList = mutableListOf<CompatibleUser>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compatible_users)
        
        initializeViews()
        setupToolbar()
        setupRecyclerView()
        loadCompatibleUsers()
    }
    
    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.compatibleUsersRecyclerView)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Compatible Donors"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }
    
    private fun setupRecyclerView() {
        adapter = CompatibleUserAdapter(usersList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
    
    private fun loadCompatibleUsers() {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return
        
        // Get current user's blood group
        database.reference.child("users").child(currentUserId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val bloodGroup = snapshot.child("bloodgroup").getValue(String::class.java)
                    bloodGroup?.let { loadDonorsByBloodGroup(it) }
                }
                
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@CompatibleUsersActivity,
                        "Error loading user data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
    
    private fun loadDonorsByBloodGroup(recipientBloodGroup: String) {
        val compatibleBloodGroups = getCompatibleBloodGroups(recipientBloodGroup)
        
        database.reference.child("users")
            .orderByChild("type")
            .equalTo("donor")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    usersList.clear()
                    
                    snapshot.children.forEach { userSnapshot ->
                        val bloodGroup = userSnapshot.child("bloodgroup").getValue(String::class.java)
                        
                        if (bloodGroup in compatibleBloodGroups) {
                            val user = CompatibleUser(
                                id = userSnapshot.child("id").getValue(String::class.java) ?: "",
                                name = userSnapshot.child("name").getValue(String::class.java) ?: "",
                                bloodgroup = bloodGroup ?: "",
                                phonenumber = userSnapshot.child("phonenumber").getValue(String::class.java) ?: "",
                                address = userSnapshot.child("address").getValue(String::class.java) ?: ""
                            )
                            usersList.add(user)
                        }
                    }
                    
                    adapter.notifyDataSetChanged()
                }
                
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@CompatibleUsersActivity,
                        "Error loading donors",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
    
    private fun getCompatibleBloodGroups(recipientBloodGroup: String): List<String> {
        return when (recipientBloodGroup) {
            "A+" -> listOf("A+", "A-", "O+", "O-")
            "A-" -> listOf("A-", "O-")
            "B+" -> listOf("B+", "B-", "O+", "O-")
            "B-" -> listOf("B-", "O-")
            "AB+" -> listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
            "AB-" -> listOf("A-", "B-", "AB-", "O-")
            "O+" -> listOf("O+", "O-")
            "O-" -> listOf("O-")
            else -> emptyList()
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
