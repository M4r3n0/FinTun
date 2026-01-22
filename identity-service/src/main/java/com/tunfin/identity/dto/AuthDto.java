package com.tunfin.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthDto {
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RegisterRequest {
        @jakarta.validation.constraints.NotBlank(message = "Phone number is required")
        private String phoneNumber;

        @jakarta.validation.constraints.NotBlank(message = "Password is required")
        private String password;

        @jakarta.validation.constraints.NotBlank(message = "Full name is required")
        private String fullName;

        @jakarta.validation.constraints.NotBlank(message = "National ID is required")
        private String nationalId;

        @jakarta.validation.constraints.Email(message = "Invalid email format")
        @jakarta.validation.constraints.NotBlank(message = "Email is required")
        private String email;

        @jakarta.validation.constraints.NotBlank(message = "Address is required")
        private String address;

        @jakarta.validation.constraints.Past(message = "Date of Birth must be in the past")
        private java.time.LocalDate dateOfBirth;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthRequest {
        private String phoneNumber;
        private String password;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthResponse {
        private String token;
        private String userId;
    }
}
