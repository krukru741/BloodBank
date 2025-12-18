package com.example.bloodbank

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bloodbank.Adapter.UserAdapter
import com.example.bloodbank.Model.User
import com.example.bloodbank.Service.DonationReminderService
import com.example.bloodbank.utils.GooglePlayServicesUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    @Inject
    lateinit var databaseHelper: DatabaseHelper

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var navView: NavigationView
    private lateinit var fabEmergency: FloatingActionButton

    private lateinit var navFullname: TextView
    private lateinit var navEmail: TextView
    private lateinit var navBloodgroup: TextView
    private lateinit var navType: TextView
    private lateinit var navLastDonation: TextView
    private lateinit var navLastRequest: TextView
    private lateinit var navPhone: TextView
    private lateinit var additionalInfoSection: LinearLayout
    private lateinit var donorInfo: LinearLayout
    private lateinit var recipientInfo: LinearLayout
    private lateinit var navProfileImage: de.hdodenhof.circleimageview.CircleImageView

    private lateinit var userRef: DatabaseReference
    private var userValueEventListener: ValueEventListener? = null
    private var userTypeValueEventListener: ValueEventListener? = null
    private var donorsQueryListener: ValueEventListener? = null
    private var recipientsQueryListener: ValueEventListener? = null

    private lateinit var recycleView: RecyclerView
    private lateinit var progressbar: ProgressBar

    private lateinit var userList: MutableList<User>
    private lateinit var userAdapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check for Google Play Services
        checkGooglePlayServices()

        // Initialize Toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.nav_view)
        fabEmergency = findViewById(R.id.fab_emergency)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        progressbar = findViewById(R.id.progressbar)

        recycleView = findViewById(R.id.recycleView)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        recycleView.layoutManager = layoutManager

        userList = ArrayList()
        userAdapter = UserAdapter(this, userList)
        recycleView.adapter = userAdapter

        // Initialize Firebase listeners
        initializeFirebaseListeners()
    }

    private fun initializeFirebaseListeners() {
        val userId = firebaseAuth.currentUser?.uid ?: return

        // Reference to user data
        userRef = databaseHelper.getUserReference(userId)

        // Listener for user type
        userTypeValueEventListener = object : ValueEventListener {
            override fun onDataChange(@NonNull snapshot: DataSnapshot) {
                val type = snapshot.child("type").getValue(String::class.java)
                if (type == "donor") {
                    readRecipients()
                    showDonorInfo()
                } else {
                    readDonors()
                    showRecipientInfo()
                }
            }

            override fun onCancelled(@NonNull error: DatabaseError) {
                Log.e(TAG, "Error fetching user type: ${error.message}")
            }
        }

        // Add the listener
        userRef.addValueEventListener(userTypeValueEventListener!!)

        // Initialize navigation header views
        val headerView = navView.getHeaderView(0)
        if (headerView != null) {
            navProfileImage = headerView.findViewById(R.id.nav_user_image)
            navFullname = headerView.findViewById(R.id.nav_user_fullname)
            navEmail = headerView.findViewById(R.id.nav_user_email)
            navPhone = headerView.findViewById(R.id.nav_user_phone)
            navBloodgroup = headerView.findViewById(R.id.nav_user_bloodgroup)
            navType = headerView.findViewById(R.id.nav_user_type)

            // Initialize additional info views
            additionalInfoSection = headerView.findViewById(R.id.additional_info_section)
            donorInfo = headerView.findViewById(R.id.donor_info)
            recipientInfo = headerView.findViewById(R.id.recipient_info)
            navLastDonation = headerView.findViewById(R.id.nav_last_donation)
            navLastRequest = headerView.findViewById(R.id.nav_last_request)
        }

        // Listener for user profile data
        userValueEventListener = object : ValueEventListener {
            override fun onDataChange(@NonNull snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Update basic user info
                    val fullname = snapshot.child("name").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)
                    val phone = snapshot.child("phonenumber").getValue(String::class.java)
                    val bloodgroup = snapshot.child("bloodgroup").getValue(String::class.java)
                    val type = snapshot.child("type").getValue(String::class.java)

                    Log.d(TAG, "Fullname: $fullname")
                    Log.d(TAG, "Email: $email")
                    Log.d(TAG, "Phone: $phone")
                    Log.d(TAG, "Bloodgroup: $bloodgroup")
                    Log.d(TAG, "Type: $type")

                    navFullname.text = fullname
                    navEmail.text = email
                    navPhone.text = phone
                    navBloodgroup.text = bloodgroup
                    type?.let {
                        navType.text = it.toUpperCase()
                        if (it == "donor") {
                            showDonorInfo()
                            if (snapshot.hasChild("lastDonation")) {
                                val lastDonation = snapshot.child("lastDonation").getValue(String::class.java)
                                navLastDonation.text = lastDonation
                            }
                        } else {
                            showRecipientInfo()
                            if (snapshot.hasChild("lastRequest")) {
                                val lastRequest = snapshot.child("lastRequest").getValue(String::class.java)
                                navLastRequest.text = lastRequest
                            }
                        }
                    }

                    // Update profile image if available
                    if (snapshot.hasChild("profileImagePath")) {
                        val profileImagePath = snapshot.child("profileImagePath").getValue(String::class.java)
                        if (!profileImagePath.isNullOrEmpty()) {
                            try {
                                val imgFile = File(profileImagePath)
                                if (imgFile.exists()) {
                                    val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                                    navProfileImage.setImageBitmap(bitmap)
                                } else {
                                    Glide.with(this@MainActivity)
                                        .load(profileImagePath)
                                        .placeholder(R.drawable.profile)
                                        .into(navProfileImage)
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error loading profile image: ${e.message}")
                                navProfileImage.setImageResource(R.drawable.profile)
                            }
                        }
                    }
                } else {
                    Log.e(TAG, "Snapshot does not exist")
                }
            }

            override fun onCancelled(@NonNull error: DatabaseError) {
                Log.e(TAG, "Error loading user data: ${error.message}")
                Toast.makeText(this@MainActivity, "Error loading user data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }

        userRef.addValueEventListener(userValueEventListener!!)

        // Set up FAB visibility and click listener
        fabEmergency.visibility = View.GONE // Hide by default
        fabEmergency.setOnClickListener { startEmergencyRequest() }
    }

    private fun showDonorInfo() {
        additionalInfoSection.visibility = View.VISIBLE
        donorInfo.visibility = View.VISIBLE
        recipientInfo.visibility = View.GONE
    }

    private fun showRecipientInfo() {
        additionalInfoSection.visibility = View.VISIBLE
        donorInfo.visibility = View.GONE
        recipientInfo.visibility = View.VISIBLE
    }

    private fun readDonors() {
        val reference = databaseHelper.getUsersReference()
        val query = reference.orderByChild("type").equalTo("donor")

        donorsQueryListener = object : ValueEventListener {
            override fun onDataChange(@NonNull snapshot: DataSnapshot) {
                userList.clear()
                for (dataSnapshot in snapshot.children) {
                    val user = dataSnapshot.getValue(User::class.java)
                    user?.let { userList.add(it) }
                }
                userAdapter.notifyDataSetChanged()
                progressbar.visibility = View.GONE

                if (userList.isEmpty()) {
                    Toast.makeText(this@MainActivity, "No Donors", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(@NonNull error: DatabaseError) {
                Log.e(TAG, "Error reading donors: ${error.message}")
            }
        }

        query.addValueEventListener(donorsQueryListener!!)
    }

    private fun readRecipients() {
        val reference = databaseHelper.getUsersReference()
        val query = reference.orderByChild("type").equalTo("recipient")

        recipientsQueryListener = object : ValueEventListener {
            override fun onDataChange(@NonNull snapshot: DataSnapshot) {
                userList.clear()
                for (dataSnapshot in snapshot.children) {
                    val user = dataSnapshot.getValue(User::class.java)
                    user?.let { userList.add(it) }
                }
                userAdapter.notifyDataSetChanged()
                progressbar.visibility = View.GONE

                if (userList.isEmpty()) {
                    Toast.makeText(this@MainActivity, "No Recipients", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(@NonNull error: DatabaseError) {
                Log.e(TAG, "Error reading recipients: ${error.message}")
            }
        }

        query.addValueEventListener(recipientsQueryListener!!)
    }

    private fun startEmergencyRequest() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        databaseHelper.getUserReference(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(@NonNull snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val type = snapshot.child("type").getValue(String::class.java)
                    val intent: Intent = if (type == "recipient") {
                        Intent(this@MainActivity, EmergencyRequestActivity::class.java)
                    } else {
                        Intent(this@MainActivity, EmergencyRequestListActivity::class.java)
                    }
                    startActivity(intent)
                }
            }

            override fun onCancelled(@NonNull error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onNavigationItemSelected(@NonNull item: MenuItem): Boolean {
        val itemId = item.itemId

        when (itemId) {
            R.id.emergency_request -> {
                val userId = firebaseAuth.currentUser?.uid ?: return false
                databaseHelper.getUserReference(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(@NonNull snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val type = snapshot.child("type").getValue(String::class.java)
                            val intent: Intent = if (type == "recipient") {
                                Intent(this@MainActivity, EmergencyRequestActivity::class.java)
                            } else {
                                Intent(this@MainActivity, EmergencyRequestListActivity::class.java)
                            }
                            startActivity(intent)
                        }
                    }

                    override fun onCancelled(@NonNull error: DatabaseError) {
                        Toast.makeText(this@MainActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            R.id.view_emergency_requests -> startActivity(Intent(this, EmergencyRequestListActivity::class.java))
            R.id.donor_health -> startActivity(Intent(this, DonorHealthActivity::class.java))
            R.id.achievements -> startActivity(Intent(this, AchievementsActivity::class.java))
            R.id.schedule_donation -> startActivity(Intent(this, ScheduleDonationActivity::class.java))
            R.id.my_appointments -> startActivity(Intent(this, MyAppointmentsActivity::class.java))
            R.id.donation_centers -> startActivity(Intent(this, DonationCentersActivity::class.java))
            R.id.aplus -> {
                val intent = Intent(this, CategorySelectedActivity::class.java)
                intent.putExtra("group", "A+")
                startActivity(intent)
            }
            R.id.aminus -> {
                val intent = Intent(this, CategorySelectedActivity::class.java)
                intent.putExtra("group", "A-")
                startActivity(intent)
            }
            R.id.bplus -> {
                val intent = Intent(this, CategorySelectedActivity::class.java)
                intent.putExtra("group", "B+")
                startActivity(intent)
            }
            R.id.bminus -> {
                val intent = Intent(this, CategorySelectedActivity::class.java)
                intent.putExtra("group", "B-")
                startActivity(intent)
            }
            R.id.abplus -> {
                val intent = Intent(this, CategorySelectedActivity::class.java)
                intent.putExtra("group", "AB+")
                startActivity(intent)
            }
            R.id.abminus -> {
                val intent = Intent(this, CategorySelectedActivity::class.java)
                intent.putExtra("group", "AB-")
                startActivity(intent)
            }
            R.id.oplus -> {
                val intent = Intent(this, CategorySelectedActivity::class.java)
                intent.putExtra("group", "O+")
                startActivity(intent)
            }
            R.id.ominus -> {
                val intent = Intent(this, CategorySelectedActivity::class.java)
                intent.putExtra("group", "O-")
                startActivity(intent)
            }
            R.id.compatible -> {
                val intent = Intent(this, CompatibleUsersActivity::class.java)
                startActivity(intent)
            }
            R.id.notifications -> {
                val intent = Intent(this, NotificationsActivity::class.java)
                startActivity(intent)
            }
            R.id.aboutus -> {
                val intent = Intent(this, AboutUsActivity::class.java)
                startActivity(intent)
            }
            R.id.Faq -> {
                val intent = Intent(this, FaqActivity::class.java)
                startActivity(intent)
            }
            R.id.sentEmail -> {
                val intent = Intent(this, SentEmailActivity::class.java)
                startActivity(intent)
            }
            R.id.logout -> logout()
            R.id.profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * Properly handle logout by removing all Firebase listeners before signing out
     */
    private fun logout() {
        // Remove all Firebase listeners
        userValueEventListener?.let { userRef.removeEventListener(it) }
        userTypeValueEventListener?.let { userRef.removeEventListener(it) }

        // Remove query listeners
        donorsQueryListener?.let {
            databaseHelper.getUsersReference()
                .orderByChild("type")
                .equalTo("donor")
                .removeEventListener(it)
        }
        recipientsQueryListener?.let {
            databaseHelper.getUsersReference()
                .orderByChild("type")
                .equalTo("recipient")
                .removeEventListener(it)
        }

        // Sign out from Firebase Auth
        firebaseAuth.signOut()

        // Navigate to login screen
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Check donation eligibility for donors
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            databaseHelper.getUserReference(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(@NonNull snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val type = snapshot.child("type").getValue(String::class.java)
                        if (type == "donor") {
                            DonationReminderService(this@MainActivity).checkAndUpdateEligibility()
                        }
                    }
                }

                override fun onCancelled(@NonNull error: DatabaseError) {
                    Log.e(TAG, "Error checking user type on resume: ${error.message}")
                }
            })
        }
    }

    /**
     * Check if Google Play Services is available and show update dialog if needed
     */
    private fun checkGooglePlayServices() {
        if (!GooglePlayServicesUtils.isGooglePlayServicesAvailable(this)) {
            GooglePlayServicesUtils.showGooglePlayServicesUpdateDialog(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Handle Google Play Services resolution result
        if (requestCode == 9000) {
            if (resultCode == RESULT_OK) {
                // Google Play Services is now available
                Toast.makeText(this, "Google Play Services is now available", Toast.LENGTH_SHORT).show()
            } else {
                // Google Play Services is still not available
                Toast.makeText(this, "Google Play Services is required for this app", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove listeners when activity is destroyed
        userValueEventListener?.let { userRef.removeEventListener(it) }
        userTypeValueEventListener?.let { userRef.removeEventListener(it) }

        // Note: query listeners for donors/recipients are also removed in logout() if it's called.
        // For robustness, you might consider managing their lifecycle more explicitly
        // based on when they are added and if they persist beyond logout.
        // For now, mirroring the Java version's onDestroy logic.
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}