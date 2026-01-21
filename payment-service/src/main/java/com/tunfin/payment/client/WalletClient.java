package com.tunfin.payment.client;

import com.tunfin.payment.dto.WalletDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "wallet-service", url = "http://localhost:8082/api/wallet")
public interface WalletClient {

    @PostMapping("/ledger/transaction")
    Object recordTransaction(@RequestBody WalletDto.TransactionRequest request);
}
