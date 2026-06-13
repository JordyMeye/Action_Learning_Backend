package fr.epita.repository;

import fr.epita.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByCohortId(Long cohortId);
    List<Submission> findByLecturerId(Long lecturerId);
}
