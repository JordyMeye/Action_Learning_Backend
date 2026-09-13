package fr.epita.payment.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "payments",
        uniqueConstraints = @UniqueConstraint(name = "uk_payments_student_idempotency_key",
                columnNames = {"student_email", "idempotency_key"}))
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** JWT subject of the student who paid. */
    @Column(name = "student_email", nullable = false)
    private String studentEmail;

    private Long universityId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private String description;

    /** Tokenised payment method (e.g. "pm_card_visa"). Raw card numbers never reach this service. */
    @Column(nullable = false)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    /** Client-supplied Idempotency-Key header: retrying the same request never creates a second payment. */
    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    /** Charge id returned by the payment provider once the money has moved. */
    private String providerReference;

    @Column(length = 500)
    private String failureReason;

    /** Number of times a worker has called the provider for this payment. */
    private int attempts;

    /** Optimistic lock: two workers can never both move the same payment to a final state. */
    @Version
    private Long version;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
        if (status == null) status = PaymentStatus.PENDING;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
