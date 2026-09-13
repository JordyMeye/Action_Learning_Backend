package fr.epita.payment.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stand-in for a real provider. Like Stripe's test mode, the payment-method token decides
 * the outcome, so every path can be demonstrated on demand:
 *
 *   pm_card_visa          → succeeds
 *   pm_card_declined      → declined (permanent failure, no retry)
 *   pm_card_flaky         → times out twice, then succeeds (shows retries)
 *   pm_card_gateway_down  → always times out (shows retries exhausted → dead-letter queue)
 */
@Slf4j
@Component
public class FakePaymentGateway implements PaymentGateway {

    private final long latencyMs;

    /** Real providers de-duplicate on the idempotency key; we mimic that so a retry never charges twice. */
    private final Map<String, String> chargesByKey = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> attemptsByKey = new ConcurrentHashMap<>();

    public FakePaymentGateway(@Value("${payment.gateway.latency-ms:1500}") long latencyMs) {
        this.latencyMs = latencyMs;
    }

    @Override
    public String charge(String idempotencyKey, BigDecimal amount, String currency, String paymentMethod) {
        String existing = chargesByKey.get(idempotencyKey);
        if (existing != null) {
            log.info("[gateway] Idempotent replay for {} -> {}", idempotencyKey, existing);
            return existing;
        }

        simulateNetworkLatency();
        int attempt = attemptsByKey.computeIfAbsent(idempotencyKey, k -> new AtomicInteger()).incrementAndGet();

        switch (paymentMethod) {
            case "pm_card_visa" -> { }
            case "pm_card_flaky" -> {
                if (attempt <= 2) throw new GatewayUnavailableException("Provider timeout (attempt " + attempt + ")");
            }
            case "pm_card_gateway_down" ->
                    throw new GatewayUnavailableException("Provider unavailable (attempt " + attempt + ")");
            case "pm_card_declined" -> throw new PaymentDeclinedException("Card declined: insufficient funds");
            default -> throw new PaymentDeclinedException("Unsupported payment method: " + paymentMethod);
        }

        String reference = "ch_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        chargesByKey.put(idempotencyKey, reference);
        log.info("[gateway] Charged {} {} -> {}", amount, currency, reference);
        return reference;
    }

    private void simulateNetworkLatency() {
        if (latencyMs <= 0) return;
        try {
            Thread.sleep(latencyMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GatewayUnavailableException("Interrupted while calling provider");
        }
    }
}
