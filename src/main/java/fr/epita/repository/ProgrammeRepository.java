package fr.epita.repository;

import fr.epita.model.Programme;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgrammeRepository extends JpaRepository<Programme, Long> {

    boolean existsByName(String name);
}
