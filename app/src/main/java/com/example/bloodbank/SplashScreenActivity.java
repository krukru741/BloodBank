package com.example.bloodbank;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class SplashScreenActivity extends AppCompatActivity {
    private TextView title, title1, title2;

    Animation topAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        title = findViewById(R.id.title);
        title1 = findViewById(R.id.title1);
        title2 = findViewById(R.id.title2);

        topAnimation = AnimationUtils.loadAnimation(this, R.anim.top_animation);

        title.setAnimation(topAnimation);
        title1.setAnimation(topAnimation);
        title2.setAnimation(topAnimation);

        int SPLASHSREEN = 4300;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();

            }

        }, SPLASHSREEN);

    }
}