package com.tunfin.wallet.repository;

import com.tunfin.wallet.model.SubsidyClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubsidyClaimRepository extends JpaRepository<SubsidyClaim, UUID> {
    List<SubsidyClaim> findByUserId(UUID userId);

    Optional<SubsidyClaim> findByUserIdAndProgramId(UUID userId, UUID programId);
}
