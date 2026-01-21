package com.tunfin.wallet.controller;

import com.tunfin.wallet.dto.WalletDto;
import com.tunfin.wallet.model.Account;
import com.tunfin.wallet.model.Transaction;
import com.tunfin.wallet.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Transaction> recordTransaction(@RequestBody WalletDto.TransactionRequest request) {
        return ResponseEntity.ok(ledgerService.recordTransaction(request));
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<Account> getAccount(@PathVariable UUID id) {
        // Simple get for now, in prod use Repository directly or Service
        return ResponseEntity.ok(null); // TODO: Implement get
    }
}
