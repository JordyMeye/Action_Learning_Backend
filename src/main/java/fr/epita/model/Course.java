package fr.epita.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.epita.enums.CourseStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String code;

    @Column(length = 2000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @ManyToOne
    @JoinColumn(name = "programme_id", nullable = false)
    private Programme programme;

    @ManyToOne
    @JoinColumn(name = "lecturer_id")
    private Lecturer lecturer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private CourseStatus status = CourseStatus.ACTIVE;

    @OneToMany(mappedBy = "course")
    @JsonIgnore
    private List<Submission> submissions;

    @PrePersist
    void onCreate() {
        if (status == null) status = CourseStatus.ACTIVE;
    }
}
