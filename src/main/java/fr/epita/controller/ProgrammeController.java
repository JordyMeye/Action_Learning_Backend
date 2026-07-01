package fr.epita.controller;

import fr.epita.dto.Request.CreateProgrammeRequest;
import fr.epita.dto.Response.ProgrammeResponse;
import fr.epita.model.AppUser;
import fr.epita.service.ProgrammeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/programmes")
@RequiredArgsConstructor
public class ProgrammeController {

    private final ProgrammeService programmeService;

    @PostMapping
    public ResponseEntity<ProgrammeResponse> create(@RequestBody CreateProgrammeRequest request) {
        return ResponseEntity.ok(programmeService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<ProgrammeResponse>> getAll(
            @RequestParam(required = false) Long universityId,
            @AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(programmeService.getAll(resolve(universityId, currentUser)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgrammeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(programmeService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProgrammeResponse> update(
            @PathVariable Long id,
            @RequestBody CreateProgrammeRequest request) {
        return ResponseEntity.ok(programmeService.update(id, request));
    }

    private Long resolve(Long universityId, AppUser currentUser) {
        if (universityId != null) return universityId;
        return currentUser != null ? currentUser.getUniversityId() : null;
    }
}
