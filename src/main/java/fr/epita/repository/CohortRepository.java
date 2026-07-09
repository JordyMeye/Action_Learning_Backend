package fr.epita.repository;

import fr.epita.model.Cohort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CohortRepository extends JpaRepository<Cohort, Long> {

    List<Cohort> findByUniversityId(Long universityId);

    List<Cohort> findByProgrammes_Id(Long programmeId);

    boolean existsByProgrammes_Id(Long programmeId);
}
