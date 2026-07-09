package fr.epita.controller;

import fr.epita.dto.Request.CreateTimetableRequest;
import fr.epita.dto.Response.TimetableResponse;
import fr.epita.model.AppUser;
import fr.epita.service.TimetableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/timetable")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;

    @GetMapping
    public ResponseEntity<List<TimetableResponse>> getAll(
            @AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(timetableService.getAll(currentUser));
    }

    @GetMapping("/weekly")
    public ResponseEntity<Map<String, List<TimetableResponse>>> getWeekly(
            @AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(timetableService.getWeekly(currentUser));
    }

    @PostMapping
    public ResponseEntity<TimetableResponse> create(
            @Valid @RequestBody CreateTimetableRequest request,
            @AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(timetableService.create(request, currentUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TimetableResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateTimetableRequest request,
            @AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(timetableService.update(id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUser currentUser) {
        timetableService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
