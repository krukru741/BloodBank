package com.example.bloodbank

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * UpdateProfileActivity - Allows users to update their profile information.
 * Uses modern Android APIs like registerForActivityResult for image picking.
 */
class UpdateProfileActivity : AppCompatActivity() {
    
    // Views
    private lateinit var toolbar: Toolbar
    private lateinit var profileImage: CircleImageView
    private lateinit var changeImageButton: MaterialButton
    private lateinit var updateButton: MaterialButton
    private lateinit var nameLayout: TextInputLayout
    private lateinit var phoneLayout: TextInputLayout
    private lateinit var addressLayout: TextInputLayout
    private lateinit var birthdateLayout: TextInputLayout
    private lateinit var nameInput: TextInputEditText
    private lateinit var phoneInput: TextInputEditText
    private lateinit var addressInput: TextInputEditText
    private lateinit var birthdateInput: TextInputEditText
    
    // State
    private var currentPhotoFile: File? = null
    private lateinit var loader: ProgressDialog
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    // Firebase
    private val userRef by lazy {
        FirebaseDatabase.getInstance().getReference()
            .child("users")
            .child(FirebaseAuth.getInstance().currentUser?.uid ?: "")
    }
    
    // Modern image picker using Activity Result API
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { handleImageSelection(it) }
    }
    
    companion object {
        private const val APP_DIRECTORY = "BloodBank"
        private const val IMAGE_DIRECTORY = "ProfileImages"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_profile)
        
        // Initialize views
        initializeViews()
        
        // Setup toolbar
        setupToolbar()
        
        // Load current user data
        loadCurrentData()
        
        // Setup date picker
        setupDatePicker()
        
        // Setup image picker
        setupImagePicker()
        
        // Setup update button
        setupUpdateButton()
    }
    
    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        profileImage = findViewById(R.id.profileImage)
        changeImageButton = findViewById(R.id.changeImageButton)
        updateButton = findViewById(R.id.updateButton)
        
        nameLayout = findViewById(R.id.nameLayout)
        phoneLayout = findViewById(R.id.phoneLayout)
        addressLayout = findViewById(R.id.addressLayout)
        birthdateLayout = findViewById(R.id.birthdateLayout)
        
        nameInput = findViewById(R.id.nameInput)
        phoneInput = findViewById(R.id.phoneInput)
        addressInput = findViewById(R.id.addressInput)
        birthdateInput = findViewById(R.id.birthdateInput)
        
        loader = ProgressDialog(this)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Update Profile"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }
    
    private fun loadCurrentData() {
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    nameInput.setText(snapshot.child("name").getValue(String::class.java))
                    phoneInput.setText(snapshot.child("phonenumber").getValue(String::class.java))
                    addressInput.setText(snapshot.child("address").getValue(String::class.java))
                    birthdateInput.setText(snapshot.child("birthdate").getValue(String::class.java))
                    
                    val profileImagePath = snapshot.child("profileImagePath").getValue(String::class.java)
                    profileImagePath?.let { loadProfileImage(it) }
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@UpdateProfileActivity,
                    "Error loading profile: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
    
    private fun loadProfileImage(imagePath: String) {
        try {
            val imgFile = File(imagePath)
            if (imgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                profileImage.setImageBitmap(bitmap)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading profile image", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupDatePicker() {
        birthdateInput.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    birthdateInput.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }
    
    private fun setupImagePicker() {
        changeImageButton.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }
    
    private fun handleImageSelection(uri: Uri) {
        try {
            // Create a new file to save the image
            currentPhotoFile = createImageFile()
            
            // Copy the selected image to the new file
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            FileOutputStream(currentPhotoFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            
            // Display the image
            profileImage.setImageBitmap(bitmap)
        } catch (e: IOException) {
            Toast.makeText(this, "Error saving image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun createImageFile(): File {
        // Create app directory if it doesn't exist
        val appDir = File(filesDir, APP_DIRECTORY).apply {
            if (!exists()) mkdirs()
        }
        
        // Create images directory if it doesn't exist
        val imageDir = File(appDir, IMAGE_DIRECTORY).apply {
            if (!exists()) mkdirs()
        }
        
        // Create image file
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(System.currentTimeMillis())
        val imageFileName = "PROFILE_$timeStamp.jpg"
        
        return File(imageDir, imageFileName)
    }
    
    private fun setupUpdateButton() {
        updateButton.setOnClickListener {
            if (validateInputs()) {
                updateProfile()
            }
        }
    }
    
    private fun validateInputs(): Boolean {
        val name = nameInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val address = addressInput.text.toString().trim()
        val birthdate = birthdateInput.text.toString().trim()
        
        // Clear previous errors
        nameLayout.error = null
        phoneLayout.error = null
        addressLayout.error = null
        birthdateLayout.error = null
        
        return when {
            name.isEmpty() -> {
                nameLayout.error = "Name is required"
                false
            }
            phone.isEmpty() -> {
                phoneLayout.error = "Phone number is required"
                false
            }
            address.isEmpty() -> {
                addressLayout.error = "Address is required"
                false
            }
            birthdate.isEmpty() -> {
                birthdateLayout.error = "Birthdate is required"
                false
            }
            else -> true
        }
    }
    
    private fun updateProfile() {
        loader.apply {
            setMessage("Updating profile...")
            setCanceledOnTouchOutside(false)
            show()
        }
        
        val userMap = hashMapOf<String, Any>(
            "name" to nameInput.text.toString().trim(),
            "phonenumber" to phoneInput.text.toString().trim(),
            "address" to addressInput.text.toString().trim(),
            "birthdate" to birthdateInput.text.toString().trim()
        )
        
        // Add profile image path if available
        currentPhotoFile?.let {
            userMap["profileImagePath"] = it.absolutePath
        }
        
        userRef.updateChildren(userMap).addOnCompleteListener { task ->
            loader.dismiss()
            if (task.isSuccessful) {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(
                    this,
                    "Error updating profile: ${task.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
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
