package fr.epita.repository.Interface;

import fr.epita.model.Student;
import fr.epita.repository.IRepository;
import java.util.Optional;
import java.util.List;

public interface IStudentRepository extends IRepository<Student, Long> {
    Optional<Student> findByEmail(String email);
    List<Student> findByName(String name);
    List<Student> findByAge(int age);
}
