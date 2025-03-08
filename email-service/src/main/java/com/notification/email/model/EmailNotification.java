package com.notification.email.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotification {
    private String id;
    private String to;
    private String from;
    private String subject;
    private String body;
    private Map<String, Object> templateData;
    private boolean isHtml;
    private LocalDateTime createdAt;
    private String status;
    private int retryCount;
}