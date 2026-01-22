package com.tunfin.wallet.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.util.UUID;

@FeignClient(name = "identity-service", url = "http://localhost:8081/api") // Simplified URL for dev
public interface IdentityClient {

    @GetMapping("/users/{id}")
    UserDto getUser(@PathVariable("id") UUID id);

    // Inner DTO matching Identity Service User response
    record UserDto(
            UUID id,
            String fullName,
            LocalDate dateOfBirth,
            String address,
            String role, // USER, ADMIN, MERCHANT
            String kycLevel) {
    }
}
