package com.notification.sms.controller;

import com.notification.sms.model.SmsNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private static final String SMS_TOPIC = "sms-notifications";
    private final KafkaTemplate<String, SmsNotification> kafkaTemplate;

    @PostMapping("/send-sms")
    public ResponseEntity<Map<String, Object>> sendTestSms(@RequestBody SmsNotification request) {
        // Set defaults if not provided
        if (request.getId() == null || request.getId().isEmpty()) {
            request.setId(UUID.randomUUID().toString());
        }

        if (request.getCreatedAt() == null) {
            request.setCreatedAt(LocalDateTime.now());
        }

        if (request.getStatus() == null || request.getStatus().isEmpty()) {
            request.setStatus("NEW");
        }

        log.info("Sending test SMS notification: {}", request);

        kafkaTemplate.send(SMS_TOPIC, request.getId(), request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "SMS notification sent to Kafka");
        response.put("id", request.getId());

        return ResponseEntity.ok(response);
    }
}