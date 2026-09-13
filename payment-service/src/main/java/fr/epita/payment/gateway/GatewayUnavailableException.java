package fr.epita.payment.gateway;

/** The provider could not be reached or timed out. The charge may be retried. */
public class GatewayUnavailableException extends RuntimeException {
    public GatewayUnavailableException(String message) {
        super(message);
    }
}
