package fr.epita.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.epita.enums.NotificationType;
import fr.epita.model.ProcessedEvent;
import fr.epita.repository.ProcessedEventRepository;
import fr.epita.repository.StudentRepository;
import fr.epita.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Consumes payment outcomes from Kafka and turns them into student notifications.
 * The offset is committed only after this method returns, so a crash mid-way means the
 * event is delivered again — the processed_events check makes that harmless.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "payments.kafka.enabled", havingValue = "true")
public class PaymentEventListener {

    public static final String TOPIC = "payment-events";
    private static final int MAX_MESSAGE_LENGTH = 500;

    private final ObjectMapper objectMapper;
    private final ProcessedEventRepository processedEventRepository;
    private final StudentRepository studentRepository;
    private final NotificationService notificationService;

    @KafkaListener(topics = TOPIC)
    @Transactional
    public void onPaymentEvent(String payload) throws JsonProcessingException {
        PaymentEvent event = objectMapper.readValue(payload, PaymentEvent.class);

        if (processedEventRepository.existsById(event.eventId())) {
            log.info("Skipping already-processed payment event {}", event.eventId());
            return;
        }

        NotificationType type = switch (event.type()) {
            case PaymentEvent.COMPLETED -> NotificationType.PAYMENT_COMPLETED;
            case PaymentEvent.FAILED -> NotificationType.PAYMENT_FAILED;
            default -> null; // a type added later by payment-service; nothing to do here yet
        };

        if (type != null) {
            studentRepository.findByEmail(event.studentEmail()).ifPresentOrElse(
                    student -> notificationService.notifyStudentOfPayment(student, type, messageFor(event)),
                    () -> log.warn("Payment event {} refers to unknown student {}", event.eventId(), event.studentEmail()));
        }

        processedEventRepository.save(new ProcessedEvent(event.eventId(), event.type(), Instant.now()));
        log.info("Handled {} for payment {}", event.type(), event.paymentId());
    }

    private String messageFor(PaymentEvent event) {
        String what = event.amount() + " " + event.currency() + " for \"" + event.description() + "\"";
        String message = PaymentEvent.COMPLETED.equals(event.type())
                ? "Your payment of " + what + " was successful. Reference: " + event.providerReference() + "."
                : "Your payment of " + what + " could not be completed: " + event.failureReason()
                        + ". You have not been charged.";
        return message.length() <= MAX_MESSAGE_LENGTH ? message : message.substring(0, MAX_MESSAGE_LENGTH - 3) + "...";
    }
}
