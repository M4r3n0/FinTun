package com.tunfin.wallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.cloud.openfeign.EnableFeignClients
public class WalletServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(WalletServiceApplication.class, args);
    }

    @org.springframework.context.annotation.Bean
    public org.springframework.boot.CommandLineRunner seedWallet(
            com.tunfin.wallet.repository.AccountRepository accountRepository) {
        return args -> {
            var logger = org.slf4j.LoggerFactory.getLogger(WalletServiceApplication.class);
            String adminId = "00000000-0000-0000-0000-000000000001";
            String currency = "TND";

            var existingAccount = accountRepository.findByUserIdAndCurrency(adminId, currency);

            if (existingAccount.isEmpty()) {
                logger.info(">>> SEED: Creating Admin wallet for {}...", adminId);
                var account = com.tunfin.wallet.model.Account.builder()
                        .userId(adminId)
                        .currency(currency)
                        .balance(java.math.BigDecimal.valueOf(100.00))
                        .status(com.tunfin.wallet.model.AccountStatus.ACTIVE)
                        .type(com.tunfin.wallet.model.AccountType.LIABILITY)
                        .build();
                accountRepository.save(account);
                logger.info(">>> SEED: Admin wallet created with 100 TND");
            } else {
                var account = existingAccount.get();
                if (account.getBalance().compareTo(java.math.BigDecimal.ZERO) == 0) {
                    logger.info(">>> SEED: Funding empty Admin wallet...");
                    account.setBalance(java.math.BigDecimal.valueOf(100.00));
                    accountRepository.save(account);
                    logger.info(">>> SEED: Admin wallet funded with 100 TND");
                } else {
                    logger.info(">>> SEED: Admin wallet already has funds: {} TND", account.getBalance());
                }
            }
        };
    }
}
