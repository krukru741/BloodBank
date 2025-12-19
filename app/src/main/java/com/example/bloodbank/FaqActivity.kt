package com.example.bloodbank

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

/**
 * FaqActivity - Displays frequently asked questions using a scrollable card-based layout.
 */
class FaqActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var headingText: TextView
    private lateinit var contentText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faq)
        
        setupToolbar()
        initializeViews()
    }
    
    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "FAQ"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }
    
    private fun initializeViews() {
        headingText = findViewById(R.id.heading)
        contentText = findViewById(R.id.content)
        
        contentText.text = "Blood is the most precious gift that anyone can give to another person — the gift of life. A decision to donate your blood can save a life, or even several if your blood is separated into its components — red cells, platelets and plasma — which can be used individually for patients with specific conditions."
    }
    
    // Toggle functions for the cards as defined in activity_faq.xml (android:onClick)
    
    fun whyDonate(view: View) {
        toggleVisibility(findViewById(R.id.first), "Donating blood has benefits for your emotional and physical health. According to a report by the Mental Health Foundation, helping others can: Reduce stress, improve your emotional well-being, benefit your physical health, help get rid of negative feelings, provide a sense of belonging and reduce isolation.")
    }
    
    fun eligible(view: View) {
        toggleVisibility(findViewById(R.id.second), "To be eligible to donate whole blood, plasma or platelets, you must be: \n• In good health and feeling well\n• At least 16 years old in most states\n• At least 110 pounds")
    }
    
    fun notEligible(view: View) {
        toggleVisibility(findViewById(R.id.third), "You are NOT eligible if you have: \n• HIV/AIDS\n• Hepatitis B or C\n• Ever injected drugs\n• Used clotting factor concentrates\n• Syphilis or Gonorrhea in the past 12 months")
    }
    
    fun before(view: View) {
        toggleVisibility(findViewById(R.id.fourth), "• Have a healthy, low-fat meal\n• Drink plenty of water (an extra 16 oz is recommended)\n• Bring your ID and a list of medications\n• Wear comfortable clothing with sleeves that can be rolled up")
    }
    
    fun after(view: View) {
        toggleVisibility(findViewById(R.id.fifth), "• Drink plenty of fluids for the next 24 to 48 hours\n• Avoid strenuous physical activity or heavy lifting for the rest of the day\n• Keep the bandage on for several hours\n• Eat iron-rich foods")
    }
    
    private fun toggleVisibility(textView: TextView, description: String) {
        if (textView.visibility == View.GONE) {
            textView.text = description
            textView.visibility = View.VISIBLE
        } else {
            textView.visibility = View.GONE
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
