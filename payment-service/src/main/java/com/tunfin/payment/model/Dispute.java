package com.tunfin.payment.model;

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
@Table(name = "disputes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dispute {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID paymentId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    private String category; // e.g., UNAUTHORIZED, SERVICE_NOT_RENDERED, FRAUD, OTHER

    private String status; // PENDING, AUTO_RESOLVED, ESCALATED, REJECTED, RESOLVED

    private Double aiConfidence;

    @Column(columnDefinition = "TEXT")
    private String aiRecommendation;

    private UUID assignedAgentId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;
}
