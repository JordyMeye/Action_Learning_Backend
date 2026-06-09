package fr.epita.controller;

import fr.epita.dto.Request.CreateProgrammeRequest;
import fr.epita.dto.Response.ProgrammeResponse;
import fr.epita.service.ProgrammeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<ProgrammeResponse>> getAll() {
        return ResponseEntity.ok(programmeService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgrammeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(programmeService.getById(id));
    }
}
