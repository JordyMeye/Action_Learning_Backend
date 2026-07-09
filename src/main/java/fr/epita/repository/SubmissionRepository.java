package fr.epita.repository;

import fr.epita.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByCourseId(Long courseId);

    List<Submission> findByLecturerId(Long lecturerId);

    List<Submission> findByCourse_Programme_Id(Long programmeId);

    List<Submission> findByCourse_Programme_University_Id(Long universityId);
}
