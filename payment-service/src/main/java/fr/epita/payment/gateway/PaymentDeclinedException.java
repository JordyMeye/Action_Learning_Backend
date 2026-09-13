package fr.epita.payment.gateway;

/** The provider refused the charge (insufficient funds, stolen card...). Retrying will not help. */
public class PaymentDeclinedException extends RuntimeException {
    public PaymentDeclinedException(String message) {
        super(message);
    }
}
