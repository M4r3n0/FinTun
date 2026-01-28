package com.tunfin.payment.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        try {
            ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
            if (!resource.exists()) {
                log.warn(
                        ">>> FIREBASE: firebase-service-account.json not found. Push notifications will run in MOCK mode.");
                return null;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info(">>> FIREBASE: Initialized successfully.");
            }

            return FirebaseMessaging.getInstance();
        } catch (IOException e) {
            log.error(">>> FIREBASE: Failed to initialize: {}", e.getMessage());
            return null;
        }
    }
}
