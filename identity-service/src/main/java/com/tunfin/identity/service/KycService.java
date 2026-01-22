package com.tunfin.identity.service;

import com.tunfin.identity.model.KycDocument;
import com.tunfin.identity.model.KycLevel;
import com.tunfin.identity.model.User;
import com.tunfin.identity.repository.KycDocumentRepository;
import com.tunfin.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    @Transactional
    public KycDocument uploadDocument(UUID userId, MultipartFile file, KycDocument.DocumentType type) {
        log.info("Uploading KYC document for user: {}", userId);

        // Simulator: "Store" file by creating a mock URL using filename
        String mockUrl = "https://s3.tunfin.com/kyc/" + userId + "/" + file.getOriginalFilename();

        KycDocument document = KycDocument.builder()
                .userId(userId)
                .type(type)
                .fileUrl(mockUrl)
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

                document.setStatus(KycDocument.KycStatus.VERIFIED); // Auto-verify doc for demo
                kycDocumentRepository.save(document);

                // Auto-Verify User if all docs present (Simplified)
                checkAndVerifyUser(document.getUserId());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private void checkAndVerifyUser(UUID userId) {
        List<KycDocument> docs = kycDocumentRepository.findByUserId(userId);
        boolean hasId = docs.stream().anyMatch(d -> d.getType() == KycDocument.DocumentType.ID_CARD_FRONT
                && d.getStatus() == KycDocument.KycStatus.VERIFIED);
        boolean hasSelfie = docs.stream().anyMatch(
                d -> d.getType() == KycDocument.DocumentType.SELFIE && d.getStatus() == KycDocument.KycStatus.VERIFIED);

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

        // Update documents?
        if (approve) {
            List<KycDocument> docs = kycDocumentRepository.findByUserId(userId);
            docs.forEach(d -> {
                if (d.getStatus() != KycDocument.KycStatus.VERIFIED) {
                    d.setStatus(KycDocument.KycStatus.VERIFIED);
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
