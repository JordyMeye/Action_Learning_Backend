package fr.epita.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.epita.enums.NotificationType;
import fr.epita.model.Student;
import fr.epita.repository.ProcessedEventRepository;
import fr.epita.repository.StudentRepository;
import fr.epita.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEventListenerTest {

    @Mock ProcessedEventRepository processedEventRepository;
    @Mock StudentRepository studentRepository;
    @Mock NotificationService notificationService;

    PaymentEventListener listener;

    private final Student alice = Student.builder().id(7L).email("alice@epita.fr").firstName("Alice").build();

    @BeforeEach
    void setUp() {
        listener = new PaymentEventListener(new ObjectMapper(), processedEventRepository, studentRepository, notificationService);
    }

    /** Same JSON payment-service publishes, including fields this consumer does not model. */
    private static String event(String eventId, String type, String failureReason) {
        return """
                {"eventId":"%s","type":"%s","occurredAt":"2026-09-13T10:00:00Z",
                 "paymentId":"6f1c2a0e-8a1b-4c7e-9d2f-0b1e2c3d4e5f","studentEmail":"alice@epita.fr","universityId":1,
                 "amount":1500.00,"currency":"EUR","description":"Semester 1 tuition",
                 "providerReference":"ch_abc","failureReason":%s}
                """.formatted(eventId, type, failureReason == null ? "null" : "\"" + failureReason + "\"");
    }

    @Test
    void completedPaymentNotifiesTheStudentAndRecordsTheEvent() throws Exception {
        when(processedEventRepository.existsById("evt-1")).thenReturn(false);
        when(studentRepository.findByEmail("alice@epita.fr")).thenReturn(Optional.of(alice));

        listener.onPaymentEvent(event("evt-1", "PAYMENT_COMPLETED", null));

        verify(notificationService).notifyStudentOfPayment(eq(alice), eq(NotificationType.PAYMENT_COMPLETED), contains("ch_abc"));
        verify(processedEventRepository).save(argThat(e -> e.getEventId().equals("evt-1")));
    }

    @Test
    void failedPaymentTellsTheStudentWhy() throws Exception {
        when(processedEventRepository.existsById("evt-2")).thenReturn(false);
        when(studentRepository.findByEmail("alice@epita.fr")).thenReturn(Optional.of(alice));

        listener.onPaymentEvent(event("evt-2", "PAYMENT_FAILED", "Card declined: insufficient funds"));

        verify(notificationService).notifyStudentOfPayment(eq(alice), eq(NotificationType.PAYMENT_FAILED), contains("insufficient funds"));
    }

    @Test
    void redeliveredEventIsIgnored() throws Exception {
        when(processedEventRepository.existsById("evt-1")).thenReturn(true);

        listener.onPaymentEvent(event("evt-1", "PAYMENT_COMPLETED", null));

        verifyNoInteractions(notificationService, studentRepository);
        verify(processedEventRepository, never()).save(any());
    }

    @Test
    void unknownEventTypeIsRecordedWithoutNotifying() throws Exception {
        when(processedEventRepository.existsById("evt-3")).thenReturn(false);

        listener.onPaymentEvent(event("evt-3", "PAYMENT_REFUNDED", null));

        verifyNoInteractions(notificationService, studentRepository);
        verify(processedEventRepository).save(argThat(e -> e.getEventId().equals("evt-3")));
    }
}
