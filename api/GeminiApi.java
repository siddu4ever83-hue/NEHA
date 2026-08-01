package com.mitra.ai.api;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiApi {

    // ✅ మీ Gemini API Key ఇక్కడ పెట్టండి
    // Get it from: https://makersuite.google.com/app/apikey
    private static final String API_KEY = "YOUR_API_KEY_HERE";

    private static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/" +
            "gemini-1.5-flash:generateContent?key=";

    private static final String SYSTEM_PROMPT =
            "You are MITRA AI (మిత్ర AI), a friendly Telugu-English bilingual AI assistant. " +
            "MITRA means 'Friend' in Telugu. You help Telugu people with any questions. " +
            "If user writes in Telugu, reply in Telugu. " +
            "If user writes in English, reply in English. " +
            "Be friendly, helpful, and concise.";

    private final OkHttpClient client;

    public interface Callback {
        void onResponse(String response);
        void onError(String error);
    }

    public GeminiApi() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public void sendMessage(String userMessage, Callback callback) {
        try {
            // Build JSON
            JSONObject json = new JSONObject();
            JSONArray contents = new JSONArray();

            // System message
            JSONObject sysMsg = new JSONObject();
            sysMsg.put("role", "user");
            JSONArray sysParts = new JSONArray();
            JSONObject sysPart = new JSONObject();
            sysPart.put("text", SYSTEM_PROMPT);
            sysParts.put(sysPart);
            sysMsg.put("parts", sysParts);
            contents.put(sysMsg);

            // Model ack
            JSONObject modelMsg = new JSONObject();
            modelMsg.put("role", "model");
            JSONArray modelParts = new JSONArray();
            JSONObject modelPart = new JSONObject();
            modelPart.put("text", "Understood! I am MITRA AI, your Telugu-English friend. How can I help?");
            modelParts.put(modelPart);
            modelMsg.put("parts", modelParts);
            contents.put(modelMsg);

            // User message
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            JSONArray userParts = new JSONArray();
            JSONObject userPart = new JSONObject();
            userPart.put("text", userMessage);
            userParts.put(userPart);
            userMsg.put("parts", userParts);
            contents.put(userMsg);

            json.put("contents", contents);

            // Config
            JSONObject config = new JSONObject();
            config.put("temperature", 0.8);
            config.put("maxOutputTokens", 1024);
            json.put("generationConfig", config);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(BASE_URL + API_KEY)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String bodyStr = response.body().string();
                        if (!response.isSuccessful()) {
                            callback.onError("HTTP " + response.code() + ": " + bodyStr);
                            return;
                        }
                        JSONObject res = new JSONObject(bodyStr);
                        String text = res
                                .getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text");
                        callback.onResponse(text);
                    } catch (Exception e) {
                        callback.onError("Parse error: " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            callback.onError("Request error: " + e.getMessage());
        }
    }
}
