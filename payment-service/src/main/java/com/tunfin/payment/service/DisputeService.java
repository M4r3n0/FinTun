package com.tunfin.payment.service;

import com.tunfin.payment.model.Dispute;
import com.tunfin.payment.model.Payment;
import com.tunfin.payment.repository.DisputeRepository;
import com.tunfin.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DisputeService {

    private final DisputeRepository disputeRepository;
    private final PaymentRepository paymentRepository;
    private final DisputeAiService aiService;

    public Dispute fileDispute(UUID userId, UUID paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        // AI Analysis
        DisputeAiService.AiAnalysis analysis = aiService.analyze(reason);

        Dispute dispute = Dispute.builder()
                .paymentId(paymentId)
                .userId(userId)
                .reason(reason)
                .category(analysis.getCategory())
                .aiConfidence(analysis.getConfidence())
                .aiRecommendation(analysis.getRecommendation())
                .status("PENDING")
                .build();

        // Auto-Resolution Logic
        // Rule: If amount < 50 TND and high confidence 'FRAUD' or
        // 'SERVICE_NOT_RENDERED', auto-approve
        if (payment.getAmount().compareTo(new BigDecimal("50.00")) < 0 && analysis.getConfidence() > 0.80) {
            dispute.setStatus("AUTO_RESOLVED");
            dispute.setResolvedAt(LocalDateTime.now());
            log.info("Dispute {} auto-resolved based on AI confidence and low amount.", dispute.getId());
        } else if (analysis.getConfidence() < 0.60 || payment.getAmount().compareTo(new BigDecimal("200.00")) > 0) {
            dispute.setStatus("ESCALATED");
            log.info("Dispute {} escalated to agent for review.", dispute.getId());
        }

        return disputeRepository.save(dispute);
    }

    public List<Dispute> getUserDisputes(UUID userId) {
        return disputeRepository.findByUserId(userId);
    }

    public List<Dispute> getAllDisputes() {
        return disputeRepository.findAll();
    }

    public Dispute resolveDispute(UUID disputeId, String status, UUID agentId) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new RuntimeException("Dispute not found"));

        dispute.setStatus(status);
        dispute.setAssignedAgentId(agentId);
        dispute.setResolvedAt(LocalDateTime.now());

        return disputeRepository.save(dispute);
    }
}
