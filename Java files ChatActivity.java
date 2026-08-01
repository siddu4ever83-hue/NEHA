package com.mitra.ai;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mitra.ai.adapter.ChatAdapter;
import com.mitra.ai.api.GeminiApi;
import com.mitra.ai.model.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etInput;
    private ImageButton btnSend, btnVoice, btnBack;
    private ChatAdapter adapter;
    private List<Message> messages = new ArrayList<>();
    private GeminiApi geminiApi;
    private TextToSpeech tts;
    private boolean ttsReady = false;

    private static final int RC_VOICE = 100;
    private static final int RC_PERMISSION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Init views
        rvChat = findViewById(R.id.rv_chat);
        etInput = findViewById(R.id.et_input);
        btnSend = findViewById(R.id.btn_send);
        btnVoice = findViewById(R.id.btn_voice);
        btnBack = findViewById(R.id.btn_back);

        // Setup RecyclerView
        adapter = new ChatAdapter(messages, this);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        rvChat.setLayoutManager(lm);
        rvChat.setAdapter(adapter);

        // Init API
        geminiApi = new GeminiApi();

        // Init TTS
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int res = tts.setLanguage(new Locale("te", "IN"));
                if (res < 0) tts.setLanguage(Locale.ENGLISH);
                ttsReady = true;
            }
        });

        // Welcome message
        addBotMessage(getString(R.string.welcome_message));

        // Listeners
        btnSend.setOnClickListener(v -> sendMessage());
        btnBack.setOnClickListener(v -> finish());
        btnVoice.setOnClickListener(v -> checkPermissionAndStartVoice());
    }

    private void sendMessage() {
        String text = etInput.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        etInput.setText("");

        // Add user message
        messages.add(new Message(text, Message.TYPE_USER));
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();

        // Add typing indicator
        messages.add(new Message("typing", Message.TYPE_TYPING));
        int typingIndex = messages.size() - 1;
        adapter.notifyItemInserted(typingIndex);
        scrollToBottom();

        // Call API
        geminiApi.sendMessage(text, new GeminiApi.Callback() {
            @Override
            public void onResponse(String response) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    // Remove typing
                    if (typingIndex < messages.size() &&
                            messages.get(typingIndex).getType() == Message.TYPE_TYPING) {
                        messages.remove(typingIndex);
                        adapter.notifyItemRemoved(typingIndex);
                    }
                    // Add response
                    addBotMessage(response);
                    // Speak
                    if (ttsReady) {
                        String clean = response.replaceAll("[*#]", "");
                        tts.speak(clean, TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                });
            }

            @Override
            public void onError(String error) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (typingIndex < messages.size() &&
                            messages.get(typingIndex).getType() == Message.TYPE_TYPING) {
                        messages.remove(typingIndex);
                        adapter.notifyItemRemoved(typingIndex);
                    }
                    addBotMessage("Error: " + error + "\n\nAPI Key check చేయండి!");
                });
            }
        });
    }

    private void addBotMessage(String text) {
        messages.add(new Message(text, Message.TYPE_BOT));
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    private void scrollToBottom() {
        rvChat.post(() -> rvChat.smoothScrollToPosition(messages.size() - 1));
    }

    private void checkPermissionAndStartVoice() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, RC_PERMISSION);
        } else {
            startVoice();
        }
    }

    private void startVoice() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "te-IN");
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.voice_prompt));
        try {
            startActivityForResult(i, RC_VOICE);
        } catch (Exception e) {
            // Try English if Telugu not available
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            try {
                startActivityForResult(i, RC_VOICE);
            } catch (Exception ex) {
                Toast.makeText(this, "Voice not supported", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == RC_VOICE && res == RESULT_OK && data != null) {
            ArrayList<String> results =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                etInput.setText(results.get(0));
                sendMessage();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int req, @NonNull String[] perms,
                                           @NonNull int[] results) {
        super.onRequestPermissionsResult(req, perms, results);
        if (req == RC_PERMISSION && results.length > 0
                && results[0] == PackageManager.PERMISSION_GRANTED) {
            startVoice();
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
