package com.example.bloodbank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class FaqActivity extends AppCompatActivity {

    TextView heading, content, first, second, third, fourth, fifth;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        // Setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        heading = findViewById(R.id.heading);
        content = findViewById(R.id.content);
        first = findViewById(R.id.first);
        second = findViewById(R.id.second);
        third = findViewById(R.id.third);
        fourth = findViewById(R.id.fourth);
        fifth = findViewById(R.id.fifth);

        content.setText("Did you know one unit of donated blood can save up to three lives? This is because your blood is separated into its " +
                "components (red blood cells, plasma, and platelet). Moreover, blood is needed on a regular basis for people suffering from blood disorders such as thalassemia and hemophilia," +
                " and also for the treatment of injuries after an accident, major surgeries, anemia, etc." +
                " \nWondering if you are eligible for donating blood? \nHere is what you need to know before you plan for blood donation.");

        heading.setTypeface(null, Typeface.BOLD);
        content.setTypeface(null, Typeface.BOLD);
        first.setTypeface(null, Typeface.BOLD);
        second.setTypeface(null, Typeface.BOLD);
        third.setTypeface(null, Typeface.BOLD);
        fourth.setTypeface(null, Typeface.BOLD);
        fifth.setTypeface(null, Typeface.BOLD);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void whyDonate(View view) {
        if (first.getVisibility() == View.GONE) {
            first.setText("Blood donation not only saves lives but also has key benefits that we are unaware of:\n\n" +
                    "1. Health Benefits:\n" +
                    "   • Balances iron levels in the body\n" +
                    "   • Reduces risk of cardiovascular diseases\n" +
                    "   • Stimulates production of new blood cells\n" +
                    "   • Helps in weight management\n" +
                    "   • Improves overall blood circulation\n\n" +
                    "2. Psychological Benefits:\n" +
                    "   • Creates a sense of fulfillment\n" +
                    "   • Reduces stress and anxiety\n" +
                    "   • Boosts self-esteem\n" +
                    "   • Promotes social responsibility\n\n" +
                    "3. Medical Benefits:\n" +
                    "   • Free health check-up before donation\n" +
                    "   • Regular blood pressure monitoring\n" +
                    "   • Hemoglobin level check\n" +
                    "   • Early detection of potential health issues\n\n" +
                    "4. Community Impact:\n" +
                    "   • Helps accident victims\n" +
                    "   • Supports cancer patients\n" +
                    "   • Aids in surgical procedures\n" +
                    "   • Saves lives during emergencies");
            first.setVisibility(View.VISIBLE);
        } else {
            first.setVisibility(View.GONE);
        }
    }

    public void eligible(View view) {
        if (second.getVisibility() == View.GONE) {
            second.setText("To ensure the safety of both donors and recipients, there are specific eligibility criteria:\n\n" +
                    "1. Basic Requirements:\n" +
                    "   • Age: 18-65 years\n" +
                    "   • Weight: Minimum 45 kg\n" +
                    "   • Pulse: 60-100 beats per minute\n" +
                    "   • Body Temperature: 98.6°F (37°C)\n" +
                    "   • Hemoglobin: Minimum 12.5 g/dL\n" +
                    "   • Blood Pressure: 120/80 mm Hg\n\n" +
                    "2. Health Conditions:\n" +
                    "   • Must be in good health\n" +
                    "   • No active infections\n" +
                    "   • No chronic diseases\n" +
                    "   • No recent surgeries\n\n" +
                    "3. Lifestyle Factors:\n" +
                    "   • No alcohol consumption 24 hours before donation\n" +
                    "   • No smoking 2 hours before donation\n" +
                    "   • Adequate sleep the night before\n" +
                    "   • Proper hydration\n\n" +
                    "4. Donation Frequency:\n" +
                    "   • Men: Every 3 months\n" +
                    "   • Women: Every 4 months\n" +
                    "   • Maximum 4 donations per year");
            second.setVisibility(View.VISIBLE);
        } else {
            second.setVisibility(View.GONE);
        }
    }

    public void notEligible(View view) {
        if (third.getVisibility() == View.GONE) {
            third.setText("You cannot donate blood if you:\n\n" +
                    "1. Health Conditions:\n" +
                    "   • Active infections (cold, flu, etc.)\n" +
                    "   • Chronic diseases (diabetes, heart disease)\n" +
                    "   • Blood disorders\n" +
                    "   • Cancer or history of cancer\n" +
                    "   • HIV/AIDS or other STDs\n\n" +
                    "2. Recent Medical Procedures:\n" +
                    "   • Dental work within 24 hours\n" +
                    "   • Surgery within 6 months\n" +
                    "   • Tattoos or piercings within 12 months\n" +
                    "   • Acupuncture within 12 months\n\n" +
                    "3. Lifestyle Factors:\n" +
                    "   • Intravenous drug use\n" +
                    "   • Multiple sexual partners\n" +
                    "   • Recent travel to malaria-endemic areas\n" +
                    "   • History of hepatitis\n\n" +
                    "4. Special Conditions:\n" +
                    "   • Pregnancy or breastfeeding\n" +
                    "   • Recent childbirth (within 1 year)\n" +
                    "   • Menstruation (temporary restriction)\n" +
                    "   • Low iron levels");
            third.setVisibility(View.VISIBLE);
        } else {
            third.setVisibility(View.GONE);
        }
    }

    public void before(View view) {
        if (fourth.getVisibility() == View.GONE) {
            fourth.setText("Preparing for blood donation is crucial for a safe and comfortable experience:\n\n" +
                    "1. Day Before Donation:\n" +
                    "   • Get adequate sleep (7-8 hours)\n" +
                    "   • Avoid alcohol consumption\n" +
                    "   • Stay hydrated (drink plenty of water)\n" +
                    "   • Eat iron-rich foods\n\n" +
                    "2. Morning of Donation:\n" +
                    "   • Eat a healthy breakfast\n" +
                    "   • Drink extra fluids\n" +
                    "   • Avoid fatty foods\n" +
                    "   • Wear comfortable clothing\n\n" +
                    "3. At the Donation Center:\n" +
                    "   • Bring valid ID\n" +
                    "   • Complete health questionnaire\n" +
                    "   • Undergo mini-physical exam\n" +
                    "   • Relax and stay calm\n\n" +
                    "4. Important Reminders:\n" +
                    "   • No smoking 2 hours before\n" +
                    "   • No heavy exercise\n" +
                    "   • No aspirin 48 hours before\n" +
                    "   • No alcohol 24 hours before");
            fourth.setVisibility(View.VISIBLE);
        } else {
            fourth.setVisibility(View.GONE);
        }
    }

    public void after(View view) {
        if (fifth.getVisibility() == View.GONE) {
            fifth.setText("Proper care after blood donation ensures quick recovery:\n\n" +
                    "1. Immediate Aftercare:\n" +
                    "   • Rest for 10-15 minutes\n" +
                    "   • Drink plenty of fluids\n" +
                    "   • Eat light snacks provided\n" +
                    "   • Keep bandage on for 4-6 hours\n\n" +
                    "2. First 24 Hours:\n" +
                    "   • Increase fluid intake\n" +
                    "   • Eat iron-rich foods\n" +
                    "   • Avoid strenuous activities\n" +
                    "   • No heavy lifting\n\n" +
                    "3. Diet Recommendations:\n" +
                    "   • Eat balanced meals\n" +
                    "   • Include protein-rich foods\n" +
                    "   • Consume iron-rich foods\n" +
                    "   • Avoid alcohol and caffeine\n\n" +
                    "4. Activity Guidelines:\n" +
                    "   • No smoking for 24 hours\n" +
                    "   • No alcohol for 24 hours\n" +
                    "   • No heavy exercise for 24 hours\n" +
                    "   • No hot baths or saunas\n\n" +
                    "5. Warning Signs:\n" +
                    "   • Dizziness or lightheadedness\n" +
                    "   • Nausea or vomiting\n" +
                    "   • Bleeding from puncture site\n" +
                    "   • Unusual pain or discomfort");
            fifth.setVisibility(View.VISIBLE);
        } else {
            fifth.setVisibility(View.GONE);
        }
    }
}