package com.tunfin.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class WalletDto {
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
        private UUID accountId;
        private BigDecimal amount;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Account {
        private UUID id;
        private String userId;
        private String currency;
        private BigDecimal balance;
    }
}
