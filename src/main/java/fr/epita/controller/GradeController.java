package fr.epita.controller;

import fr.epita.dto.Response.MyGradeResponse;
import fr.epita.model.AppUser;
import fr.epita.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @GetMapping("/me")
    public ResponseEntity<List<MyGradeResponse>> getMyGrades(
            @AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(gradeService.getMyGrades(currentUser.getEmail()));
    }
}
