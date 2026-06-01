package fr.epita.mapper;

import fr.epita.model.Student;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {
    
    public Student copyStudent(Student student) {
        if (student == null) {
            return null;
        }
        Student newStudent = new Student.Builder()
                .setName(student.getName())
                .setSurname(student.getSurname())
                .setEmail(student.getEmail())
                .setAge(student.getAge())
                .setAddress(student.getAddress())
                .build();
        if (student.getId() != null) {
            newStudent.setId(student.getId());
        }
        return newStudent;
    }

    public void mapToExisting(Student source, Student target) {
        if (source == null || target == null) {
            return;
        }
        target.setName(source.getName());
        target.setSurname(source.getSurname());
        target.setEmail(source.getEmail());
        target.setAge(source.getAge());
        target.setAddress(source.getAddress());
    }
}
