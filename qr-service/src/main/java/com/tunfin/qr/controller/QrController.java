package com.tunfin.qr.controller;

import com.tunfin.qr.dto.QrDto;
import com.tunfin.qr.model.PaymentIntent;
import com.tunfin.qr.service.QrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
public class QrController {

    private final QrService qrService;

    /**
     * Generate a static QR code for a merchant
     */
    @PostMapping("/merchant/static")
    public ResponseEntity<QrDto.QrCodeResponse> generateStaticQr(
            @RequestBody QrDto.GenerateStaticQrRequest request) {
        return ResponseEntity.ok(qrService.generateStaticQr(request));
    }

    /**
     * Generate a dynamic QR code with amount
     */
    @PostMapping("/merchant/dynamic")
    public ResponseEntity<QrDto.QrCodeResponse> generateDynamicQr(
            @RequestBody QrDto.GenerateDynamicQrRequest request) {
        return ResponseEntity.ok(qrService.generateDynamicQr(request));
    }

    /**
     * Validate a scanned QR code
     */
    @PostMapping("/validate")
    public ResponseEntity<QrDto.QrValidationResponse> validateQr(
            @RequestBody QrDto.ValidateQrRequest request) {
        return ResponseEntity.ok(qrService.validateQr(request));
    }

    /**
     * Create a payment intent from a scanned QR
     */
    @PostMapping("/payment-intent")
    public ResponseEntity<QrDto.PaymentIntentResponse> createPaymentIntent(
            @RequestBody QrDto.CreatePaymentIntentRequest request) {
        return ResponseEntity.ok(qrService.createPaymentIntent(request));
    }

    /**
     * Get payment intent details
     */
    @GetMapping("/payment-intent/{id}")
    public ResponseEntity<PaymentIntent> getPaymentIntent(@PathVariable UUID id) {
        return ResponseEntity.ok(qrService.getPaymentIntent(id));
    }

    /**
     * Mark payment intent as completed (called by payment service)
     */
    @PostMapping("/payment-intent/{id}/complete")
    public ResponseEntity<Void> completePaymentIntent(
            @PathVariable UUID id,
            @RequestParam String transactionId) {
        qrService.completePaymentIntent(id, transactionId);
        return ResponseEntity.ok().build();
    }

    /**
     * Execute a payment intent (user-initiated after scanning QR)
     */
    @PostMapping("/payment-intent/{id}/execute")
    public ResponseEntity<QrDto.PaymentExecutionResponse> executePaymentIntent(@PathVariable UUID id) {
        return ResponseEntity.ok(qrService.executePaymentIntent(id));
    }
}
