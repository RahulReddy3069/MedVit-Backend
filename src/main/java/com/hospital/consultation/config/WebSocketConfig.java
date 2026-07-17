package com.hospital.consultation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.*;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private static final Map<String, WebSocketSession> doctorSessions = new ConcurrentHashMap<>();

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new DoctorSocketHandler(), "/ws/doctor/*")
                .setAllowedOrigins("*");
    }

    // Stores doctor sessions map for real-time notification broadcasts
    public static class DoctorSocketHandler extends TextWebSocketHandler {
        
        @Override
        public void afterConnectionEstablished(WebSocketSession session) {
            String path = session.getUri().getPath();
            String doctorId = path.substring(path.lastIndexOf('/') + 1);
            doctorSessions.put(doctorId, session);
            System.out.println("Doctor registered WebSocket session: " + doctorId);
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
            doctorSessions.values().remove(session);
        }
    }

    // Broadcast helper method to send events to specific doctor
    public static boolean notifyDoctorOfNewRequest(String doctorId, String jsonPayload) {
        WebSocketSession session = doctorSessions.get(doctorId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(jsonPayload));
                return true;
            } catch (IOException e) {
                System.err.println("Failed to dispatch WebSocket event to doctor " + doctorId + ": " + e.getMessage());
            }
        }
        return false;
    }
}
