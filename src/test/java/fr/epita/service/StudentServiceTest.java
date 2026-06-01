package fr.epita.service;

import fr.epita.model.Student;
import fr.epita.repository.impl.StudentRepository;
import fr.epita.service.impl.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    private Student student;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        student = new Student.Builder()
                .setName("John")
                .setSurname("Doe")
                .setEmail("john@example.com")
                .setAge(25)
                .setAddress("123 Main St")
                .build();
    }

    @Test
    public void testCreateStudent() {
        when(studentRepository.save(student)).thenReturn(student);
        
        Student createdStudent = studentService.create(student);
        
        assertNotNull(createdStudent);
        assertEquals("John", createdStudent.getName());
        verify(studentRepository, times(1)).save(student);
    }

    @Test
    public void testGetStudentById() {
        Long id = 1L;
        student.setId(id);
        when(studentRepository.findById(id)).thenReturn(Optional.of(student));
        
        Optional<Student> foundStudent = studentService.getById(id);
        
        assertTrue(foundStudent.isPresent());
        assertEquals("John", foundStudent.get().getName());
        verify(studentRepository, times(1)).findById(id);
    }

    @Test
    public void testGetAllStudents() {
        List<Student> students = Arrays.asList(student);
        when(studentRepository.findAll()).thenReturn(students);
        
        List<Student> result = studentService.getAll();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(studentRepository, times(1)).findAll();
    }

    @Test
    public void testDeleteStudent() {
        Long id = 1L;
        studentService.delete(id);
        
        verify(studentRepository, times(1)).deleteById(id);
    }

    @Test
    public void testFindByEmail() {
        when(studentRepository.findByEmail("john@example.com")).thenReturn(Optional.of(student));
        
        Optional<Student> foundStudent = studentService.findByEmail("john@example.com");
        
        assertTrue(foundStudent.isPresent());
        assertEquals("john@example.com", foundStudent.get().getEmail());
    }
}
