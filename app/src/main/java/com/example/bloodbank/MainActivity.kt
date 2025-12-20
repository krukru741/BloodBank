package com.example.bloodbank

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
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
import com.example.bloodbank.worker.DonationReminderScheduler
import com.example.bloodbank.utils.GooglePlayServicesUtils
import com.example.bloodbank.viewmodel.MainViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

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

        checkGooglePlayServices()

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
        recycleView.layoutManager = LinearLayoutManager(this).apply {
            reverseLayout = true
            stackFromEnd = true
        }

        userList = ArrayList()
        userAdapter = UserAdapter(userList)
        recycleView.adapter = userAdapter

        fabEmergency.visibility = View.GONE
        fabEmergency.setOnClickListener { startEmergencyRequest() }

        observeViewModel()
        mainViewModel.checkUserTypeAndLoadContent()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    mainViewModel.currentUser.collectLatest { user ->
                        user?.let {
                            updateNavigationHeader(it)
                            if (it.type == "donor") {
                                // DonationReminderScheduler.scheduleReminder(this@MainActivity)
                            }
                        }
                    }
                }
                launch {
                    mainViewModel.userType.collectLatest { type ->
                        type?.let {
                            if (it == "donor") {
                                showDonorInfo()
                                fabEmergency.visibility = View.GONE
                            } else {
                                showRecipientInfo()
                                fabEmergency.visibility = View.VISIBLE
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
                        if (donors.isEmpty() && mainViewModel.userType.value == "recipient") {
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
                        if (recipients.isEmpty() && mainViewModel.userType.value == "donor") {
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
                    mainViewModel.logoutEvent.collectLatest {
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
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
            navLastRequest.text = "N/A"
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
        // Only update UI if navigation header has been initialized
        if (::additionalInfoSection.isInitialized) {
            additionalInfoSection.visibility = View.VISIBLE
            donorInfo.visibility = View.VISIBLE
            recipientInfo.visibility = View.GONE
        }
    }

    private fun showRecipientInfo() {
        // Only update UI if navigation header has been initialized
        if (::additionalInfoSection.isInitialized) {
            additionalInfoSection.visibility = View.VISIBLE
            donorInfo.visibility = View.GONE
            recipientInfo.visibility = View.VISIBLE
        }
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
        when (item.itemId) {
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
            R.id.aplus -> navigateToCategory("A+")
            R.id.aminus -> navigateToCategory("A-")
            R.id.bplus -> navigateToCategory("B+")
            R.id.bminus -> navigateToCategory("B-")
            R.id.abplus -> navigateToCategory("AB+")
            R.id.abminus -> navigateToCategory("AB-")
            R.id.oplus -> navigateToCategory("O+")
            R.id.ominus -> navigateToCategory("O-")
            R.id.compatible -> startActivity(Intent(this, CompatibleUsersActivity::class.java))
            R.id.notifications -> startActivity(Intent(this, NotificationsActivity::class.java))
            R.id.aboutus -> startActivity(Intent(this, AboutUsActivity::class.java))
            R.id.Faq -> startActivity(Intent(this, FaqActivity::class.java))
            R.id.sentEmail -> startActivity(Intent(this, SentEmailActivity::class.java))
            R.id.logout -> mainViewModel.logout()
            R.id.profile -> startActivity(Intent(this, ProfileActivity::class.java))
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun navigateToCategory(group: String) {
        val intent = Intent(this, CategorySelectedActivity::class.java)
        intent.putExtra("group", group)
        startActivity(intent)
    }

    private fun checkGooglePlayServices() {
        if (!GooglePlayServicesUtils.isGooglePlayServicesAvailable(this)) {
            GooglePlayServicesUtils.showGooglePlayServicesUpdateDialog(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 9000) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Google Play Services is now available", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Google Play Services is required for this app", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
