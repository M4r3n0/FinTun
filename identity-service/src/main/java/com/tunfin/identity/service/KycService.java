package com.tunfin.identity.service;

import com.tunfin.identity.model.KycDocument;
import com.tunfin.identity.model.KycLevel;
import com.tunfin.identity.model.User;
import com.tunfin.identity.repository.KycDocumentRepository;
import com.tunfin.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import okhttp3.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KycService {

    private final KycDocumentRepository kycDocumentRepository;
    private final UserRepository userRepository;

    @Value("${sumsub.app.token}")
    private String appToken;

    @Value("${sumsub.secret.key}")
    private String secretKey;

    @Value("${sumsub.api.url}")
    private String apiUrl;

    private final OkHttpClient httpClient = new OkHttpClient();

    public String generateSumsubAccessToken(UUID userId, String levelName) {
        if (appToken == null || appToken.isEmpty() || appToken.contains("${") ||
                secretKey == null || secretKey.isEmpty() || secretKey.contains("${")) {
            log.warn(">>> Sumsub: Credentials not configured properly. Falling back to MOCK.");
            return "MOCK_TOKEN_" + userId;
        }
        // levelName is usually "basic-kyc-level" or similar in Sumsub dashboard
        long ts = Instant.now().getEpochSecond();
        String method = "POST";
        String path = "/resources/accessTokens?userId=" + userId + "&levelName=" + levelName;

        String signature = hmacSha256(ts + method + path, secretKey);

        Request request = new Request.Builder()
                .url(apiUrl + path)
                .post(RequestBody.create("", MediaType.parse("application/json")))
                .addHeader("X-App-Token", appToken)
                .addHeader("X-App-Access-Sig", signature)
                .addHeader("X-App-Access-Ts", String.valueOf(ts))
                .build();

        try (
                Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String body = response.body().string();
                // Simple JSON parse for "token"
                return body.split("\"token\":\"")[1].split("\"")[0];
            } else {
                log.error("Sumsub Error: {} - {}", response.code(), response.message());
                throw new RuntimeException("Failed to generate Sumsub token");
            }
        } catch (Exception e) {
            log.error("Sumsub Token Exception: {}", e.getMessage());
            return "MOCK_TOKEN_" + userId; // Fallback for demo
        }
    }

    private String hmacSha256(String data, String key) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("HMAC error", e);
        }
    }

    @Transactional
    public KycDocument uploadDocument(UUID userId, MultipartFile file, KycDocument.DocumentType type) {
        log.info("Uploading KYC document for user: {}", userId);

        try {
            // Ensure upload directory exists
            java.nio.file.Path uploadPath = java.nio.file.Paths.get("uploads", "kyc");
            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String filename = userId + "_" + type + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            java.nio.file.Path filePath = uploadPath.resolve(filename);

            // Save file
            java.nio.file.Files.copy(file.getInputStream(), filePath,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // URL that will be served by our controller
            String fileUrl = "/api/kyc/view/" + filename;

            KycDocument document = KycDocument.builder()
                    .userId(userId)
                    .type(type)
                    .documentType(type)
                    .fileUrl(fileUrl)
                    .filePath(filePath.toString())
                    .status(KycDocument.KycStatus.PENDING)
                    .build();

            document = kycDocumentRepository.save(document);

            // Update User Status
            User user = userRepository.findById(userId).orElseThrow();
            if (user.getKycLevel() == KycLevel.UNVERIFIED || user.getKycLevel() == KycLevel.REJECTED) {
                user.setKycLevel(KycLevel.PENDING_VERIFICATION);
                userRepository.save(user);
            }

            // Trigger Async Analysis (Simulated)
            triggerAiAnalysis(document);

            return document;
        } catch (java.io.IOException e) {
            log.error("Failed to save KYC document: {}", e.getMessage());
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    private void triggerAiAnalysis(KycDocument document) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(2000); // Simulate processing delay

                // MOCK OCR LOGIC
                if (document.getType() == KycDocument.DocumentType.ID_CARD_FRONT) {
                    // Simulate extracting data
                    String extractedJson = "{\"name\": \"Tunisian Citizen\", \"id\": \"12345678\", \"dob\": \"1990-01-01\"}";
                    document.setExtractedData(extractedJson);
                    document.setConfidenceScore(0.98);
                    log.info("OCR Completed for doc: {}", document.getId());
                } else if (document.getType() == KycDocument.DocumentType.SELFIE) {
                    // Simulate Liveness / Face Match
                    document.setConfidenceScore(0.99); // High match
                    log.info("Face Match Completed for doc: {}", document.getId());
                }

                // document.setStatus(KycDocument.KycStatus.APPROVED); // DISABLED auto-verify
                // for manual review flow
                kycDocumentRepository.save(document);

                // Auto-Verify User if all docs present (Simplified) - DISABLED for manual
                // review
                // checkAndVerifyUser(document.getUserId());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private void checkAndVerifyUser(UUID userId) {
        List<KycDocument> docs = kycDocumentRepository.findByUserId(userId);
        boolean hasId = docs.stream().anyMatch(d -> d.getType() == KycDocument.DocumentType.ID_CARD_FRONT
                && d.getStatus() == KycDocument.KycStatus.APPROVED);
        boolean hasSelfie = docs.stream().anyMatch(
                d -> d.getType() == KycDocument.DocumentType.SELFIE && d.getStatus() == KycDocument.KycStatus.APPROVED);

        if (hasId && hasSelfie) {
            User user = userRepository.findById(userId).orElseThrow();
            user.setKycLevel(KycLevel.VERIFIED);
            userRepository.save(user);
            log.info("User {} is now VERIFIED", userId);
        }
    }

    public List<KycDocument> getUserDocuments(UUID userId) {
        return kycDocumentRepository.findByUserId(userId);
    }

    // Admin Review
    public List<User> getPendingUsers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getKycLevel() == KycLevel.PENDING_VERIFICATION)
                .toList();
    }

    public void processManualReview(UUID userId, boolean approve) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setKycLevel(approve ? KycLevel.VERIFIED : KycLevel.REJECTED);
        userRepository.save(user);

        // Update documents
        List<KycDocument> docs = kycDocumentRepository.findByUserId(userId);
        if (approve) {
            if (docs.isEmpty()) {
                // Ensure UI sees a verified document in demo mode
                KycDocument mockDoc = KycDocument.builder()
                        .userId(userId)
                        .type(KycDocument.DocumentType.ID_CARD_FRONT)
                        .documentType(KycDocument.DocumentType.ID_CARD_FRONT)
                        .fileUrl("https://mock.sumsub.com/demo.png")
                        .filePath("mock/demo.png")
                        .status(KycDocument.KycStatus.APPROVED)
                        .build();
                kycDocumentRepository.save(mockDoc);
                log.info(">>> KYC: Created mock document for verified user: {}", userId);
            } else {
                docs.forEach(d -> {
                    if (d.getStatus() != KycDocument.KycStatus.APPROVED) {
                        d.setStatus(KycDocument.KycStatus.APPROVED);
                        kycDocumentRepository.save(d);
                    }
                });
            }
        } else {
            // REJECT case
            docs.forEach(d -> {
                if (d.getStatus() != KycDocument.KycStatus.REJECTED) {
                    d.setStatus(KycDocument.KycStatus.REJECTED);
                    kycDocumentRepository.save(d);
                }
            });
        }
    }

    @Deprecated
    public void reviewUserKyc(UUID userId, boolean approve) {
        processManualReview(userId, approve);
    }
}
