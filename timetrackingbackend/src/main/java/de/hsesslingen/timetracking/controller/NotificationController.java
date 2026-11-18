package de.hsesslingen.timetracking.controller;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.hsesslingen.timetracking.dto.NotificationResponseDTO;
import de.hsesslingen.timetracking.service.NotificationService;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api-timetracking/Notification-management")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/Notifications")
    public ResponseEntity<List<NotificationResponseDTO>> getNotifications(
            @RequestParam long employeeId,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        return ResponseEntity.ok(notificationService.getNotifications(employeeId, unreadOnly));
    }

    @GetMapping("/Notifications/unread-count")
    public ResponseEntity<Long> countUnread(@RequestParam long employeeId) {
        return ResponseEntity.ok(notificationService.countUnread(employeeId));
    }

    @PostMapping("/Notifications/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(
            @PathVariable("id") long id, @RequestParam long employeeId) {
        return ResponseEntity.ok(notificationService.markAsRead(id, employeeId));
    }

    @PostMapping("/Notifications/read-all")
    public ResponseEntity<Void> markAllAsRead(@RequestParam long employeeId) {
        notificationService.markAllAsRead(employeeId);
        return ResponseEntity.noContent().build();
    }
}
