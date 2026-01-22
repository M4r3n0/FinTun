package com.tunfin.wallet.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subsidy_claims")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubsidyClaim {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID programId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private ClaimStatus status;

    private LocalDateTime disbursedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum ClaimStatus {
        PENDING,
        APPROVED,
        REJECTED,
        DISBURSED
    }
}
