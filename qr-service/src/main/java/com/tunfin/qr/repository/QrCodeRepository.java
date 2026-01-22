package com.tunfin.qr.repository;

import com.tunfin.qr.model.QrCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QrCodeRepository extends JpaRepository<QrCode, UUID> {
    List<QrCode> findByMerchantId(UUID merchantId);
    Optional<QrCode> findByQrData(String qrData);
    List<QrCode> findByMerchantIdAndQrType(UUID merchantId, QrCode.QrType qrType);
}
