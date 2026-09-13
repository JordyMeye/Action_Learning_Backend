package fr.epita.payment.model;

public enum PaymentStatus {
    /** Accepted by the API; the ProcessPayment command is on its way to / waiting in RabbitMQ. */
    PENDING,
    /** A worker picked the command up and is talking to the payment provider. */
    PROCESSING,
    COMPLETED,
    FAILED;

    public boolean isFinal() {
        return this == COMPLETED || this == FAILED;
    }
}
