package fr.epita.payment.messaging;

import fr.epita.payment.config.RabbitConfig;
import fr.epita.payment.gateway.PaymentDeclinedException;
import fr.epita.payment.gateway.PaymentGateway;
import fr.epita.payment.model.Payment;
import fr.epita.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * The worker side of the RabbitMQ queue. Several instances (threads, or copies of this service)
 * compete for messages, and each payment is handled by exactly one of them at a time.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCommandListener {

    private final PaymentService paymentService;
    private final PaymentGateway paymentGateway;

    /**
     * Three outcomes:
     *  - success          → COMPLETED, message acked
     *  - card declined    → FAILED, message acked (retrying would not change the answer)
     *  - provider outage  → exception escapes, Spring retries with backoff, then dead-letters
     */
    @RabbitListener(queues = RabbitConfig.PROCESS_QUEUE)
    public void process(ProcessPaymentCommand command) {
        // Short DB transaction; never hold one open during the slow network call below.
        Optional<Payment> started = paymentService.startProcessing(command.paymentId());
        if (started.isEmpty()) {
            log.info("Payment {} is already final; ignoring duplicate command", command.paymentId());
            return;
        }
        Payment payment = started.get();
        log.info("Charging payment {} (attempt {})", payment.getId(), payment.getAttempts());

        try {
            // The payment id doubles as the provider's idempotency key: if a retry follows a
            // timeout where the charge actually succeeded, the provider won't charge twice.
            String providerReference = paymentGateway.charge(
                    payment.getId().toString(), payment.getAmount(), payment.getCurrency(), payment.getPaymentMethod());
            paymentService.markCompleted(payment.getId(), providerReference);
        } catch (PaymentDeclinedException e) {
            paymentService.markFailed(payment.getId(), e.getMessage());
        }
    }

    /** Commands land here once every retry has failed; the student gets a definitive answer. */
    @RabbitListener(queues = RabbitConfig.DEAD_LETTER_QUEUE)
    public void onDeadLetter(ProcessPaymentCommand command) {
        log.error("Payment {} dead-lettered after exhausting retries", command.paymentId());
        paymentService.markFailed(command.paymentId(), "Payment provider unavailable, please try again later");
    }
}
