package fr.epita.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.epita.payment.dto.CreatePaymentRequest;
import fr.epita.payment.dto.PaymentResponse;
import fr.epita.payment.messaging.PaymentEvent;
import fr.epita.payment.messaging.ProcessPaymentCommand;
import fr.epita.payment.model.OutboxEvent;
import fr.epita.payment.model.Payment;
import fr.epita.payment.model.PaymentStatus;
import fr.epita.payment.repository.OutboxEventRepository;
import fr.epita.payment.repository.PaymentRepository;
import fr.epita.payment.security.AuthenticatedUser;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    /**
     * HTTP side: record the payment as PENDING and queue a ProcessPayment command.
     * Both rows are written in ONE transaction, so there is never a payment without its
     * command (or a command without its payment), whatever crashes afterwards.
     */
    @Transactional
    public PaymentResponse create(CreatePaymentRequest request, String idempotencyKey, AuthenticatedUser student) {
        Optional<Payment> existing = paymentRepository.findByStudentEmailAndIdempotencyKey(student.email(), idempotencyKey);
        if (existing.isPresent()) {
            if (!isSameRequest(existing.get(), request))
                throw new IllegalStateException("Idempotency-Key was already used for a different payment request");
            log.info("Idempotent replay of payment {} (key {})", existing.get().getId(), idempotencyKey);
            return toResponse(existing.get());
        }

        Payment payment = paymentRepository.save(Payment.builder()
                .studentEmail(student.email())
                .universityId(student.universityId())
                .amount(request.amount())
                .currency(request.currency().toUpperCase())
                .description(request.description().trim())
                .paymentMethod(request.paymentMethod())
                .idempotencyKey(idempotencyKey)
                .status(PaymentStatus.PENDING)
                .build());

        enqueue(OutboxEvent.Destination.RABBITMQ, ProcessPaymentCommand.TYPE, payment,
                new ProcessPaymentCommand(payment.getId()));

        log.info("Payment {} accepted: {} {} from {}", payment.getId(), payment.getAmount(), payment.getCurrency(), student.email());
        return toResponse(payment);
    }

    /** Worker step 1: claim the payment. Empty if it is already final (duplicate delivery). */
    @Transactional
    public Optional<Payment> startProcessing(UUID paymentId) {
        Payment payment = find(paymentId);
        if (payment.getStatus().isFinal()) return Optional.empty();
        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setAttempts(payment.getAttempts() + 1);
        return Optional.of(payment);
    }

    /** Worker step 2a: money moved. Status change + Kafka event committed together. */
    @Transactional
    public void markCompleted(UUID paymentId, String providerReference) {
        Payment payment = find(paymentId);
        if (payment.getStatus().isFinal()) return;
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setProviderReference(providerReference);
        enqueue(OutboxEvent.Destination.KAFKA, PaymentEvent.COMPLETED, payment,
                PaymentEvent.of(PaymentEvent.COMPLETED, payment));
        log.info("Payment {} COMPLETED ({})", paymentId, providerReference);
    }

    /** Worker step 2b: declined, or provider unreachable after every retry. */
    @Transactional
    public void markFailed(UUID paymentId, String reason) {
        Payment payment = find(paymentId);
        if (payment.getStatus().isFinal()) return;
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(reason);
        enqueue(OutboxEvent.Destination.KAFKA, PaymentEvent.FAILED, payment,
                PaymentEvent.of(PaymentEvent.FAILED, payment));
        log.info("Payment {} FAILED: {}", paymentId, reason);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getMine(AuthenticatedUser user) {
        return paymentRepository.findByStudentEmailOrderByCreatedAtDesc(user.email())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getForUniversity(AuthenticatedUser admin) {
        if (admin.universityId() == null) return List.of();
        return paymentRepository.findByUniversityIdOrderByCreatedAtDesc(admin.universityId())
                .stream().map(this::toResponse).toList();
    }

    /** The paying student, or an admin of the same university. */
    @Transactional(readOnly = true)
    public PaymentResponse get(UUID id, AuthenticatedUser user) {
        Payment payment = find(id);
        boolean owner = payment.getStudentEmail().equalsIgnoreCase(user.email());
        boolean universityAdmin = user.isUniAdmin() && Objects.equals(payment.getUniversityId(), user.universityId());
        if (!owner && !universityAdmin) throw new AccessDeniedException("You cannot view this payment");
        return toResponse(payment);
    }

    private void enqueue(OutboxEvent.Destination destination, String eventType, Payment payment, Object message) {
        try {
            outboxRepository.save(OutboxEvent.builder()
                    .destination(destination)
                    .eventType(eventType)
                    .aggregateId(payment.getId().toString())
                    .payload(objectMapper.writeValueAsString(message))
                    .build());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialise " + eventType, e);
        }
    }

    private boolean isSameRequest(Payment payment, CreatePaymentRequest request) {
        return payment.getAmount().compareTo(request.amount()) == 0
                && payment.getCurrency().equalsIgnoreCase(request.currency())
                && payment.getDescription().equals(request.description().trim())
                && payment.getPaymentMethod().equals(request.paymentMethod());
    }

    private Payment find(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));
    }

    private PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(
                p.getId(), p.getStudentEmail(), p.getAmount(), p.getCurrency(), p.getDescription(),
                p.getStatus().name(), p.getProviderReference(), p.getFailureReason(), p.getAttempts(),
                p.getCreatedAt(), p.getUpdatedAt());
    }
}
