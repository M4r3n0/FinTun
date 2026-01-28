package com.tunfin.payment.repository;

import com.tunfin.payment.model.Dispute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, UUID> {
    List<Dispute> findByUserId(UUID userId);

    List<Dispute> findByStatus(String status);
}
