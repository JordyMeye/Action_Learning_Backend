package fr.epita.controller;

import fr.epita.dto.Response.NotificationResponse;
import fr.epita.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/students/{studentId}/notifications")
    public ResponseEntity<List<NotificationResponse>> getForStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(notificationService.getForStudent(studentId));
    }

    @GetMapping("/students/{studentId}/notifications/unread-count")
    public ResponseEntity<Long> unreadCount(@PathVariable Long studentId) {
        return ResponseEntity.ok(notificationService.unreadCount(studentId));
    }

    @PatchMapping("/notifications/{id}/read")
    public ResponseEntity<NotificationResponse> markRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markRead(id));
    }

    @PatchMapping("/students/{studentId}/notifications/read-all")
    public ResponseEntity<Void> markAllRead(@PathVariable Long studentId) {
        notificationService.markAllRead(studentId);
        return ResponseEntity.ok().build();
    }
}
