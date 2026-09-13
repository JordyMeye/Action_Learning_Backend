package fr.epita.payment.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * @param paymentMethod token from the provider's checkout widget, never a card number.
 *                      Test tokens: pm_card_visa, pm_card_declined, pm_card_flaky, pm_card_gateway_down
 */
public record CreatePaymentRequest(
        @NotNull @DecimalMin("0.50") @Digits(integer = 10, fraction = 2) BigDecimal amount,
        @NotBlank @Pattern(regexp = "[A-Za-z]{3}") String currency,
        @NotBlank @Size(max = 255) String description,
        @NotBlank @Size(max = 100) String paymentMethod) {
}
