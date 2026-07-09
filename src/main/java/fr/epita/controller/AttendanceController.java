package fr.epita.controller;

import fr.epita.dto.Request.CreateAttendanceSessionRequest;
import fr.epita.dto.Request.MarkAttendanceRequest;
import fr.epita.dto.Response.AttendanceRecordResponse;
import fr.epita.dto.Response.AttendanceSessionResponse;
import fr.epita.dto.Response.SessionStudentResponse;
import fr.epita.dto.Response.StudentAttendanceStatsResponse;
import fr.epita.model.AppUser;
import fr.epita.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/sessions")
    public ResponseEntity<AttendanceSessionResponse> createSession(
            @RequestBody CreateAttendanceSessionRequest request,
            @AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(attendanceService.createSession(request, currentUser));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<AttendanceSessionResponse>> getSessions(
            @RequestParam(required = false) Long cohortId,
            @AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(attendanceService.getSessions(cohortId, currentUser));
    }

    @GetMapping("/sessions/{sessionId}/students")
    public ResponseEntity<List<SessionStudentResponse>> getSessionStudents(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(attendanceService.getSessionStudents(sessionId, currentUser));
    }

    @PostMapping("/sessions/{sessionId}/mark")
    public ResponseEntity<List<AttendanceRecordResponse>> markAttendance(
            @PathVariable Long sessionId,
            @RequestBody List<MarkAttendanceRequest> requests,
            @AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(attendanceService.markAttendance(sessionId, requests, currentUser));
    }

    @GetMapping("/me")
    public ResponseEntity<List<AttendanceRecordResponse>> getMyAttendance(
            @AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(attendanceService.getMyAttendance(currentUser));
    }

    @GetMapping("/me/stats")
    public ResponseEntity<StudentAttendanceStatsResponse> getMyStats(
            @AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(attendanceService.getMyStats(currentUser));
    }

    @GetMapping("/students/{studentId}/stats")
    public ResponseEntity<StudentAttendanceStatsResponse> getStudentStats(
            @PathVariable Long studentId,
            @AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(attendanceService.getStudentStats(studentId, currentUser));
    }

    @GetMapping("/cohorts/{cohortId}/stats")
    public ResponseEntity<List<StudentAttendanceStatsResponse>> getCohortStats(
            @PathVariable Long cohortId,
            @AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(attendanceService.getCohortStats(cohortId, currentUser));
    }
}
