package com.example.bloodbank

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bloodbank.Adapter.MessageAdapter
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch

/**
 * ChatActivity - Displays chat conversation between two users.
 * Follows MVVM architecture pattern with ChatViewModel.
 */
@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {
    
    private val viewModel: ChatViewModel by viewModels()
    
    // Views
    private lateinit var profileImage: CircleImageView
    private lateinit var username: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var toolbar: Toolbar
    
    // Adapter
    private lateinit var messageAdapter: MessageAdapter
    
    // User info from intent
    private var userId: String = ""
    private var profilePic: String = ""
    private var userName: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        
        // Get user info from intent
        userId = intent.getStringExtra("userid") ?: ""
        profilePic = intent.getStringExtra("profilepic") ?: ""
        userName = intent.getStringExtra("username") ?: ""
        
        // Initialize views
        initializeViews()
        
        // Setup toolbar
        setupToolbar()
        
        // Setup RecyclerView
        setupRecyclerView()
        
        // Setup send button
        setupSendButton()
        
        // Load messages
        viewModel.loadMessages(userId)
        
        // Observe ViewModel state
        observeViewModel()
    }
    
    private fun initializeViews() {
        profileImage = findViewById(R.id.profileImage)
        username = findViewById(R.id.username)
        recyclerView = findViewById(R.id.recyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        toolbar = findViewById(R.id.toolbar)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = ""
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        
        username.text = userName
        
        if (profilePic.isNotEmpty()) {
            Glide.with(this)
                .load(profilePic)
                .placeholder(R.drawable.profile_pic)
                .into(profileImage)
        } else {
            profileImage.setImageResource(R.drawable.profile_pic)
        }
    }
    
    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(this, mutableListOf())
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }
    }
    
    private fun setupSendButton() {
        sendButton.setOnClickListener {
            val message = messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                viewModel.sendMessage(message)
                messageInput.setText("")
            }
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe messages
                launch {
                    viewModel.messages.collect { messages ->
                        messageAdapter.updateMessages(messages)
                        if (messages.isNotEmpty()) {
                            recyclerView.scrollToPosition(messages.size - 1)
                        }
                    }
                }
                
                // Observe errors
                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            Toast.makeText(this@ChatActivity, it, Toast.LENGTH_SHORT).show()
                            viewModel.clearError()
                        }
                    }
                }
                
                // Observe loading state (optional - can show progress indicator)
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        // Optional: show/hide loading indicator
                    }
                }
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
