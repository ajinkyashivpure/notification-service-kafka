package com.notification.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsNotification {
    private String id;
    private String phoneNumber;
    private String message;
    private LocalDateTime createdAt;
    private String status;
    private int retryCount;
}