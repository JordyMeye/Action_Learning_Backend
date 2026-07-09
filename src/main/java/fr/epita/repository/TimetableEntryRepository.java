package fr.epita.repository;

import fr.epita.model.TimetableEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimetableEntryRepository extends JpaRepository<TimetableEntry, Long> {

    List<TimetableEntry> findByCohort_UniversityId(Long universityId);

    List<TimetableEntry> findByCohortId(Long cohortId);

    List<TimetableEntry> findByLecturerId(Long lecturerId);
}
