package com.tunfin.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IdentityServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }

    @org.springframework.context.annotation.Bean
    public org.springframework.boot.CommandLineRunner seedAdmin(
            com.tunfin.identity.repository.UserRepository userRepository,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        return args -> {
            var logger = org.slf4j.LoggerFactory.getLogger(IdentityServiceApplication.class);

            // 1. Data Migration: Normalize existing phone numbers
            logger.info(">>> MIGRATION: Normalizing existing phone numbers...");
            userRepository.findAll().forEach(u -> {
                String original = u.getPhoneNumber();
                String normalized = com.tunfin.identity.service.AuthService.normalizePhone(original);
                if (!original.equals(normalized)) {
                    u.setPhoneNumber(normalized);
                    userRepository.save(u);
                    logger.info(">>> MIGRATION: Normalized {} -> {}", original, normalized);
                }
            });

            var adminId = java.util.UUID.fromString("00000000-0000-0000-0000-000000000001");
            var phoneNumber = "21611111111"; // Use no spaces for reliability

            userRepository.findByPhoneNumber(phoneNumber).ifPresent(u -> {
                if (!u.getId().equals(adminId)) {
                    logger.info(">>> SEED: Removing conflicting user with phone: {}", phoneNumber);
                    userRepository.delete(u);
                }
            });

            logger.info(">>> SEED: Synchronizing Master Admin Account...");
            var admin = com.tunfin.identity.model.User.builder()
                    .id(adminId)
                    .phoneNumber(phoneNumber)
                    .passwordHash(passwordEncoder.encode("password123"))
                    .fullName("System Administrator")
                    .email("admin@tunfin.com")
                    .role(com.tunfin.identity.model.Role.ADMIN)
                    .nationalId("AD123456")
                    .address("TunFin HQ")
                    .dateOfBirth(java.time.LocalDate.of(1980, 1, 1))
                    .build();
            userRepository.save(admin);
            logger.info(">>> SEED: Master Admin synchronized: {} / password123", phoneNumber);
        };
    }
}
