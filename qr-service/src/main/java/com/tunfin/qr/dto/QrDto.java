package com.tunfin.qr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class QrDto {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GenerateStaticQrRequest {
        private UUID merchantId;
        private String merchantName;
        private String currency;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GenerateDynamicQrRequest {
        private UUID merchantId;
        private String merchantName;
        private BigDecimal amount;
        private String currency;
        private Integer expiryMinutes; // Optional, defaults to 15
        private String description;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QrCodeResponse {
        private UUID qrCodeId;
        private String qrType;
        private String qrDataString; // The actual QR code data
        private byte[] qrImageBytes; // Base64 encoded PNG image
        private BigDecimal amount;
        private String currency;
        private LocalDateTime expiresAt;
        private String status;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ValidateQrRequest {
        private String qrDataString;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QrValidationResponse {
        private boolean valid;
        private UUID qrCodeId;
        private UUID merchantId;
        private String merchantName;
        private String qrType;
        private BigDecimal amount;
        private String currency;
        private LocalDateTime expiresAt;
        private String errorMessage;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreatePaymentIntentRequest {
        private UUID qrCodeId;
        private UUID payerAccountId;
        private BigDecimal amount; // For static QR, user enters amount
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaymentIntentResponse {
        private UUID paymentIntentId;
        private UUID qrCodeId;
        private UUID merchantAccountId;
        private BigDecimal amount;
        private String currency;
        private String status;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaymentExecutionResponse {
        private UUID paymentIntentId;
        private String status;
        private String transactionId;
        private String message;
    }
}
