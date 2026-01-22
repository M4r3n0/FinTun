package com.tunfin.wallet.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tunfin.wallet.client.IdentityClient;
import com.tunfin.wallet.dto.WalletDto;
import com.tunfin.wallet.model.Account;
import com.tunfin.wallet.model.SubsidyClaim;
import com.tunfin.wallet.model.SubsidyProgram;
import com.tunfin.wallet.repository.AccountRepository;
import com.tunfin.wallet.repository.SubsidyClaimRepository;
import com.tunfin.wallet.repository.SubsidyProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubsidyService {

    private final SubsidyProgramRepository programRepository;
    private final SubsidyClaimRepository claimRepository;
    private final IdentityClient identityClient;
    private final LedgerService ledgerService;
    private final AccountRepository accountRepository;
    private final ObjectMapper objectMapper;

    // Fixed UUID for Government Funding Source (simulated)
    private static final UUID GOVT_TREASURY_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public List<SubsidyProgram> getActivePrograms() {
        return programRepository.findByIsActiveTrue();
    }

    public List<SubsidyClaim> getUserClaims(UUID userId) {
        return claimRepository.findByUserId(userId);
    }

    @Transactional
    public SubsidyProgram createProgram(SubsidyProgram program) {
        return programRepository.save(program);
    }

    public boolean checkEligibility(UUID userId, UUID programId) {
        SubsidyProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new RuntimeException("Program not found"));

        try {
            IdentityClient.UserDto user = identityClient.getUser(userId);
            JsonNode criteria = objectMapper.readTree(program.getCriteriaJson());

            // Check Age
            if (criteria.has("maxAge") && user.dateOfBirth() != null) {
                int age = Period.between(user.dateOfBirth(), LocalDate.now()).getYears();
                if (age > criteria.get("maxAge").asInt()) {
                    return false;
                }
            }

            // Check Role
            if (criteria.has("role")) {
                if (!criteria.get("role").asText().equals(user.role())) {
                    return false;
                }
            }

            // Check KYC (Must be VERIFIED for any subsidy)
            if (!"VERIFIED".equals(user.kycLevel())) {
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("Eligibility check failed", e);
            return false;
        }
    }

    @Transactional
    public SubsidyClaim claimSubsidy(UUID userId, UUID programId) {
        // Double check eligibility
        if (!checkEligibility(userId, programId)) {
            throw new RuntimeException("User not eligible for this subsidy");
        }

        // Check duplicate claim
        if (claimRepository.findByUserIdAndProgramId(userId, programId).isPresent()) {
            throw new RuntimeException("Subsidy already claimed");
        }

        SubsidyProgram program = programRepository.findById(programId).orElseThrow();

        // Check budget
        if (program.getRemainingBudget().compareTo(program.getAmountPerUser()) < 0) {
            throw new RuntimeException("Program budget exhausted");
        }

        // Create Claim
        SubsidyClaim claim = SubsidyClaim.builder()
                .programId(programId)
                .userId(userId)
                .amount(program.getAmountPerUser())
                .status(SubsidyClaim.ClaimStatus.APPROVED) // Auto-approve for demo
                .build();

        claim = claimRepository.save(claim);

        // Disburse Funds
        disburseFunds(userId, program);

        return claim;
    }

    private void disburseFunds(UUID userId, SubsidyProgram program) {
        // Find or create User TND Account
        Account userAccount = accountRepository.findByUserId(userId.toString()).stream()
                .filter(a -> "TND".equals(a.getCurrency()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User needs a TND account to receive subsidy"));

        Account govtAccount = ensureGovtAccount();

        // Build Ledger Request
        WalletDto.TransactionRequest request = WalletDto.TransactionRequest.builder()
                .referenceId(UUID.randomUUID().toString())
                .type("SUBSIDY")
                .description("Subsidy: " + program.getName())
                .entries(List.of(
                        WalletDto.LedgerEntryRequest.builder()
                                .accountId(govtAccount.getId().toString())
                                .amount(program.getAmountPerUser().negate())
                                .build(),
                        WalletDto.LedgerEntryRequest.builder()
                                .accountId(userAccount.getId().toString())
                                .amount(program.getAmountPerUser())
                                .build()))
                .build();

        ledgerService.recordTransaction(request);

        // Update Budget
        program.setRemainingBudget(program.getRemainingBudget().subtract(program.getAmountPerUser()));
        programRepository.save(program);
    }

    private Account ensureGovtAccount() {
        // Find existing or create
        return accountRepository.findByUserId(GOVT_TREASURY_USER_ID.toString()).stream()
                .findFirst()
                .orElseGet(() -> {
                    Account acc = Account.builder()
                            .userId(GOVT_TREASURY_USER_ID.toString())
                            .currency("TND")
                            .balance(BigDecimal.valueOf(1000000000)) // Infinite money
                            .status(com.tunfin.wallet.model.AccountStatus.ACTIVE)
                            .type(com.tunfin.wallet.model.AccountType.LIABILITY)
                            .build();
                    return accountRepository.save(acc);
                });
    }
}
