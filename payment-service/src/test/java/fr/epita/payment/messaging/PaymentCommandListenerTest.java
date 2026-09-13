package fr.epita.payment.messaging;

import fr.epita.payment.gateway.GatewayUnavailableException;
import fr.epita.payment.gateway.PaymentDeclinedException;
import fr.epita.payment.gateway.PaymentGateway;
import fr.epita.payment.model.Payment;
import fr.epita.payment.model.PaymentStatus;
import fr.epita.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCommandListenerTest {

    @Mock PaymentService paymentService;
    @Mock PaymentGateway paymentGateway;
    @InjectMocks PaymentCommandListener listener;

    private final UUID id = UUID.randomUUID();
    private final ProcessPaymentCommand command = new ProcessPaymentCommand(id);
    private final Payment payment = Payment.builder()
            .id(id).amount(new BigDecimal("1500.00")).currency("EUR")
            .paymentMethod("pm_card_visa").status(PaymentStatus.PROCESSING).attempts(1)
            .build();

    @Test
    void successfulChargeCompletesThePayment() {
        when(paymentService.startProcessing(id)).thenReturn(Optional.of(payment));
        when(paymentGateway.charge(id.toString(), payment.getAmount(), "EUR", "pm_card_visa")).thenReturn("ch_123");

        listener.process(command);

        verify(paymentService).markCompleted(id, "ch_123");
    }

    @Test
    void declinedCardFailsThePaymentAndAcksTheMessage() {
        when(paymentService.startProcessing(id)).thenReturn(Optional.of(payment));
        when(paymentGateway.charge(any(), any(), any(), any()))
                .thenThrow(new PaymentDeclinedException("Card declined: insufficient funds"));

        assertThatCode(() -> listener.process(command)).doesNotThrowAnyException();

        verify(paymentService).markFailed(id, "Card declined: insufficient funds");
    }

    @Test
    void providerOutageIsRethrownSoRabbitMqRetries() {
        when(paymentService.startProcessing(id)).thenReturn(Optional.of(payment));
        when(paymentGateway.charge(any(), any(), any(), any()))
                .thenThrow(new GatewayUnavailableException("Provider timeout"));

        assertThatThrownBy(() -> listener.process(command)).isInstanceOf(GatewayUnavailableException.class);

        verify(paymentService, never()).markFailed(any(), any());
        verify(paymentService, never()).markCompleted(any(), any());
    }

    @Test
    void duplicateDeliveryOfAFinishedPaymentNeverChargesAgain() {
        when(paymentService.startProcessing(id)).thenReturn(Optional.empty());

        listener.process(command);

        verifyNoInteractions(paymentGateway);
    }

    @Test
    void deadLetteredCommandFailsThePayment() {
        listener.onDeadLetter(command);

        verify(paymentService).markFailed(eq(id), contains("unavailable"));
    }
}
