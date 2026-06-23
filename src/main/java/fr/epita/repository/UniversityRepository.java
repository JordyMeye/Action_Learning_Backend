package fr.epita.repository;

import fr.epita.model.University;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UniversityRepository extends JpaRepository<University, Long> {
    boolean existsByName(String name);
    boolean existsByCode(String code);
    Optional<University> findByName(String name);
}
