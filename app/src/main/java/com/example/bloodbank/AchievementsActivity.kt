package com.example.bloodbank

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bloodbank.Adapter.AchievementAdapter
import com.example.bloodbank.Adapter.BadgeAdapter
import com.example.bloodbank.Model.DonorAchievement
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * AchievementsActivity - Display donor achievements, badges, and rankings.
 * Simple Firestore query activity.
 */
class AchievementsActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var totalPointsText: TextView
    private lateinit var rankText: TextView
    private lateinit var streakText: TextView
    private lateinit var badgesRecyclerView: RecyclerView
    private lateinit var leaderboardRecyclerView: RecyclerView
    
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)
        
        initializeViews()
        setupToolbar()
        setupRecyclerViews()
        loadAchievements()
        loadLeaderboard()
    }
    
    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        // Fixed: ID was totalPointsText in code but pointsText in XML
        totalPointsText = findViewById(R.id.pointsText)
        rankText = findViewById(R.id.rankText)
        streakText = findViewById(R.id.streakText)
        badgesRecyclerView = findViewById(R.id.badgesRecyclerView)
        // Fixed: ID was leaderboardRecyclerView in code but achievementsRecyclerView in XML
        leaderboardRecyclerView = findViewById(R.id.achievementsRecyclerView)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Achievements"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }
    
    private fun setupRecyclerViews() {
        badgesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        leaderboardRecyclerView.layoutManager = LinearLayoutManager(this)
    }
    
    private fun loadAchievements() {
        val userId = firebaseAuth.currentUser?.uid ?: return
        
        firestore.collection("donor_achievements")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val achievement = document.toObject(DonorAchievement::class.java)
                achievement?.let {
                    totalPointsText.text = it.totalPoints.toString()
                    rankText.text = "#${it.rank}"
                    streakText.text = it.donationStreak.toString()
                    
                    // Fixed: Converted Map to List<String> to match BadgeAdapter constructor
                    val earnedBadges = it.badges.filter { entry -> entry.value }.keys.toList()
                    val badgeAdapter = BadgeAdapter(earnedBadges)
                    badgesRecyclerView.adapter = badgeAdapter
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading achievements", Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun loadLeaderboard() {
        firestore.collection("donor_achievements")
            .orderBy("totalPoints", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                val achievements = documents.mapNotNull { it.toObject(DonorAchievement::class.java) }
                val achievementAdapter = AchievementAdapter(achievements)
                leaderboardRecyclerView.adapter = achievementAdapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading leaderboard", Toast.LENGTH_SHORT).show()
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
