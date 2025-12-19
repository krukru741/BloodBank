package com.example.bloodbank

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.bloodbank.Model.User
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * DonorRegistrationActivity - Donor registration with comprehensive validation.
 * Follows MVVM architecture with AuthViewModel.
 */
@AndroidEntryPoint
class DonorRegistrationActivity : AppCompatActivity() {
    
    private val viewModel: AuthViewModel by viewModels()
    
    // Views
    private lateinit var backButton: TextView
    private lateinit var profileImage: CircleImageView
    private lateinit var fullNameInput: TextInputEditText
    private lateinit var idNumberInput: TextInputEditText
    private lateinit var phoneInput: TextInputEditText
    private lateinit var occupationInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var lastDonationInput: TextInputEditText
    private lateinit var addressInput: TextInputEditText
    private lateinit var birthdateInput: TextInputEditText
    private lateinit var bloodGroupSpinner: MaterialAutoCompleteTextView
    private lateinit var genderSpinner: MaterialAutoCompleteTextView
    private lateinit var registerButton: Button
    
    // State
    private var currentPhotoFile: File? = null
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    // Modern image picker
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
        setContentView(R.layout.activity_donor_registration)
        
        initializeViews()
        setupSpinners()
        setupDatePickers()
        setupClickListeners()
        observeViewModel()
    }
    
    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        profileImage = findViewById(R.id.profile_image)
        fullNameInput = findViewById(R.id.registeredFullName)
        idNumberInput = findViewById(R.id.registeredIdNumber)
        phoneInput = findViewById(R.id.registeredPhoneNumber)
        occupationInput = findViewById(R.id.registeredOccupation)
        emailInput = findViewById(R.id.registeredEmail)
        passwordInput = findViewById(R.id.registeredPassword)
        lastDonationInput = findViewById(R.id.registeredLastDonation)
        addressInput = findViewById(R.id.registeredAddress)
        birthdateInput = findViewById(R.id.registeredDate)
        genderSpinner = findViewById(R.id.gendersSpinner)
        bloodGroupSpinner = findViewById(R.id.bloodGroupsSpinner)
        registerButton = findViewById(R.id.registerButton)
    }
    
    private fun setupSpinners() {
        // Gender spinner
        val genderItems = arrayOf("Select your gender", "Male", "Female", "Other")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genderItems)
        genderSpinner.setAdapter(genderAdapter)
        
        // Blood group spinner
        val bloodGroupItems = arrayOf("Select your blood group", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        val bloodGroupAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, bloodGroupItems)
        bloodGroupSpinner.setAdapter(bloodGroupAdapter)
    }
    
    private fun setupDatePickers() {
        birthdateInput.setOnClickListener { showDatePicker(birthdateInput, true) }
        birthdateInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) showDatePicker(birthdateInput, true)
        }
        
        lastDonationInput.setOnClickListener { showDatePicker(lastDonationInput, false) }
        lastDonationInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) showDatePicker(lastDonationInput, false)
        }
    }
    
    private fun setupClickListeners() {
        backButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        
        profileImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
        
        registerButton.setOnClickListener {
            if (validateInputs()) {
                registerDonor()
            }
        }
    }
    
    private fun validateInputs(): Boolean {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val fullName = fullNameInput.text.toString().trim()
        val idNumber = idNumberInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val occupation = occupationInput.text.toString().trim()
        val address = addressInput.text.toString().trim()
        val birthdate = birthdateInput.text.toString().trim()
        val lastDonation = lastDonationInput.text.toString().trim()
        val gender = genderSpinner.text.toString()
        val bloodGroup = bloodGroupSpinner.text.toString()
        
        return when {
            email.isEmpty() -> {
                emailInput.error = "Email is required"
                false
            }
            password.isEmpty() -> {
                passwordInput.error = "Password is required"
                false
            }
            password.length < 6 -> {
                passwordInput.error = "Password must be at least 6 characters"
                false
            }
            fullName.isEmpty() -> {
                fullNameInput.error = "Full name is required"
                false
            }
            idNumber.isEmpty() -> {
                idNumberInput.error = "ID number is required"
                false
            }
            phone.isEmpty() -> {
                phoneInput.error = "Phone number is required"
                false
            }
            occupation.isEmpty() -> {
                occupationInput.error = "Occupation is required"
                false
            }
            address.isEmpty() -> {
                addressInput.error = "Address is required"
                false
            }
            birthdate.isEmpty() -> {
                birthdateInput.error = "Birthdate is required"
                false
            }
            lastDonation.isEmpty() -> {
                lastDonationInput.error = "Last donation date is required"
                false
            }
            gender == "Select your gender" -> {
                Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show()
                false
            }
            bloodGroup == "Select your blood group" -> {
                Toast.makeText(this, "Please select your blood group", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }
    
    private fun registerDonor() {
        val user = User(
            id = "",
            name = fullNameInput.text.toString().trim(),
            email = emailInput.text.toString().trim(),
            idnumber = idNumberInput.text.toString().trim(),
            phoneNumber = phoneInput.text.toString().trim(),
            address = addressInput.text.toString().trim(),
            birthdate = birthdateInput.text.toString().trim(),
            gender = genderSpinner.text.toString(),
            bloodGroup = bloodGroupSpinner.text.toString(),
            type = "donor",
            search = "donor${bloodGroupSpinner.text}",
            occupation = occupationInput.text.toString().trim(),
            lastDonationDate = lastDonationInput.text.toString().trim(),
            profileImagePath = currentPhotoFile?.absolutePath ?: ""
        )
        
        val password = passwordInput.text.toString().trim()
        viewModel.register(user, password)
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.authState.collect { state ->
                        when (state) {
                            is AuthViewModel.AuthState.Authenticated -> {
                                Toast.makeText(
                                    this@DonorRegistrationActivity,
                                    "Registration successful! Please verify your email.",
                                    Toast.LENGTH_LONG
                                ).show()
                                startActivity(Intent(this@DonorRegistrationActivity, VerifyEmailActivity::class.java))
                                finish()
                            }
                            else -> {}
                        }
                    }
                }
                
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        registerButton.isEnabled = !isLoading
                        registerButton.text = if (isLoading) "Registering..." else "Register"
                    }
                }
                
                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            Toast.makeText(this@DonorRegistrationActivity, it, Toast.LENGTH_SHORT).show()
                            viewModel.clearError()
                        }
                    }
                }
            }
        }
    }
    
    private fun showDatePicker(targetField: TextInputEditText, isBirthdate: Boolean) {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                targetField.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    
    private fun handleImageSelection(uri: Uri) {
        try {
            profileImage.setImageURI(uri)
            
            // Create directory
            val storageDir = File(filesDir, "$APP_DIRECTORY${File.separator}$IMAGE_DIRECTORY").apply {
                if (!exists()) mkdirs()
            }
            
            // Create unique filename
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            currentPhotoFile = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
            
            // Save image
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            FileOutputStream(currentPhotoFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving profile image", Toast.LENGTH_SHORT).show()
        }
    }
}
