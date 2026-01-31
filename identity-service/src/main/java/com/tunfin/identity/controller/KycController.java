package com.tunfin.identity.controller;

import com.tunfin.identity.service.KycService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
@Slf4j
public class KycController {

    private final KycService kycService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("userId") UUID userId,
            @RequestParam("type") String type,
            @RequestParam("file") MultipartFile file) {
        com.tunfin.identity.model.KycDocument.DocumentType docType = com.tunfin.identity.model.KycDocument.DocumentType
                .valueOf(type);
        kycService.uploadDocument(userId, file, docType);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/documents")
    public ResponseEntity<?> getDocuments(@RequestParam UUID userId) {
        return ResponseEntity.ok(kycService.getUserDocuments(userId));
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingUsers() {
        return ResponseEntity.ok(kycService.getPendingUsers());
    }

    @PostMapping("/review/{userId}")
    public ResponseEntity<?> review(@PathVariable UUID userId, @RequestParam boolean approve) {
        kycService.processManualReview(userId, approve);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> getToken(@RequestParam UUID userId,
            @RequestParam(defaultValue = "basic-kyc-level") String levelName) {
        log.info(">>> KYC: Token requested for user: {}, level: {}", userId, levelName);
        String token = kycService.generateSumsubAccessToken(userId, levelName);
        log.info(">>> KYC: Token generated successfully for user: {}", userId);
        Map<String, String> res = new HashMap<>();
        res.put("token", token);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> webhook(@RequestBody String payload,
            @RequestHeader(value = "X-Hub-Signature", required = false) String signature) {
        log.info(">>> SUMSUB WEBHOOK: {}", payload);
        // Logic to parse JSON and update user status would go here
        return ResponseEntity.ok().build();
    }

    @GetMapping("/view/{filename}")
    public org.springframework.core.io.Resource viewDocument(@PathVariable String filename) {
        java.nio.file.Path path = java.nio.file.Paths.get("uploads", "kyc", filename);
        return new org.springframework.core.io.FileSystemResource(path);
    }
}
