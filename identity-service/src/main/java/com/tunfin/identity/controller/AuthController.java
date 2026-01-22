package com.tunfin.identity.controller;

import com.tunfin.identity.dto.AuthDto;
import com.tunfin.identity.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthDto.AuthResponse> register(
            @jakarta.validation.Valid @RequestBody AuthDto.RegisterRequest request) {
        log.info("Received registration request for: {}", request.getPhoneNumber());
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDto.AuthResponse> authenticate(
            @RequestBody AuthDto.AuthRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }
}
