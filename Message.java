package com.mitra.ai.models;

public class Message {
    public static final int TYPE_USER = 0;
    public static final int TYPE_BOT = 1;
    public static final int TYPE_TYPING = 2;

    private String text;
    private int type;
    private long timestamp;

    public Message(String text, int type) {
        this.text = text;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public int getType() { return type; }
    public long getTimestamp() { return timestamp; }
}
