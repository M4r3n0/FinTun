package com.tunfin.identity.repository;

import com.tunfin.identity.model.KycDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface KycDocumentRepository extends JpaRepository<KycDocument, UUID> {
    List<KycDocument> findByUserId(UUID userId);
}
