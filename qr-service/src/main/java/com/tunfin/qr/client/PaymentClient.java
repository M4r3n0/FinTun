package com.tunfin.qr.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "payment-service", url = "${payment.service.url:http://localhost:8083/api/payment}")
public interface PaymentClient {

    @PostMapping("/p2p")
    PaymentResponse transfer(@RequestBody TransferRequest request);

    record TransferRequest(UUID senderAccountId, UUID receiverAccountId, BigDecimal amount) {
    }

    record PaymentResponse(UUID id, String status) {
    }
}
