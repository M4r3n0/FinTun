package com.tunfin.wallet.repository;

import com.tunfin.wallet.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    boolean existsByReferenceId(String referenceId);
}
