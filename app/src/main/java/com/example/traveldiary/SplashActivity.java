package com.example.traveldiary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 2000;

    TextView textView;
    ImageView logoView;
    Animation slideFromTop, slideFromBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        slideFromTop = AnimationUtils.loadAnimation(this, R.anim.slide_from_top);
        slideFromBottom = AnimationUtils.loadAnimation(this, R.anim.slide_from_bottom_splash);

        textView = findViewById(R.id.appName);
        textView.startAnimation(slideFromBottom);
        logoView = findViewById(R.id.logoView);
        logoView.startAnimation(slideFromTop);

        getSupportActionBar().hide();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent homeIntent = new Intent(SplashActivity.this, HomeActivity.class);
                startActivity(homeIntent);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}