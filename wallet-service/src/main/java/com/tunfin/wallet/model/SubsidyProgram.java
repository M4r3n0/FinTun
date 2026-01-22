package com.tunfin.wallet.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subsidy_programs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubsidyProgram {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private BigDecimal amountPerUser;

    private BigDecimal totalBudget;

    private BigDecimal remainingBudget;

    // JSON String for criteria e.g. {"minAge": 18, "maxAge": 25, "role": "STUDENT"}
    @Column(columnDefinition = "TEXT")
    private String criteriaJson;

    @Builder.Default
    private boolean isActive = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
