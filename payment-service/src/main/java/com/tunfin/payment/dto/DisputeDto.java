package com.tunfin.payment.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class DisputeDto {
    private UUID paymentId;
    private String reason;
}
