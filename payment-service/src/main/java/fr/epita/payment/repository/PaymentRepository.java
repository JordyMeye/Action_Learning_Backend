package fr.epita.payment.repository;

import fr.epita.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByStudentEmailAndIdempotencyKey(String studentEmail, String idempotencyKey);
    List<Payment> findByStudentEmailOrderByCreatedAtDesc(String studentEmail);
    List<Payment> findByUniversityIdOrderByCreatedAtDesc(Long universityId);
}
