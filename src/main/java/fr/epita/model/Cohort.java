package fr.epita.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.epita.enums.CohortSeason;
import fr.epita.enums.CohortStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "cohorts")
public class Cohort {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    private CohortSeason season;

    @Column(nullable = false)
    private int academicYear;

    @ManyToOne
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50)")
    private CohortStatus status = CohortStatus.NOT_STARTED;

    @ManyToMany(mappedBy = "cohorts")
    @JsonIgnore
    private List<Programme> programmes;

    @OneToMany(mappedBy = "cohort")
    @JsonIgnore
    private List<Student> students;

    @PrePersist
    void onCreate() {
        if (status == null) status = CohortStatus.NOT_STARTED;
    }
}
