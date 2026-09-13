package fr.epita.payment.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Transactional outbox row: a message that must reach a broker, saved in the SAME database
 * transaction as the business change it describes. OutboxRelay ships it afterwards.
 * If the service crashes in between, the row is still there and gets sent on restart.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "outbox_events",
        indexes = @Index(name = "idx_outbox_events_unpublished", columnList = "published_at, created_at"))
public class OutboxEvent {

    public enum Destination { RABBITMQ, KAFKA }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Destination destination;

    @Column(nullable = false)
    private String eventType;

    /** The payment this message is about; used as the Kafka key so one payment's events stay in order. */
    @Column(nullable = false)
    private String aggregateId;

    /** JSON body, sent to the broker as-is. */
    @Column(nullable = false, length = 4000)
    private String payload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Null until the relay has handed the message to the broker. */
    @Column(name = "published_at")
    private Instant publishedAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
