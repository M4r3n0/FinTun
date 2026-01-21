package com.tunfin.wallet.service;

import com.tunfin.wallet.dto.WalletDto;
import com.tunfin.wallet.model.*;
import com.tunfin.wallet.repository.AccountRepository;
import com.tunfin.wallet.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public Account createAccount(WalletDto.CreateAccountRequest request) {
        if (accountRepository.findByUserIdAndCurrency(request.getUserId(), request.getCurrency()).isPresent()) {
            throw new RuntimeException("Account already exists");
        }

        var account = Account.builder()
                .userId(request.getUserId())
                .currency(request.getCurrency())
                .type(AccountType.LIABILITY)
                .build();

        return accountRepository.save(account);
    }

    @Transactional
    public Transaction recordTransaction(WalletDto.TransactionRequest request) {
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
            Account account = accountRepository.findById(entryReq.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Account not found"));

            // Logic:
            // LIABILITY Account (User Wallet):
            // - Debit (+) means we OWE LESS -> Balance DECREASES
            // - Credit (-) means we OWE MORE -> Balance INCREASES
            // ASSET Account (System):
            // - Debit (+) means we HAVE MORE -> Balance INCREASES
            // - Credit (-) means we HAVE LESS -> Balance DECREASES

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

        return transactionRepository.save(tx);
    }
}
