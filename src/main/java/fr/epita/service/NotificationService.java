package fr.epita.service;

import fr.epita.dto.Response.NotificationResponse;
import fr.epita.enums.NotificationType;
import fr.epita.model.Notification;
import fr.epita.model.Student;
import fr.epita.model.Submission;
import fr.epita.repository.NotificationRepository;
import fr.epita.repository.StudentRepository;
import fr.epita.repository.SubmissionUploadRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final StudentRepository studentRepository;
    private final SubmissionUploadRepository uploadRepository;
    private final EmailService emailService;

    @Transactional
    public void notifyCourseStudents(Submission submission, NotificationType type, String message) {
        for (Student student : audienceFor(submission)) {
            notifyStudent(student, submission, type, message);
        }
    }

    @Transactional
    public void notifyStudent(Student student, Submission submission, NotificationType type, String message) {
        notificationRepository.save(Notification.builder()
                .student(student)
                .submission(submission)
                .type(type)
                .message(message)
                .readFlag(false)
                .build());
        if (student.getEmail() != null && !student.getEmail().isBlank()) {
            String subject = "Action Learning Platform  " + submission.getTitle();
            emailService.sendNotificationEmail(student.getEmail(), student.getFirstName(), subject, message);
        }
    }

    @Transactional
    public int notifyNonSubmitters(Submission submission, NotificationType type, String message) {
        int count = 0;
        for (Student student : audienceFor(submission)) {
            boolean submitted = uploadRepository
                    .findTopBySubmissionIdAndStudentIdOrderByUploadedAtDesc(submission.getId(), student.getId())
                    .map(u -> Boolean.TRUE.equals(u.getTurnedIn()))
                    .orElse(false);
            if (!submitted) {
                notifyStudent(student, submission, type, message);
                count++;
            }
        }
        return count;
    }

    private List<Student> audienceFor(Submission submission) {
        if (submission.getCourse() == null || submission.getCourse().getProgramme() == null) {
            return java.util.List.of();
        }
        return studentRepository.findByProgrammeId(submission.getCourse().getProgramme().getId());
    }

    public List<NotificationResponse> getForStudentByEmail(String email) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));
        return getForStudent(student.getId());
    }

    public long unreadCountByEmail(String email) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));
        return unreadCount(student.getId());
    }

    @Transactional
    public void markAllReadByEmail(String email) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));
        markAllRead(student.getId());
    }

    public List<NotificationResponse> getForStudent(Long studentId) {
        return notificationRepository.findByStudentIdOrderByCreatedAtDesc(studentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public long unreadCount(Long studentId) {
        return notificationRepository.findByStudentIdAndReadFlagFalse(studentId).size();
    }

    @Transactional
    public NotificationResponse markRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
        notification.setReadFlag(true);
        return toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllRead(Long studentId) {
        List<Notification> unread = notificationRepository.findByStudentIdAndReadFlagFalse(studentId);
        unread.forEach(n -> n.setReadFlag(true));
        notificationRepository.saveAll(unread);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .submissionId(n.getSubmission() != null ? n.getSubmission().getId() : null)
                .submissionTitle(n.getSubmission() != null ? n.getSubmission().getTitle() : null)
                .type(n.getType().name())
                .message(n.getMessage())
                .read(n.isReadFlag())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
