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
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bloodbank.Adapter.UserAdapter
import com.example.bloodbank.Model.User
import com.example.bloodbank.Service.DonationReminderService
import com.example.bloodbank.utils.GooglePlayServicesUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject // Keep if other @Inject fields exist, otherwise remove

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    // MainViewModel injection
    private val mainViewModel: MainViewModel by viewModels()

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
    private lateinit var navProfileImage: CircleImageView

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

        // Set up FAB visibility and click listener
        fabEmergency.visibility = View.GONE // Hide by default, will be made visible based on user type later
        fabEmergency.setOnClickListener { startEmergencyRequest() }

        // Observe ViewModel data
        observeViewModel()

        // Initialize user data and content based on user type
        mainViewModel.checkUserTypeAndLoadContent()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    mainViewModel.currentUser.collectLatest { user ->
                        user?.let {
                            updateNavigationHeader(it)
                            // Re-check donation eligibility if user is a donor
                            if (it.type == "donor") {
                                DonationReminderService(this@MainActivity).checkAndUpdateEligibility()
                            }
                        }
                    }
                }
                launch {
                    mainViewModel.userType.collectLatest { type ->
                        type?.let {
                            if (it == "donor") {
                                showDonorInfo()
                                fabEmergency.visibility = View.GONE // Donors don't create emergency requests via FAB
                            } else {
                                showRecipientInfo()
                                fabEmergency.visibility = View.VISIBLE // Recipients create emergency requests via FAB
                            }
                        }
                    }
                }
                launch {
                    mainViewModel.donors.collectLatest { donors ->
                        userList.clear()
                        userList.addAll(donors)
                        userAdapter.notifyDataSetChanged()
                        progressbar.visibility = View.GONE
                        if (donors.isEmpty()) {
                            Toast.makeText(this@MainActivity, "No Donors available", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                launch {
                    mainViewModel.recipients.collectLatest { recipients ->
                        userList.clear()
                        userList.addAll(recipients)
                        userAdapter.notifyDataSetChanged()
                        progressbar.visibility = View.GONE
                        if (recipients.isEmpty()) {
                            Toast.makeText(this@MainActivity, "No Recipients available", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                launch {
                    mainViewModel.isLoading.collectLatest { isLoading ->
                        progressbar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    mainViewModel.logoutEvent.collectLatest { loggedOut ->
                        if (loggedOut) {
                            val intent = Intent(this@MainActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun updateNavigationHeader(user: User) {
        val headerView = navView.getHeaderView(0) ?: return

        navProfileImage = headerView.findViewById(R.id.nav_user_image)
        navFullname = headerView.findViewById(R.id.nav_user_fullname)
        navEmail = headerView.findViewById(R.id.nav_user_email)
        navPhone = headerView.findViewById(R.id.nav_user_phone)
        navBloodgroup = headerView.findViewById(R.id.nav_user_bloodgroup)
        navType = headerView.findViewById(R.id.nav_user_type)

        additionalInfoSection = headerView.findViewById(R.id.additional_info_section)
        donorInfo = headerView.findViewById(R.id.donor_info)
recipientInfo = headerView.findViewById(R.id.recipient_info)
        navLastDonation = headerView.findViewById(R.id.nav_last_donation)
        navLastRequest = headerView.findViewById(R.id.nav_last_request)

        navFullname.text = user.name
        navEmail.text = user.email
        navPhone.text = user.phoneNumber
        navBloodgroup.text = user.bloodGroup
        user.type?.let { navType.text = it.uppercase() }

        if (user.type == "donor") {
            showDonorInfo()
            navLastDonation.text = user.lastDonationDate ?: "N/A"
        } else {
            showRecipientInfo()
            // Assuming lastRequest for recipient is available in User model or needs another fetch
            // For now, mirroring the original logic, which assumed it's part of the user snapshot.
            // If it's a separate field, ViewModel should provide it.
            // Placeholder:
            navLastRequest.text = user.lastRequestDate ?: "N/A" // Assuming a field `lastRequestDate` in User model
        }

        user.profileImagePath?.let { path ->
            if (path.isNotEmpty()) {
                Glide.with(this@MainActivity)
                    .load(path)
                    .placeholder(R.drawable.profile)
                    .into(navProfileImage)
            } else {
                navProfileImage.setImageResource(R.drawable.profile)
            }
        } ?: navProfileImage.setImageResource(R.drawable.profile)
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

    private fun startEmergencyRequest() {
        val userType = mainViewModel.userType.value
        val intent: Intent = if (userType == "recipient") {
            Intent(this@MainActivity, EmergencyRequestActivity::class.java)
        } else {
            Intent(this@MainActivity, EmergencyRequestListActivity::class.java)
        }
        startActivity(intent)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId

        when (itemId) {
            R.id.emergency_request -> {
                val userType = mainViewModel.userType.value
                val intent: Intent = if (userType == "recipient") {
                    Intent(this@MainActivity, EmergencyRequestActivity::class.java)
                } else {
                    Intent(this@MainActivity, EmergencyRequestListActivity::class.java)
                }
                startActivity(intent)
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
            R.id.logout -> mainViewModel.logout()
            R.id.profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

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
                Toast.makeText(this, "Google Play Services is now available", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Google Play Services is required for this app", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Removed onDestroy listener removal as ViewModel handles Flow collection lifecycle
    // Note: If DonationReminderService depends on MainActivity's direct Firebase interactions,
    // it will also need refactoring to use the UserRepository/ViewModel pattern.

    companion object {
        private const val TAG = "MainActivity"
    }
}