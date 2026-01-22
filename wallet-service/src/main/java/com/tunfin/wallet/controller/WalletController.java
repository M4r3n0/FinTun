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
        return ResponseEntity.ok(ledgerService.createAccount(request));
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
}
