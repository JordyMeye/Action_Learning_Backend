package fr.epita.payment.messaging;

import fr.epita.payment.model.Payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Fact published on the Kafka "payment-events" topic. This JSON shape is the contract with
 * every consumer (the core backend today; accounting, analytics... tomorrow). Evolve it by
 * adding fields, never by renaming or removing them.
 *
 * @param eventId unique per event; consumers use it to ignore duplicates
 */
public record PaymentEvent(
        String eventId,
        String type,
        Instant occurredAt,
        UUID paymentId,
        String studentEmail,
        Long universityId,
        BigDecimal amount,
        String currency,
        String description,
        String providerReference,
        String failureReason) {

    public static final String COMPLETED = "PAYMENT_COMPLETED";
    public static final String FAILED = "PAYMENT_FAILED";

    public static PaymentEvent of(String type, Payment payment) {
        return new PaymentEvent(
                UUID.randomUUID().toString(),
                type,
                Instant.now(),
                payment.getId(),
                payment.getStudentEmail(),
                payment.getUniversityId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getDescription(),
                payment.getProviderReference(),
                payment.getFailureReason());
    }
}
