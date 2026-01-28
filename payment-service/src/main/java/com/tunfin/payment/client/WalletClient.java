package com.tunfin.payment.client;

import com.tunfin.payment.dto.WalletDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.UUID;

@FeignClient(name = "wallet-service", url = "http://localhost:8082/api/wallet")
public interface WalletClient {

    @PostMapping("/ledger/transaction")
    Object recordTransaction(@RequestBody WalletDto.TransactionRequest request);

    @GetMapping("/accounts/{id}")
    com.tunfin.payment.dto.WalletDto.Account getAccount(@PathVariable UUID id);
}
