package com.tunfin.wallet.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String referenceId; // External Idempotency Key

    @Column(nullable = false)
    private String type; // P2P, DEPOSIT, etc.

    @Column(nullable = false)
    private String status; // PENDING, COMPLETED, FAILED

    private String description;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Builder.Default
    private List<LedgerEntry> entries = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;
}
