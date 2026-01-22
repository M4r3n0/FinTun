package com.tunfin.qr.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.tunfin.qr.dto.QrDto;
import com.tunfin.qr.model.PaymentIntent;
import com.tunfin.qr.model.QrCode;
import com.tunfin.qr.repository.PaymentIntentRepository;
import com.tunfin.qr.repository.QrCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QrService {

    private final QrCodeRepository qrCodeRepository;
    private final PaymentIntentRepository paymentIntentRepository;
    private final com.tunfin.qr.client.PaymentClient paymentClient;

    @Value("${qr.default.size:300}")
    private int defaultQrSize;

    @Value("${qr.dynamic.expiry.minutes:15}")
    private int defaultExpiryMinutes;

    /**
     * Generate a static QR code for a merchant
     * Static QR contains only merchant identification
     */
    @Transactional
    public QrDto.QrCodeResponse generateStaticQr(QrDto.GenerateStaticQrRequest request) {
        // Create QR data in format: TUNFIN:MERCHANT:{merchantId}:{merchantName}
        String qrData = String.format("TUNFIN:MERCHANT:%s:%s",
                request.getMerchantId(),
                request.getMerchantName());

        QrCode qrCode = QrCode.builder()
                .merchantId(request.getMerchantId())
                .qrType(QrCode.QrType.STATIC)
                .qrData(qrData)
                .currency(request.getCurrency())
                .status(QrCode.QrStatus.ACTIVE)
                .build();

        qrCode = qrCodeRepository.save(qrCode);

        byte[] qrImageBytes = generateQrImage(qrData);

        return QrDto.QrCodeResponse.builder()
                .qrCodeId(qrCode.getId())
                .qrType(qrCode.getQrType().name())
                .qrDataString(qrData)
                .qrImageBytes(qrImageBytes)
                .currency(qrCode.getCurrency())
                .status(qrCode.getStatus().name())
                .build();
    }

    /**
     * Generate a dynamic QR code with amount and expiry
     * Dynamic QR contains merchant ID, amount, and expiration time
     */
    @Transactional
    public QrDto.QrCodeResponse generateDynamicQr(QrDto.GenerateDynamicQrRequest request) {
        int expiryMinutes = request.getExpiryMinutes() != null ? request.getExpiryMinutes() : defaultExpiryMinutes;

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expiryMinutes);

        // Create QR data in format:
        // TUNFIN:PAYMENT:{merchantId}:{amount}:{currency}:{expiryTimestamp}
        String qrData = String.format("TUNFIN:PAYMENT:%s:%s:%s:%s:%s",
                request.getMerchantId(),
                request.getMerchantName(),
                request.getAmount(),
                request.getCurrency(),
                expiresAt.toString());

        QrCode qrCode = QrCode.builder()
                .merchantId(request.getMerchantId())
                .qrType(QrCode.QrType.DYNAMIC)
                .qrData(qrData)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .expiresAt(expiresAt)
                .status(QrCode.QrStatus.ACTIVE)
                .build();

        qrCode = qrCodeRepository.save(qrCode);

        byte[] qrImageBytes = generateQrImage(qrData);

        return QrDto.QrCodeResponse.builder()
                .qrCodeId(qrCode.getId())
                .qrType(qrCode.getQrType().name())
                .qrDataString(qrData)
                .qrImageBytes(qrImageBytes)
                .amount(qrCode.getAmount())
                .currency(qrCode.getCurrency())
                .expiresAt(qrCode.getExpiresAt())
                .status(qrCode.getStatus().name())
                .build();
    }

    /**
     * Validate a scanned QR code
     */
    public QrDto.QrValidationResponse validateQr(QrDto.ValidateQrRequest request) {
        String qrData = request.getQrDataString();

        // Parse QR data
        if (!qrData.startsWith("TUNFIN:")) {
            return QrDto.QrValidationResponse.builder()
                    .valid(false)
                    .errorMessage("Invalid QR code format")
                    .build();
        }

        String[] parts = qrData.split(":");
        if (parts.length < 3) {
            return QrDto.QrValidationResponse.builder()
                    .valid(false)
                    .errorMessage("Invalid QR code structure")
                    .build();
        }

        String qrType = parts[1]; // MERCHANT or PAYMENT
        UUID merchantId = UUID.fromString(parts[2]);
        String merchantName = parts[3];

        // Find QR code in database
        Optional<QrCode> qrCodeOpt = qrCodeRepository.findByQrData(qrData);
        if (qrCodeOpt.isEmpty()) {
            return QrDto.QrValidationResponse.builder()
                    .valid(false)
                    .errorMessage("QR code not found")
                    .build();
        }

        QrCode qrCode = qrCodeOpt.get();

        // Check if expired (for dynamic QR)
        if (qrCode.getQrType() == QrCode.QrType.DYNAMIC) {
            if (qrCode.getExpiresAt() != null && LocalDateTime.now().isAfter(qrCode.getExpiresAt())) {
                qrCode.setStatus(QrCode.QrStatus.EXPIRED);
                qrCodeRepository.save(qrCode);
                return QrDto.QrValidationResponse.builder()
                        .valid(false)
                        .errorMessage("QR code has expired")
                        .build();
            }
        }

        // Check if already used (for dynamic QR)
        if (qrCode.getStatus() == QrCode.QrStatus.USED) {
            return QrDto.QrValidationResponse.builder()
                    .valid(false)
                    .errorMessage("QR code has already been used")
                    .build();
        }

        // Build response
        QrDto.QrValidationResponse.QrValidationResponseBuilder responseBuilder = QrDto.QrValidationResponse.builder()
                .valid(true)
                .qrCodeId(qrCode.getId())
                .merchantId(qrCode.getMerchantId())
                .merchantName(merchantName)
                .qrType(qrCode.getQrType().name())
                .currency(qrCode.getCurrency());

        if (qrCode.getQrType() == QrCode.QrType.DYNAMIC) {
            responseBuilder
                    .amount(qrCode.getAmount())
                    .expiresAt(qrCode.getExpiresAt());
        }

        return responseBuilder.build();
    }

    /**
     * Create a payment intent from a scanned QR code
     */
    @Transactional
    public QrDto.PaymentIntentResponse createPaymentIntent(QrDto.CreatePaymentIntentRequest request) {
        QrCode qrCode = qrCodeRepository.findById(request.getQrCodeId())
                .orElseThrow(() -> new RuntimeException("QR code not found"));

        // Determine amount
        BigDecimal amount;
        if (qrCode.getQrType() == QrCode.QrType.DYNAMIC) {
            amount = qrCode.getAmount();
        } else {
            // For static QR, user must provide amount
            if (request.getAmount() == null) {
                throw new RuntimeException("Amount is required for static QR payments");
            }
            amount = request.getAmount();
        }

        // Create payment intent
        PaymentIntent paymentIntent = PaymentIntent.builder()
                .qrCodeId(qrCode.getId())
                .payerAccountId(request.getPayerAccountId())
                .merchantAccountId(qrCode.getMerchantId()) // Assuming merchantId is the account ID
                .amount(amount)
                .currency(qrCode.getCurrency())
                .status(PaymentIntent.PaymentStatus.PENDING)
                .build();

        paymentIntent = paymentIntentRepository.save(paymentIntent);

        return QrDto.PaymentIntentResponse.builder()
                .paymentIntentId(paymentIntent.getId())
                .qrCodeId(paymentIntent.getQrCodeId())
                .merchantAccountId(paymentIntent.getMerchantAccountId())
                .amount(paymentIntent.getAmount())
                .currency(paymentIntent.getCurrency())
                .status(paymentIntent.getStatus().name())
                .createdAt(paymentIntent.getCreatedAt())
                .build();
    }

    /**
     * Mark payment intent as completed
     */
    @Transactional
    public void completePaymentIntent(UUID paymentIntentId, String transactionId) {
        PaymentIntent paymentIntent = paymentIntentRepository.findById(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Payment intent not found"));

        paymentIntent.setStatus(PaymentIntent.PaymentStatus.COMPLETED);
        paymentIntent.setTransactionId(transactionId);
        paymentIntent.setCompletedAt(LocalDateTime.now());
        paymentIntentRepository.save(paymentIntent);

        // Mark dynamic QR as used
        QrCode qrCode = qrCodeRepository.findById(paymentIntent.getQrCodeId())
                .orElseThrow(() -> new RuntimeException("QR code not found"));

        if (qrCode.getQrType() == QrCode.QrType.DYNAMIC) {
            qrCode.setStatus(QrCode.QrStatus.USED);
            qrCodeRepository.save(qrCode);
        }
    }

    /**
     * Generate QR code image using ZXing
     */
    private byte[] generateQrImage(String qrData) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE,
                    defaultQrSize, defaultQrSize, hints);

            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", baos);
            return baos.toByteArray();

        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code image", e);
        }
    }

    /**
     * Get payment intent by ID
     */
    public PaymentIntent getPaymentIntent(UUID paymentIntentId) {
        return paymentIntentRepository.findById(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Payment intent not found"));
    }

    /**
     * Execute a payment intent by calling the payment service
     */
    @Transactional
    public QrDto.PaymentExecutionResponse executePaymentIntent(UUID paymentIntentId) {
        PaymentIntent intent = paymentIntentRepository.findById(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Payment intent not found"));

        if (intent.getStatus() == PaymentIntent.PaymentStatus.COMPLETED) {
            throw new RuntimeException("Payment intent already completed");
        }

        if (intent.getStatus() == PaymentIntent.PaymentStatus.FAILED) {
            throw new RuntimeException("Payment intent has failed");
        }

        try {
            // Call payment service to execute the transfer
            var response = paymentClient.transfer(
                    new com.tunfin.qr.client.PaymentClient.TransferRequest(
                            intent.getPayerAccountId(),
                            intent.getMerchantAccountId(),
                            intent.getAmount()));

            // Mark intent as completed
            intent.setStatus(PaymentIntent.PaymentStatus.COMPLETED);
            intent.setTransactionId(response.id().toString());
            intent.setCompletedAt(LocalDateTime.now());
            paymentIntentRepository.save(intent);

            // Mark dynamic QR as used
            QrCode qrCode = qrCodeRepository.findById(intent.getQrCodeId())
                    .orElseThrow(() -> new RuntimeException("QR code not found"));

            if (qrCode.getQrType() == QrCode.QrType.DYNAMIC) {
                qrCode.setStatus(QrCode.QrStatus.USED);
                qrCodeRepository.save(qrCode);
            }

            return QrDto.PaymentExecutionResponse.builder()
                    .paymentIntentId(paymentIntentId)
                    .status("SUCCESS")
                    .transactionId(response.id().toString())
                    .message("Payment executed successfully")
                    .build();

        } catch (Exception e) {
            intent.setStatus(PaymentIntent.PaymentStatus.FAILED);
            paymentIntentRepository.save(intent);

            return QrDto.PaymentExecutionResponse.builder()
                    .paymentIntentId(paymentIntentId)
                    .status("FAILED")
                    .message("Payment failed: " + e.getMessage())
                    .build();
        }
    }
}
