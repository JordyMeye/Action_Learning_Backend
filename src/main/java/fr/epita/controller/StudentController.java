package fr.epita.controller;

import fr.epita.factory.StudentFactory;
import fr.epita.model.Student;
import fr.epita.service.impl.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {

    @Autowired
    private StudentService studentService;

    /**
     * Get all students
     * @return List of all students
     */
    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {
        List<Student> students = studentService.getAll();
        return ResponseEntity.ok(students);
    }

    /**
     * Get student by ID
     * @param id Student ID
     * @return Student if found, 404 if not
     */
    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        Optional<Student> student = studentService.getById(id);
        return student.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create a new student
     * @param student Student object to create
     * @return Created student with 201 status
     */
    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        try {
            Student createdStudent = studentService.create(student);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Create student using factory pattern with validation
     * @param name Student name
     * @param surname Student surname
     * @param email Student email
     * @param age Student age
     * @param address Student address
     * @return Created student or 400 if validation fails
     */
    @PostMapping("/factory")
    public ResponseEntity<Student> createStudentByFactory(
            @RequestParam String name,
            @RequestParam String surname,
            @RequestParam String email,
            @RequestParam int age,
            @RequestParam String address) {
        Student student = StudentFactory.createStudent(name, surname, email, age, address);
        if (student == null) {
            return ResponseEntity.badRequest().build();
        }
        Student createdStudent = studentService.create(student);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
    }

    /**
     * Update an existing student
     * @param id Student ID
     * @param student Updated student object
     * @return Updated student or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @RequestBody Student student) {
        Optional<Student> existingStudent = studentService.getById(id);
        if (existingStudent.isPresent()) {
            student.setId(id);
            Student updatedStudent = studentService.update(student);
            return ResponseEntity.ok(updatedStudent);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Delete a student by ID
     * @param id Student ID
     * @return 204 No Content if deleted, 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        if (studentService.getById(id).isPresent()) {
            studentService.delete(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Find student by email
     * @param email Student email
     * @return Student if found, 404 if not
     */
    @GetMapping("/search/email")
    public ResponseEntity<Student> findByEmail(@RequestParam String email) {
        Optional<Student> student = studentService.findByEmail(email);
        return student.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Find students by name
     * @param name Student name
     * @return List of students with matching name
     */
    @GetMapping("/search/name")
    public ResponseEntity<List<Student>> findByName(@RequestParam String name) {
        List<Student> students = studentService.findByName(name);
        return ResponseEntity.ok(students);
    }

    /**
     * Find students by age
     * @param age Student age
     * @return List of students with matching age
     */
    @GetMapping("/search/age")
    public ResponseEntity<List<Student>> findByAge(@RequestParam int age) {
        List<Student> students = studentService.findByAge(age);
        return ResponseEntity.ok(students);
    }
}
