package fr.epita.payment.messaging;

import java.util.UUID;

/**
 * RabbitMQ command: "charge this payment". It carries only the id — the worker reloads the
 * current state from the database, so a stale or duplicated message can't act on old data.
 */
public record ProcessPaymentCommand(UUID paymentId) {
    public static final String TYPE = "ProcessPayment";
}
