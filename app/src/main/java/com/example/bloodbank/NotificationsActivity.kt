package com.example.bloodbank

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bloodbank.Adapter.NotificationAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * NotificationsActivity - Displays user notifications.
 * Follows MVVM architecture pattern with NotificationsViewModel.
 */
@AndroidEntryPoint
class NotificationsActivity : AppCompatActivity() {
    
    private val viewModel: NotificationsViewModel by viewModels()
    
    // Views
    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    
    // Adapter
    private lateinit var notificationAdapter: NotificationAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)
        
        // Initialize views
        initializeViews()
        
        // Setup toolbar
        setupToolbar()
        
        // Setup RecyclerView
        setupRecyclerView()
        
        // Observe ViewModel state
        observeViewModel()
    }
    
    private fun initializeViews() {
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recycleView)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Notifications"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }
    
    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(this, mutableListOf())
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@NotificationsActivity).apply {
                reverseLayout = true
                stackFromEnd = true
            }
            adapter = notificationAdapter
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe notifications
                launch {
                    viewModel.notifications.collect { notifications ->
                        notificationAdapter.updateNotifications(notifications)
                    }
                }
                
                // Observe errors
                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            Toast.makeText(this@NotificationsActivity, it, Toast.LENGTH_SHORT).show()
                            viewModel.clearError()
                        }
                    }
                }
                
                // Observe unread count (optional - can show in toolbar)
                launch {
                    viewModel.unreadCount.collect { count ->
                        // Optional: update toolbar subtitle or badge
                        if (count > 0) {
                            supportActionBar?.subtitle = "$count unread"
                        } else {
                            supportActionBar?.subtitle = null
                        }
                    }
                }
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
