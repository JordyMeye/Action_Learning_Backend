package fr.epita.controller;

import fr.epita.dto.Response.NotificationResponse;
import fr.epita.model.AppUser;
import fr.epita.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // ── Student /me endpoints (auth-based, no studentId in URL) ──

    @GetMapping("/notifications/me")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(notificationService.getForStudentByEmail(currentUser.getEmail()));
    }

    @GetMapping("/notifications/me/unread-count")
    public ResponseEntity<Long> getMyUnreadCount(
            @AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(notificationService.unreadCountByEmail(currentUser.getEmail()));
    }

    @PatchMapping("/notifications/me/read-all")
    public ResponseEntity<Void> markMyAllRead(
            @AuthenticationPrincipal AppUser currentUser) {
        notificationService.markAllReadByEmail(currentUser.getEmail());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/notifications/{id}/read")
    public ResponseEntity<NotificationResponse> markRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markRead(id));
    }

    // ── Admin / lecturer endpoints (studentId in URL) ──

    @GetMapping("/students/{studentId}/notifications")
    public ResponseEntity<List<NotificationResponse>> getForStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(notificationService.getForStudent(studentId));
    }

    @GetMapping("/students/{studentId}/notifications/unread-count")
    public ResponseEntity<Long> unreadCount(@PathVariable Long studentId) {
        return ResponseEntity.ok(notificationService.unreadCount(studentId));
    }

    @PatchMapping("/students/{studentId}/notifications/read-all")
    public ResponseEntity<Void> markAllRead(@PathVariable Long studentId) {
        notificationService.markAllRead(studentId);
        return ResponseEntity.ok().build();
    }
}
