package com.example.smartwallet;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    // Variables
    Animation topAnim, bottomAnim;
    ImageView image;
    TextView title, tagline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This line hides the status bar (makes it full screen)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);

        // 1. Load Animations
        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);

        // 2. Find Views
        image = findViewById(R.id.iv_logo);
        title = findViewById(R.id.tv_app_name);
        tagline = findViewById(R.id.tv_tagline);

        // 3. Set Animations
        image.setAnimation(topAnim);
        title.setAnimation(bottomAnim);
        tagline.setAnimation(bottomAnim);

        // 4. Wait 4 seconds, then go to next screen
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            Intent intent;

            // Check if user is logged in
            if (auth.getCurrentUser() != null) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish();
        }, 4000);
    }
}