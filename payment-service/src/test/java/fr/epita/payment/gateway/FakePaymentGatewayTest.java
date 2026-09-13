package fr.epita.payment.gateway;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FakePaymentGatewayTest {

    private final FakePaymentGateway gateway = new FakePaymentGateway(0);
    private final BigDecimal amount = new BigDecimal("100.00");

    @Test
    void visaCardSucceeds() {
        assertThat(gateway.charge("key-1", amount, "EUR", "pm_card_visa")).startsWith("ch_");
    }

    @Test
    void declinedCardIsAPermanentFailure() {
        assertThatThrownBy(() -> gateway.charge("key-2", amount, "EUR", "pm_card_declined"))
                .isInstanceOf(PaymentDeclinedException.class);
    }

    @Test
    void flakyCardTimesOutTwiceThenSucceeds() {
        assertThatThrownBy(() -> gateway.charge("key-3", amount, "EUR", "pm_card_flaky"))
                .isInstanceOf(GatewayUnavailableException.class);
        assertThatThrownBy(() -> gateway.charge("key-3", amount, "EUR", "pm_card_flaky"))
                .isInstanceOf(GatewayUnavailableException.class);
        assertThat(gateway.charge("key-3", amount, "EUR", "pm_card_flaky")).startsWith("ch_");
    }

    @Test
    void retryingAChargeThatAlreadySucceededDoesNotChargeTwice() {
        String first = gateway.charge("key-4", amount, "EUR", "pm_card_visa");
        String retry = gateway.charge("key-4", amount, "EUR", "pm_card_visa");

        assertThat(retry).isEqualTo(first);
    }
}
