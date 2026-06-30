package fr.epita.controller;

import fr.epita.dto.Request.CreateStudentRequest;
import fr.epita.dto.Response.StudentResponse;
import fr.epita.model.AppUser;
import fr.epita.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {

    @Autowired
    private StudentService studentService;

    // CREATE
    @PostMapping
    public ResponseEntity<StudentResponse> create(
            @RequestBody CreateStudentRequest req,
            @AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studentService.create(req, currentUser.getUniversityId()));
    }

    // GET OWN PROFILE
    @GetMapping("/me")
    public ResponseEntity<StudentResponse> getMyProfile(@AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(studentService.getMyProfile(currentUser.getEmail()));
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAll(
            @RequestParam(required = false) Long universityId,
            @AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(studentService.getAll(resolve(universityId, currentUser)));
    }

    // GET BY COHORT
    @GetMapping("/cohort/{cohortId}")
    public ResponseEntity<List<StudentResponse>> getByCohort(@PathVariable Long cohortId) {
        return ResponseEntity.ok(studentService.getByCohort(cohortId));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> update(
            @PathVariable Long id,
            @RequestBody CreateStudentRequest req,
            @AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(studentService.update(id, req, currentUser));
    }

    // DEACTIVATE
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser currentUser) {
        studentService.deactivate(id, currentUser);
        return ResponseEntity.ok().build();
    }

    private Long resolve(Long universityId, AppUser currentUser) {
        if (universityId != null) return universityId;
        return currentUser != null ? currentUser.getUniversityId() : null;
    }

}
