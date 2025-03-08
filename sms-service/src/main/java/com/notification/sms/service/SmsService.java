package com.notification.sms.service;

import com.notification.sms.model.SmsNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class SmsService {

    private static final String SMS_TOPIC = "sms-notifications";
    private static final String DEAD_LETTER_TOPIC = "sms-notifications-dlt";
    private static final int MAX_RETRY_COUNT = 3;

    private final KafkaTemplate<String, SmsNotification> kafkaTemplate;

    @KafkaListener(topics = SMS_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void processSmsNotification(SmsNotification notification) {
        log.info("Received SMS notification: {}", notification);

        try {
            sendSms(notification);
            notification.setStatus("SENT");
            log.info("SMS sent successfully to: {}", notification.getPhoneNumber());
        } catch (Exception e) {
            log.error("Failed to send SMS to: {}, error: {}", notification.getPhoneNumber(), e.getMessage());
            handleFailedSms(notification, e);
        }
    }

    private void sendSms(SmsNotification notification) {
        // In a real implementation, this would connect to an SMS gateway/provider
        // For now, we'll simulate sending SMS by logging it
        log.info("Sending SMS to {}: {}", notification.getPhoneNumber(), notification.getMessage());

        // Simulate random failures to test retry mechanism (about 25% of the time)
        if (Math.random() < 0.25) {
            throw new RuntimeException("Simulated SMS provider failure");
        }

        // Simulate processing time
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("SMS sent successfully (simulated)");
    }

    private void handleFailedSms(SmsNotification notification, Exception e) {
        notification.setStatus("FAILED");

        if (notification.getRetryCount() < MAX_RETRY_COUNT) {
            notification.setRetryCount(notification.getRetryCount() + 1);
            notification.setCreatedAt(LocalDateTime.now());
            log.info("Retrying SMS notification (Attempt {}/{}): {}",
                    notification.getRetryCount(), MAX_RETRY_COUNT, notification.getId());

            kafkaTemplate.send(SMS_TOPIC, notification.getId(), notification);
        } else {
            log.error("Max retry count reached for SMS: {}, sending to dead letter topic", notification.getId());
            kafkaTemplate.send(DEAD_LETTER_TOPIC, notification.getId(), notification);
        }
    }
}