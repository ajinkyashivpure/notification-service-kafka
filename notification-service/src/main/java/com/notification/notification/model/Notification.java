package com.notification.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    private String id;

    @NotBlank(message = "Recipient is required")
    private String recipient;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Content is required")
    private String content;

    private Map<String, Object> additionalData;

    private List<NotificationType> channels;

    private LocalDateTime createdAt;

    private NotificationPriority priority;

    public enum NotificationType {
        EMAIL, SMS, PUSH
    }

    public enum NotificationPriority {
        LOW, MEDIUM, HIGH, URGENT
    }

    // Initialize default values if not provided
    public void initDefaults() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (priority == null) {
            priority = NotificationPriority.MEDIUM;
        }
    }
}