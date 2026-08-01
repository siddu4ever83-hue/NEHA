package com.mitra.ai;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mitra.ai.adapters.ChatAdapter;
import com.mitra.ai.api.GeminiAPI;
import com.mitra.ai.models.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private RecyclerView recyclerView;
    private EditText inputEditText;
    private ImageButton sendButton, voiceButton, backButton;
    private ChatAdapter chatAdapter;
    private List<Message> messageList;
    private GeminiAPI geminiAPI;
    private TextToSpeech textToSpeech;
    private boolean ttsReady = false;

    private static final int VOICE_REQUEST_CODE = 100;
    private static final int PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();
        setupRecyclerView();
        setupListeners();

        geminiAPI = new GeminiAPI(this);
        textToSpeech = new TextToSpeech(this, this);

        // Add welcome message
        addBotMessage("నమస్కారం! నేను MITRA AI. మీకు ఏవిధంగా సహాయపడగలను? 😊\n\nHello! I'm MITRA AI. How can I help you today?");

        // Check if launched with voice mode
        String mode = getIntent().getStringExtra("input_mode");
        if ("voice".equals(mode)) {
            startVoiceInput();
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_chat);
        inputEditText = findViewById(R.id.et_message);
        sendButton = findViewById(R.id.btn_send);
        voiceButton = findViewById(R.id.btn_voice);
        backButton = findViewById(R.id.btn_back);
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(chatAdapter);
    }

    private void setupListeners() {
        sendButton.setOnClickListener(v -> sendMessage());

        voiceButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_CODE);
            } else {
                startVoiceInput();
            }
        });

        backButton.setOnClickListener(v -> finish());

        inputEditText.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void sendMessage() {
        String userInput = inputEditText.getText().toString().trim();
        if (TextUtils.isEmpty(userInput)) return;

        // Add user message
        messageList.add(new Message(userInput, Message.TYPE_USER));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
        inputEditText.setText("");

        // Show typing indicator
        addTypingMessage();

        // Call Gemini API
        geminiAPI.sendMessage(userInput, new GeminiAPI.ResponseCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    // Remove typing indicator
                    removeTypingMessage();
                    // Add bot response
                    addBotMessage(response);
                    // Speak response
                    speakText(response);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    removeTypingMessage();
                    addBotMessage("క్షమించండి, ఒక సమస్య వచ్చింది. దయచేసి మళ్ళీ ప్రయత్నించండి.\nSorry, an error occurred: " + error);
                });
            }
        });
    }

    private void addBotMessage(String text) {
        messageList.add(new Message(text, Message.TYPE_BOT));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void addTypingMessage() {
        messageList.add(new Message("...", Message.TYPE_TYPING));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void removeTypingMessage() {
        for (int i = messageList.size() - 1; i >= 0; i--) {
            if (messageList.get(i).getType() == Message.TYPE_TYPING) {
                messageList.remove(i);
                chatAdapter.notifyItemRemoved(i);
                break;
            }
        }
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "te-IN");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "te-IN");
        intent.putExtra(RecognizerIntent.EXTRA_ALSO_NOTIFY_ABOUT_CHANGES, true);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "మాట్లాడండి... / Speak now...");
        try {
            startActivityForResult(intent, VOICE_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "Voice input not supported", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VOICE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                inputEditText.setText(results.get(0));
                sendMessage();
            }
        }
    }

    private void speakText(String text) {
        if (ttsReady) {
            // Remove markdown for TTS
            String cleanText = text.replaceAll("\\*", "")
                    .replaceAll("#", "")
                    .replaceAll("\\n", " ");
            textToSpeech.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(new Locale("te", "IN"));
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                textToSpeech.setLanguage(Locale.ENGLISH);
            }
            ttsReady = true;
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
