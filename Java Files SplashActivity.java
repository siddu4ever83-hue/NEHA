package com.mitra.ai;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.iv_logo);
        TextView name = findViewById(R.id.tv_name);
        TextView tagline = findViewById(R.id.tv_tagline);

        // Scale + Fade animation
        ScaleAnimation scale = new ScaleAnimation(
                0.5f, 1f, 0.5f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(1000);

        AlphaAnimation fade = new AlphaAnimation(0f, 1f);
        fade.setDuration(1200);

        logo.startAnimation(scale);
        name.startAnimation(fade);
        tagline.startAnimation(fade);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(this, ChatActivity.class));
            finish();
        }, 2500);
    }
}
