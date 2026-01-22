package com.tunfin.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public class WalletDto {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateAccountRequest {
        private String userId;
        private String currency;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TransactionRequest {
        private String referenceId;
        private String type;
        private String description;
        private List<LedgerEntryRequest> entries;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LedgerEntryRequest {
        private String accountId;
        private BigDecimal amount;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TransactionResponse {
        private java.util.UUID id;
        private String referenceId;
        private String type;
        private String status;
        private String description;
        private List<LedgerEntryResponse> entries;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LedgerEntryResponse {
        private java.util.UUID id;
        private java.util.UUID accountId;
        private BigDecimal amount;
    }
}
