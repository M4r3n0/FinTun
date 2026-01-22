package com.tunfin.identity.controller;

import com.tunfin.identity.model.User;
import com.tunfin.identity.repository.UserRepository;
import com.tunfin.identity.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final KycService kycService; // Assuming we might need this later

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        // In real app, use DTO to hide password/sensitive info
        return ResponseEntity.ok(userRepository.findAll());
    }
}
