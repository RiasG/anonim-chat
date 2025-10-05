package com.example.anonymouschat;

public class ChatMessage {
    private String content;
    private long timestamp;

    public ChatMessage() {
    }

    public ChatMessage(String content) {
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    // Геттеры и сеттеры
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}