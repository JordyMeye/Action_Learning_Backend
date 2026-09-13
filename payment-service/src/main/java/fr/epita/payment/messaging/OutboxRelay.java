package fr.epita.payment.messaging;

import fr.epita.payment.config.KafkaConfig;
import fr.epita.payment.config.RabbitConfig;
import fr.epita.payment.model.OutboxEvent;
import fr.epita.payment.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Ships outbox rows to their broker and marks them published — but only after the broker
 * confirms it has the message. If a broker is down, rows simply wait and are retried on the
 * next tick. Delivery is therefore at-least-once: a crash right after sending but before
 * marking the row re-sends it, which is why every consumer ignores duplicates.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelay {

    private final OutboxEventRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "${payment.outbox.poll-interval-ms:1000}")
    public void publishPending() {
        for (OutboxEvent event : outboxRepository.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc()) {
            try {
                switch (event.getDestination()) {
                    case RABBITMQ -> sendToRabbit(event);
                    case KAFKA -> sendToKafka(event);
                }
                event.setPublishedAt(Instant.now());
                outboxRepository.save(event);
                log.info("Outbox -> {}: {} for payment {}", event.getDestination(), event.getEventType(), event.getAggregateId());
            } catch (Exception e) {
                if (e instanceof InterruptedException) Thread.currentThread().interrupt();
                // Stop at the first failure so messages keep their order; the next tick retries.
                log.warn("Outbox relay paused: could not publish {} {} ({}). Will retry.",
                        event.getDestination(), event.getEventType(), e.getMessage());
                return;
            }
        }
    }

    private void sendToRabbit(OutboxEvent event) throws Exception {
        Message message = MessageBuilder.withBody(event.getPayload().getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setMessageId(event.getId().toString())
                .build();
        CorrelationData confirm = new CorrelationData(event.getId().toString());

        rabbitTemplate.send(RabbitConfig.EXCHANGE, RabbitConfig.PROCESS_ROUTING_KEY, message, confirm);

        // Publisher confirm: RabbitMQ acks once the message is safely stored.
        // A "return" means it reached the exchange but no queue was bound to receive it.
        CorrelationData.Confirm result = confirm.getFuture().get(5, TimeUnit.SECONDS);
        if (!result.isAck() || confirm.getReturned() != null) {
            throw new IllegalStateException("RabbitMQ did not accept the message: "
                    + (result.getReason() != null ? result.getReason() : "unroutable"));
        }
    }

    private void sendToKafka(OutboxEvent event) throws Exception {
        // Keyed by payment id: all events of one payment land on the same partition, in order.
        // .get() waits for the broker's ack (acks=all) before we mark the row as published.
        kafkaTemplate.send(KafkaConfig.PAYMENT_EVENTS_TOPIC, event.getAggregateId(), event.getPayload())
                .get(5, TimeUnit.SECONDS);
    }
}
