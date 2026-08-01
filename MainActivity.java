package com.mitra.ai;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startChatBtn = findViewById(R.id.btn_start_chat);
        FloatingActionButton voiceBtn = findViewById(R.id.fab_voice);
        TextView greetingText = findViewById(R.id.tv_greeting);

        // Set greeting based on time
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour < 12) greetingText.setText("Good Morning! 🌅");
        else if (hour < 17) greetingText.setText("Good Afternoon! ☀️");
        else greetingText.setText("Good Evening! 🌙");

        startChatBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.putExtra("input_mode", "text");
            startActivity(intent);
        });

        voiceBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.putExtra("input_mode", "voice");
            startActivity(intent);
        });
    }
}
