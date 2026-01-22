package com.tunfin.identity.controller;

import com.tunfin.identity.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@RestController
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("userId") UUID userId,
            @RequestParam("type") String type,
            @RequestParam("file") MultipartFile file) {
        kycService.uploadDocument(userId, type, file);
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
}
