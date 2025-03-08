package com.notification.email.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for directly testing the email service without Kafka
 */
@RestController
@RequestMapping("/api/test-direct")
@RequiredArgsConstructor
@Slf4j
public class DirectTestController {

    private final JavaMailSender mailSender;

    @PostMapping("/send-email")
    public ResponseEntity<Map<String, Object>> sendDirectTestEmail() {
        log.info("Sending direct test email");

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("direct-test@example.com");
            message.setTo("recipient@example.com");
            message.setSubject("Direct Test Email");
            message.setText("This is a direct test email sent without using Kafka.");

            mailSender.send(message);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Direct test email sent successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to send direct test email", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to send direct test email");
            response.put("error", e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }
}