package com.notification.notification.service;


import com.notification.notification.model.EmailNotification;
import com.notification.notification.model.Notification;
import com.notification.notification.model.SmsNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${notification.kafka.topics.email}")
    private String emailTopic;

    @Value("${notification.kafka.topics.sms}")
    private String smsTopic;

    @Value("${notification.kafka.topics.push}")
    private String pushTopic;

    @Value("${notification.default-sender.email}")
    private String defaultEmailSender;

    @Value("${notification.default-sender.sms}")
    private String defaultSmsProvider;

    public void processNotification(Notification notification) {
        log.info("Processing notification: {}", notification);

        // Initialize default values if needed
        notification.initDefaults();

        // If no channels specified, default to email
        if (notification.getChannels() == null || notification.getChannels().isEmpty()) {
            log.info("No channels specified, defaulting to email for notification: {}", notification.getId());
            sendEmailNotification(notification);
            return;
        }

        // Process each specified channel
        for (Notification.NotificationType channel : notification.getChannels()) {
            switch (channel) {
                case EMAIL:
                    sendEmailNotification(notification);
                    break;
                case SMS:
                    sendSmsNotification(notification);
                    break;
                default:
                    log.warn("Unsupported notification channel: {}", channel);
            }
        }
    }

    private void sendEmailNotification(Notification notification) {
        log.info("Sending email notification: {}", notification.getId());

        EmailNotification emailNotification = EmailNotification.builder()
                .id(UUID.randomUUID().toString())
                .to(notification.getRecipient())
                .from(defaultEmailSender)
                .subject(notification.getSubject())
                .body(notification.getContent())
                .isHtml(false)
                .templateData(notification.getAdditionalData())
                .createdAt(LocalDateTime.now())
                .status("NEW")
                .retryCount(0)
                .build();

        kafkaTemplate.send(emailTopic, emailNotification.getId(), emailNotification);
        log.info("Email notification sent to Kafka topic: {}", emailTopic);
    }

    private void sendSmsNotification(Notification notification) {
        log.info("Sending SMS notification: {}", notification.getId());

        SmsNotification smsNotification = SmsNotification.builder()
                .id(UUID.randomUUID().toString())
                .phoneNumber(notification.getRecipient())
                .message(notification.getContent())
                .createdAt(LocalDateTime.now())
                .status("NEW")
                .retryCount(0)
                .build();

        kafkaTemplate.send(smsTopic, smsNotification.getId(), smsNotification);
        log.info("SMS notification sent to Kafka topic: {}", smsTopic);
    }

}