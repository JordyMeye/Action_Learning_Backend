package fr.epita.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * This app's view of the event payment-service publishes on "payment-events".
 * It is a local copy on purpose: services share a JSON contract, not a jar. Unknown
 * fields are ignored, so payment-service can add fields without breaking this consumer.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentEvent(
        String eventId,
        String type,
        String paymentId,
        String studentEmail,
        BigDecimal amount,
        String currency,
        String description,
        String providerReference,
        String failureReason) {

    public static final String COMPLETED = "PAYMENT_COMPLETED";
    public static final String FAILED = "PAYMENT_FAILED";
}
