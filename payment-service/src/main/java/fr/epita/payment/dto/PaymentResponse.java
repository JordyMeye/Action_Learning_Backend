package fr.epita.payment.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        String studentEmail,
        BigDecimal amount,
        String currency,
        String description,
        String status,
        String providerReference,
        String failureReason,
        int attempts,
        Instant createdAt,
        Instant updatedAt) {
}
