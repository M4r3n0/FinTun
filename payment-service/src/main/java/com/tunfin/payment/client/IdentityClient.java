package com.tunfin.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "identity-service", url = "http://localhost:8081/api/users")
public interface IdentityClient {

    @GetMapping("/{id}")
    Map<String, String> getUserById(@PathVariable UUID id);
}
