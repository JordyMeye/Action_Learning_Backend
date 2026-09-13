package fr.epita.payment.gateway;

import java.math.BigDecimal;

/** The external payment provider (Stripe, Adyen, PayPal...). */
public interface PaymentGateway {

    /**
     * @return the provider's charge reference
     * @throws PaymentDeclinedException    permanent refusal — do not retry
     * @throws GatewayUnavailableException temporary problem — safe to retry with the same idempotency key
     */
    String charge(String idempotencyKey, BigDecimal amount, String currency, String paymentMethod);
}
