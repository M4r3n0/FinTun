package com.tunfin.identity.controller;

import com.tunfin.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepository;

    @GetMapping("/search")
    public ResponseEntity<Map<String, String>> searchByPhone(@RequestParam String phoneNumber) {
        String cleanPhone = com.tunfin.identity.service.AuthService.normalizePhone(phoneNumber);
        logger.info(">>> SEARCH: Input phone: '{}', Normalized: '{}'", phoneNumber, cleanPhone);

        return userRepository.findByPhoneNumber(cleanPhone)
                .map(user -> {
                    logger.info(">>> SEARCH: Found user: {}", user.getFullName());
                    Map<String, String> res = new HashMap<>();
                    res.put("userId", user.getId().toString());
                    res.put("fullName", user.getFullName());
                    return ResponseEntity.ok(res);
                })
                .orElseGet(() -> {
                    logger.warn(">>> SEARCH: User NOT FOUND for normalized phone: '{}'", cleanPhone);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, String>> getUserById(@PathVariable java.util.UUID id) {
        return userRepository.findById(id)
                .map(user -> {
                    Map<String, String> res = new HashMap<>();
                    res.put("userId", user.getId().toString());
                    res.put("fullName", user.getFullName());
                    res.put("email", user.getEmail());
                    res.put("fcmToken", user.getFcmToken());
                    return ResponseEntity.ok(res);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/fcm-token")
    public ResponseEntity<?> updateFcmToken(@RequestParam java.util.UUID userId, @RequestParam String token) {
        return userRepository.findById(userId)
                .map(user -> {
                    user.setFcmToken(token);
                    userRepository.save(user);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
