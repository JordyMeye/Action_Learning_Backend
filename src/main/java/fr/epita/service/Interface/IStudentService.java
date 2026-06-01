package fr.epita.service.Interface;

import fr.epita.model.Student;
import fr.epita.service.IService;
import java.util.Optional;
import java.util.List;

public interface IStudentService extends IService<Student, Long> {
    Optional<Student> findByEmail(String email);
    List<Student> findByName(String name);
    List<Student> findByAge(int age);
}
