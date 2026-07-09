package fr.epita.model;

import fr.epita.enums.SubmissionStatus;
import fr.epita.enums.SubmissionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 4000)
    private String description;

    @Column(length = 2000)
    private String additionalNotes;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20)")
    private SubmissionType submissionType = SubmissionType.BOTH;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20)")
    private SubmissionStatus status = SubmissionStatus.DRAFT;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "lecturer_id")
    private Lecturer lecturer;

    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalTime dueTime;

    @Column(nullable = false)
    private int maxPoints;

    @Embedded
    private SubmissionRules rules;

    private String templateFileName;

    private String templateStoredPath;

    @Column(length = 5000)
    private String instructions;

    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "submission_reopened_students",
            joinColumns = @JoinColumn(name = "submission_id"))
    @Column(name = "student_id")
    private Set<Long> reopenedStudentIds = new HashSet<>();

    private Instant lastNotifiedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = SubmissionStatus.DRAFT;
        if (submissionType == null) submissionType = SubmissionType.BOTH;
    }

    public java.time.LocalDateTime deadline() {
        return dueDate.atTime(dueTime != null ? dueTime : LocalTime.of(23, 59));
    }
}
