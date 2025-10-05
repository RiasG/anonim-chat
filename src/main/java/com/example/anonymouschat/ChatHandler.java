package com.example.anonymouschat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ChatHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final AtomicLong userIdCounter = new AtomicLong(0);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = "user-" + userIdCounter.incrementAndGet();
        session.getAttributes().put("userId", userId);
        sessions.put(userId, session);

        // Оповестить всех, что новый пользователь подключился (опционально)
        broadcastMessage("[" + userId + "] присоединился к чату.");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        String payload = message.getPayload();

        // Очистка сообщения от потенциально опасных символов (минимальная защита)
        String cleanContent = payload.trim().replaceAll("<", "<").replaceAll(">", ">");

        ChatMessage chatMessage = new ChatMessage("[" + userId + "]: " + cleanContent);
        String jsonMessage = objectMapper.writeValueAsString(chatMessage);

        broadcast(jsonMessage);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        sessions.remove(userId);
        broadcastMessage("[" + userId + "] покинул чат.");
    }

    private void broadcast(String message) {
        sessions.values().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (IOException e) {
                // Логирование ошибки (в реальном проекте используйте логгер)
                e.printStackTrace();
            }
        });
    }

    private void broadcastMessage(String text) {
        try {
            broadcast(objectMapper.writeValueAsString(new ChatMessage(text)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}