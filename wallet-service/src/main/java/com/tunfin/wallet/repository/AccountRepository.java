package com.tunfin.wallet.repository;

import com.tunfin.wallet.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByUserIdAndCurrency(UUID userId, String currency);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findById(UUID id);
}
