package com.example.bloodbank

import android.content.Intent
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
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch
import java.io.File

/**
 * ProfileActivity - Displays the current user's profile information.
 * Follows MVVM architecture pattern with ProfileViewModel.
 */
@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {
    
    private val viewModel: ProfileViewModel by viewModels()
    
    // Views
    private lateinit var toolbar: Toolbar
    private lateinit var profileImage: CircleImageView
    private lateinit var nameText: TextView
    private lateinit var genderText: TextView
    private lateinit var idNumberText: TextView
    private lateinit var emailText: TextView
    private lateinit var phoneText: TextView
    private lateinit var addressText: TextView
    private lateinit var birthdateText: TextView
    private lateinit var bloodgroupText: TextView
    private lateinit var typeText: TextView
    private lateinit var updateButton: MaterialButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        
        // Initialize views
        initializeViews()
        
        // Setup toolbar
        setupToolbar()
        
        // Setup update button
        setupUpdateButton()
        
        // Observe ViewModel state
        observeViewModel()
    }
    
    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        profileImage = findViewById(R.id.profileImage)
        nameText = findViewById(R.id.nameText)
        emailText = findViewById(R.id.emailText)
        genderText = findViewById(R.id.genderText)
        idNumberText = findViewById(R.id.idNumberText)
        phoneText = findViewById(R.id.phoneText)
        addressText = findViewById(R.id.addressText)
        birthdateText = findViewById(R.id.birthdateText)
        bloodgroupText = findViewById(R.id.bloodgroupText)
        typeText = findViewById(R.id.typeText)
        updateButton = findViewById(R.id.updateButton)
        
        // Verify all views are initialized
        if (!areViewsInitialized()) {
            Toast.makeText(this, "Error initializing views", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun areViewsInitialized(): Boolean {
        return ::profileImage.isInitialized && ::nameText.isInitialized && 
               ::emailText.isInitialized && ::genderText.isInitialized &&
               ::idNumberText.isInitialized && ::phoneText.isInitialized &&
               ::addressText.isInitialized && ::birthdateText.isInitialized &&
               ::bloodgroupText.isInitialized && ::typeText.isInitialized &&
               ::updateButton.isInitialized
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Profile"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }
    
    private fun setupUpdateButton() {
        updateButton.setOnClickListener {
            viewModel.onUpdateProfileClick()
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
                            Toast.makeText(this@ProfileActivity, it, Toast.LENGTH_SHORT).show()
                            viewModel.clearError()
                        }
                    }
                }
                
                // Observe navigation
                launch {
                    viewModel.navigateToUpdate.collect { shouldNavigate ->
                        if (shouldNavigate) {
                            startActivity(Intent(this@ProfileActivity, UpdateProfileActivity::class.java))
                            viewModel.onNavigationComplete()
                        }
                    }
                }
            }
        }
    }
    
    private fun displayUserData(user: com.example.bloodbank.Model.User) {
        // Set text fields with labels
        nameText.text = user.name ?: ""
        emailText.text = "Email: ${user.email ?: ""}"
        phoneText.text = "Phone: ${user.phoneNumber ?: ""}"
        addressText.text = "Address: ${user.address ?: ""}"
        birthdateText.text = "Birthdate: ${user.birthdate ?: ""}"
        bloodgroupText.text = "Blood Group: ${user.bloodGroup ?: ""}"
        typeText.text = user.type ?: ""
        genderText.text = "Gender: ${user.gender ?: ""}"
        idNumberText.text = "ID Number: ${user.idnumber ?: ""}"
        
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
                Toast.makeText(this, "Error loading profile image", Toast.LENGTH_SHORT).show()
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
    
    override fun onResume() {
        super.onResume()
        // Refresh profile data when returning from update screen
        viewModel.refresh()
    }
}
