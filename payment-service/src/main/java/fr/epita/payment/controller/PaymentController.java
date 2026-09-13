package fr.epita.payment.controller;

import fr.epita.payment.dto.CreatePaymentRequest;
import fr.epita.payment.dto.PaymentResponse;
import fr.epita.payment.security.AuthenticatedUser;
import fr.epita.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Returns 202 Accepted, not 201: the charge happens asynchronously in a worker.
     * The client polls the Location URL (or waits for the in-app notification) for the outcome.
     * The Idempotency-Key (any unique string, e.g. a UUID generated when the checkout page loads)
     * makes double-clicks and network retries safe.
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> create(
            @Valid @RequestBody CreatePaymentRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        PaymentResponse payment = paymentService.create(request, idempotencyKey, currentUser);
        return ResponseEntity.accepted()
                .location(URI.create("/api/payments/" + payment.id()))
                .body(payment);
    }

    @GetMapping("/me")
    public ResponseEntity<List<PaymentResponse>> getMine(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(paymentService.getMine(currentUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> get(@PathVariable UUID id,
                                               @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(paymentService.get(id, currentUser));
    }

    /** University admin: every payment made by their institution's students. */
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getForUniversity(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(paymentService.getForUniversity(currentUser));
    }
}
