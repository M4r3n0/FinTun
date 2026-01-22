package com.tunfin.qr.model;

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
@Table(name = "qr_codes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QrCode {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID merchantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QrType qrType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String qrData;

    private BigDecimal amount;

    @Column(length = 3)
    private String currency;

    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private QrStatus status = QrStatus.ACTIVE;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum QrType {
        STATIC,   // Merchant identification only
        DYNAMIC   // Includes amount and expiry
    }

    public enum QrStatus {
        ACTIVE,
        EXPIRED,
        USED,
        CANCELLED
    }
}
