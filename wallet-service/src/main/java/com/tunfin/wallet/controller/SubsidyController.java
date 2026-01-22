package com.tunfin.wallet.controller;

import com.tunfin.wallet.model.SubsidyClaim;
import com.tunfin.wallet.model.SubsidyProgram;
import com.tunfin.wallet.service.SubsidyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subsidies")
@RequiredArgsConstructor
public class SubsidyController {

    private final SubsidyService subsidyService;

    @GetMapping
    public ResponseEntity<List<SubsidyProgram>> getPrograms() {
        return ResponseEntity.ok(subsidyService.getActivePrograms());
    }

    @PostMapping("/programs")
    public ResponseEntity<SubsidyProgram> createProgram(@RequestBody SubsidyProgram program) {
        return ResponseEntity.ok(subsidyService.createProgram(program));
    }

    @GetMapping("/eligibility")
    public ResponseEntity<Boolean> checkEligibility(@RequestParam UUID userId, @RequestParam UUID programId) {
        return ResponseEntity.ok(subsidyService.checkEligibility(userId, programId));
    }

    @PostMapping("/claim")
    public ResponseEntity<SubsidyClaim> claim(@RequestParam UUID userId, @RequestParam UUID programId) {
        return ResponseEntity.ok(subsidyService.claimSubsidy(userId, programId));
    }

    @GetMapping("/claims")
    public ResponseEntity<List<SubsidyClaim>> getClaims(@RequestParam UUID userId) {
        return ResponseEntity.ok(subsidyService.getUserClaims(userId));
    }
}
