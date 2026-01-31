package com.tunfin.wallet.repository;

import com.tunfin.wallet.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    boolean existsByReferenceId(String referenceId);

    @Query("SELECT DISTINCT t FROM Transaction t JOIN t.entries e WHERE e.account.id = :accountId ORDER BY t.createdAt DESC")
    List<Transaction> findByAccountId(@Param("accountId") UUID accountId);

    @Query("SELECT DISTINCT t FROM Transaction t JOIN t.entries e WHERE e.account.userId = :userId ORDER BY t.createdAt DESC")
    List<Transaction> findByUserId(@Param("userId") String userId);
}
