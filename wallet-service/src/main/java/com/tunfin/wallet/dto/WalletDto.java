package com.tunfin.wallet.dto;

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
    public static class CreateAccountRequest {
        private UUID userId;
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
        private UUID accountId;
        private BigDecimal amount;
    }
}
