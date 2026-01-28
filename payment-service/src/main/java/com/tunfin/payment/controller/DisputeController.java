package com.tunfin.payment.controller;

import com.tunfin.payment.dto.DisputeDto;
import com.tunfin.payment.model.Dispute;
import com.tunfin.payment.service.DisputeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/disputes")
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;

    @PostMapping
    public ResponseEntity<Dispute> fileDispute(@RequestHeader("X-User-Id") String userId,
            @RequestBody DisputeDto request) {
        return ResponseEntity
                .ok(disputeService.fileDispute(UUID.fromString(userId), request.getPaymentId(), request.getReason()));
    }

    @GetMapping("/my-disputes")
    public ResponseEntity<List<Dispute>> getMyDisputes(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(disputeService.getUserDisputes(UUID.fromString(userId)));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<Dispute>> getAllDisputes() {
        return ResponseEntity.ok(disputeService.getAllDisputes());
    }

    @PostMapping("/admin/resolve/{id}")
    public ResponseEntity<Dispute> resolveDispute(
            @PathVariable UUID id,
            @RequestParam String status,
            @RequestHeader("X-User-Id") String agentId) {
        return ResponseEntity.ok(disputeService.resolveDispute(id, status, UUID.fromString(agentId)));
    }
}
