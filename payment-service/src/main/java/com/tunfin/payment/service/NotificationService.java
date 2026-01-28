package com.tunfin.payment.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    @Autowired(required = false)
    private FirebaseMessaging firebaseMessaging;

    public void sendPushNotification(String token, String title, String body) {
        if (token == null || token.isEmpty()) {
            log.warn(">>> PUSH: No FCM token for user, skipping notification.");
            return;
        }

        if (firebaseMessaging == null) {
            log.info(">>> PUSH (MOCK): To: {}, Title: {}, Body: {}", token, title, body);
            return;
        }

        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = firebaseMessaging.send(message);
            log.info(">>> PUSH: Successfully sent message: " + response);
        } catch (Exception e) {
            log.error(">>> PUSH: Failed to send notification to {}: {}", token, e.getMessage());
        }
    }
}
