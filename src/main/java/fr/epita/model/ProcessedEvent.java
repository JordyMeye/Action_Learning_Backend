package fr.epita.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * "Inbox" of messages already handled. Kafka delivers at-least-once, so the same event can
 * arrive twice; the producer's event id as primary key makes re-processing impossible.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "processed_events")
public class ProcessedEvent {

    @Id
    private String eventId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false, updatable = false)
    private Instant processedAt;
}
