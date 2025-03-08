package com.notification.notification.controller;

import com.notification.notification.model.Notification;
import com.notification.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> sendNotification(@Valid @RequestBody Notification notification) {
        log.info("Received notification request: {}", notification);

        try {
            notificationService.processNotification(notification);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Notification processed successfully");
            response.put("notificationId", notification.getId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing notification", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to process notification");
            response.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "notification-service");
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}