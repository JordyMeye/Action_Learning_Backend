package fr.epita.repository.impl;

import fr.epita.model.Student;
import fr.epita.repository.Interface.IStudentRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long>, IStudentRepository {
    @Override
    Optional<Student> findByEmail(String email);
    
    @Override
    List<Student> findByName(String name);
    
    @Override
    List<Student> findByAge(int age);
}
