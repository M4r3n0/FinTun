package com.tunfin.wallet.service;

import com.tunfin.wallet.dto.WalletDto;
import com.tunfin.wallet.model.*;
import com.tunfin.wallet.repository.AccountRepository;
import com.tunfin.wallet.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public List<Account> getAccountsByUserId(String userId) {
        return accountRepository.findByUserId(userId);
    }

    public Account getAccount(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    @Transactional
    public Account createAccount(WalletDto.CreateAccountRequest request, BigDecimal initialBalance) {
        if (accountRepository.findByUserIdAndCurrency(request.getUserId(), request.getCurrency()).isPresent()) {
            throw new RuntimeException("Account already exists");
        }

        var account = Account.builder()
                .userId(request.getUserId())
                .currency(request.getCurrency())
                .type(AccountType.LIABILITY)
                .balance(initialBalance != null ? initialBalance : BigDecimal.ZERO)
                .build();

        return accountRepository.save(account);
    }

    @Transactional
    public WalletDto.TransactionResponse recordTransaction(WalletDto.TransactionRequest request) {
        // 1. Validate Idempotency
        if (transactionRepository.existsByReferenceId(request.getReferenceId())) {
            throw new RuntimeException("Transaction already exists");
        }

        // 2. Validate Sum == 0
        BigDecimal sum = request.getEntries().stream()
                .map(WalletDto.LedgerEntryRequest::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sum.compareTo(BigDecimal.ZERO) != 0) {
            throw new RuntimeException("Ledger imbalance: Sum must be 0");
        }

        // 3. Create Transaction
        Transaction tx = Transaction.builder()
                .referenceId(request.getReferenceId())
                .type(request.getType())
                .description(request.getDescription())
                .status("COMPLETED")
                .build();

        // 4. Process Entries & Update Balances
        for (WalletDto.LedgerEntryRequest entryReq : request.getEntries()) {
            Account account = accountRepository.findWithLockById(UUID.fromString(entryReq.getAccountId()))
                    .orElseThrow(() -> new RuntimeException("Account not found"));

            if (account.getType() == AccountType.LIABILITY) {
                account.setBalance(account.getBalance().subtract(entryReq.getAmount()));
            } else {
                account.setBalance(account.getBalance().add(entryReq.getAmount()));
            }

            if (account.getBalance().compareTo(BigDecimal.ZERO) < 0 && account.getType() == AccountType.LIABILITY) {
                throw new RuntimeException("Insufficient funds");
            }

            accountRepository.save(account);

            LedgerEntry entry = LedgerEntry.builder()
                    .transaction(tx)
                    .account(account)
                    .amount(entryReq.getAmount())
                    .build();

            tx.getEntries().add(entry);
        }

        Transaction savedTx = transactionRepository.save(tx);

        return WalletDto.TransactionResponse.builder()
                .id(savedTx.getId())
                .referenceId(savedTx.getReferenceId())
                .type(savedTx.getType())
                .status(savedTx.getStatus())
                .description(savedTx.getDescription())
                .entries(savedTx.getEntries().stream()
                        .map(e -> WalletDto.LedgerEntryResponse.builder()
                                .id(e.getId())
                                .accountId(e.getAccount().getId())
                                .amount(e.getAmount())
                                .build())
                        .collect(java.util.stream.Collectors.toList()))
                .build();
    }

    public List<WalletDto.TransactionHistoryResponse> getTransactionHistory(UUID accountId) {
        List<Transaction> transactions = transactionRepository.findByAccountId(accountId);

        return transactions.stream()
                .map(tx -> {
                    // Find the net amount for this specific account
                    java.math.BigDecimal netAmount = tx.getEntries().stream()
                            .filter(e -> e.getAccount().getId().equals(accountId))
                            .map(LedgerEntry::getAmount)
                            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

                    return WalletDto.TransactionHistoryResponse.builder()
                            .id(tx.getId())
                            .referenceId(tx.getReferenceId())
                            .type(tx.getType())
                            .status(tx.getStatus())
                            .description(tx.getDescription())
                            .amount(netAmount)
                            .createdAt(tx.getCreatedAt())
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());
    }
}
