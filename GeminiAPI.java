package com.mitra.ai.api;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import okhttp3.*;
import java.io.IOException;

public class GeminiAPI {

    // 🔑 Replace with your actual Gemini API Key
    private static final String API_KEY = "YOUR_GEMINI_API_KEY_HERE";
    
    private static final String API_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + API_KEY;

    private final OkHttpClient client;
    private final Context context;

    // System prompt for MITRA personality
    private static final String SYSTEM_PROMPT =
        "You are MITRA AI, a helpful, friendly AI assistant. " +
        "You can respond in both Telugu and English. " +
        "You are created to help Telugu-speaking people. " +
        "Be polite, helpful, and concise in your responses. " +
        "If the user speaks in Telugu, respond in Telugu. " +
        "If the user speaks in English, respond in English. " +
        "Your name is MITRA which means 'Friend' in Telugu.";

    public interface ResponseCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    public GeminiAPI(Context context) {
        this.context = context;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
    }

    public void sendMessage(String userMessage, ResponseCallback callback) {
        try {
            // Build request body
            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();

            // Add system context as first message
            JSONObject systemContent = new JSONObject();
            systemContent.put("role", "user");
            JSONArray systemParts = new JSONArray();
            JSONObject systemPart = new JSONObject();
            systemPart.put("text", SYSTEM_PROMPT);
            systemParts.put(systemPart);
            systemContent.put("parts", systemParts);
            contents.put(systemContent);

            // Add model acknowledgment
            JSONObject modelAck = new JSONObject();
            modelAck.put("role", "model");
            JSONArray ackParts = new JSONArray();
            JSONObject ackPart = new JSONObject();
            ackPart.put("text", "I understand. I am MITRA AI, ready to help!");
            ackParts.put(ackPart);
            modelAck.put("parts", ackParts);
            contents.put(modelAck);

            // Add user message
            JSONObject userContent = new JSONObject();
            userContent.put("role", "user");
            JSONArray userParts = new JSONArray();
            JSONObject userPart = new JSONObject();
            userPart.put("text", userMessage);
            userParts.put(userPart);
            userContent.put("parts", userParts);
            contents.put(userContent);

            requestBody.put("contents", contents);

            // Generation config
            JSONObject generationConfig = new JSONObject();
            generationConfig.put("temperature", 0.9);
            generationConfig.put("topK", 1);
            generationConfig.put("topP", 1);
            generationConfig.put("maxOutputTokens", 1024);
            requestBody.put("generationConfig", generationConfig);

            RequestBody body = RequestBody.create(
                    requestBody.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseBody = response.body().string();
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            String text = jsonResponse
                                    .getJSONArray("candidates")
                                    .getJSONObject(0)
                                    .getJSONObject("content")
                                    .getJSONArray("parts")
                                    .getJSONObject(0)
                                    .getString("text");
                            callback.onSuccess(text);
                        } catch (Exception e) {
                            callback.onError("Parse error: " + e.getMessage());
                        }
                    } else {
                        callback.onError("HTTP Error: " + response.code());
                    }
                }
            });

        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
}
