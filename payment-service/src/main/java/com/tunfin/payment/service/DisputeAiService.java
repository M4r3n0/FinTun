package com.tunfin.payment.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DisputeAiService {

    @Data
    @AllArgsConstructor
    public static class AiAnalysis {
        private String category;
        private Double confidence;
        private String recommendation;
    }

    public AiAnalysis analyze(String reason) {
        String lowerReasonString = reason.toLowerCase();

        if (lowerReasonString.contains("fraud") || lowerReasonString.contains("scam")
                || lowerReasonString.contains("unauthorized")) {
            return new AiAnalysis("FRAUD", 0.92, "Immediate escalation to Fraud Dept. Lock transaction if possible.");
        } else if (lowerReasonString.contains("never received") || lowerReasonString.contains("not delivered")
                || lowerReasonString.contains("missing")) {
            return new AiAnalysis("SERVICE_NOT_RENDERED", 0.85,
                    "Verify delivery status with vendor API. Suggest refund if no proof of delivery.");
        } else if (lowerReasonString.contains("wrong") || lowerReasonString.contains("broken")
                || lowerReasonString.contains("damaged")) {
            return new AiAnalysis("PRODUCT_QUALITY", 0.78, "Request photos from user. Escalate to dispute agent.");
        } else {
            return new AiAnalysis("OTHER", 0.50, "Insufficient data. Assign to human agent for manual review.");
        }
    }
}
