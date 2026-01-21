package com.tunfin.identity.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "kyc_documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String documentType; // ID_CARD, PASSPORT

    @Column(nullable = false)
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private KycStatus status = KycStatus.PENDING;

    @CreationTimestamp
    private LocalDateTime uploadedAt;
}

enum KycStatus {
    PENDING, APPROVED, REJECTED
}
