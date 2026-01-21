package com.tunfin.payment.controller;

import com.tunfin.payment.model.Payment;
import com.tunfin.payment.service.PaymentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/p2p")
    public ResponseEntity<Payment> transfer(@RequestBody TransferRequest request) {
        return ResponseEntity.ok(
                paymentService.processP2PTransfer(
                        request.getSenderAccountId(),
                        request.getReceiverAccountId(),
                        request.getAmount()));
    }

    @Data
    public static class TransferRequest {
        private UUID senderAccountId;
        private UUID receiverAccountId;
        private BigDecimal amount;
    }
}
