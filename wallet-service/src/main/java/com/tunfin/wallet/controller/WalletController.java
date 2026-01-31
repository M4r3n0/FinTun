package com.tunfin.wallet.controller;

import com.tunfin.wallet.dto.WalletDto;
import com.tunfin.wallet.model.Account;
import com.tunfin.wallet.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final LedgerService ledgerService;

    @PostMapping("/accounts")
    public ResponseEntity<Account> createAccount(@RequestBody WalletDto.CreateAccountRequest request) {
        boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        java.math.BigDecimal initialBalance = isAdmin ? java.math.BigDecimal.valueOf(100) : java.math.BigDecimal.ZERO;

        return ResponseEntity.ok(ledgerService.createAccount(request, initialBalance));
    }

    @PostMapping("/ledger/transaction")
    public ResponseEntity<WalletDto.TransactionResponse> recordTransaction(
            @RequestBody WalletDto.TransactionRequest request) {
        return ResponseEntity.ok(ledgerService.recordTransaction(request));
    }

    @GetMapping("/accounts/user/{userId}")
    public ResponseEntity<List<Account>> getAccountsByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(ledgerService.getAccountsByUserId(userId));
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable UUID id) {
        return ResponseEntity.ok(ledgerService.getAccount(id));
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<java.util.List<WalletDto.TransactionHistoryResponse>> getTransactionHistory(
            @PathVariable UUID accountId) {
        return ResponseEntity.ok(ledgerService.getTransactionHistory(accountId));
    }

    @GetMapping("/ledger/history/{userId}")
    public ResponseEntity<java.util.List<WalletDto.TransactionHistoryResponse>> getGlobalTransactionHistory(
            @PathVariable String userId) {
        return ResponseEntity.ok(ledgerService.getUserHistory(userId));
    }
}
