package com.example.bloodbank

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch
import java.io.File

/**
 * UserProfileActivity - Displays another user's profile information.
 * Used when viewing profiles of donors/recipients from search or emergency requests.
 * Follows MVVM architecture pattern with ProfileViewModel.
 */
@AndroidEntryPoint
class UserProfileActivity : AppCompatActivity() {
    
    private val viewModel: ProfileViewModel by viewModels()
    
    // Views
    private lateinit var toolbar: Toolbar
    private lateinit var profileImage: CircleImageView
    private lateinit var nameText: TextView
    private lateinit var genderText: TextView
    private lateinit var emailText: TextView
    private lateinit var phoneText: TextView
    private lateinit var addressText: TextView
    private lateinit var bloodgroupText: TextView
    private lateinit var typeText: TextView
    
    private var userId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        
        // Get user ID from intent
        userId = intent.getStringExtra("userid") ?: ""
        
        if (userId.isEmpty()) {
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Initialize views
        initializeViews()
        
        // Setup toolbar
        setupToolbar()
        
        // Observe ViewModel state
        observeViewModel()
    }
    
    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        profileImage = findViewById(R.id.profileImage)
        nameText = findViewById(R.id.nameText)
        emailText = findViewById(R.id.emailText)
        genderText = findViewById(R.id.genderText)
        phoneText = findViewById(R.id.phoneText)
        addressText = findViewById(R.id.addressText)
        bloodgroupText = findViewById(R.id.bloodgroupText)
        typeText = findViewById(R.id.typeText)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "User Profile"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe user data
                launch {
                    viewModel.user.collect { user ->
                        user?.let { displayUserData(it) }
                    }
                }
                
                // Observe errors
                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            Toast.makeText(this@UserProfileActivity, it, Toast.LENGTH_SHORT).show()
                            viewModel.clearError()
                        }
                    }
                }
            }
        }
    }
    
    private fun displayUserData(user: com.example.bloodbank.Model.User) {
        // Set text fields
        nameText.text = user.name ?: "Unknown"
        emailText.text = "Email: ${user.email ?: "Not provided"}"
        phoneText.text = "Phone: ${user.phoneNumber ?: "Not provided"}"
        addressText.text = "Address: ${user.address ?: "Not provided"}"
        bloodgroupText.text = "Blood Group: ${user.bloodGroup ?: "Not specified"}"
        typeText.text = user.type ?: "User"
        genderText.text = "Gender: ${user.gender ?: "Not specified"}"
        
        // Load profile image
        val profileImagePath = user.profileImagePath
        if (!profileImagePath.isNullOrEmpty()) {
            try {
                val imgFile = File(profileImagePath)
                if (imgFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                    profileImage.setImageBitmap(bitmap)
                } else {
                    profileImage.setImageResource(R.drawable.profile_pic)
                }
            } catch (e: Exception) {
                profileImage.setImageResource(R.drawable.profile_pic)
            }
        } else {
            profileImage.setImageResource(R.drawable.profile_pic)
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
