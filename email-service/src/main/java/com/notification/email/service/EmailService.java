package com.notification.email.service;

import com.notification.email.model.EmailNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private static final String EMAIL_TOPIC = "email-notifications";
    private static final String DEAD_LETTER_TOPIC = "email-notifications-dlt";
    private static final int MAX_RETRY_COUNT = 3;

    private final JavaMailSender mailSender;
    private final KafkaTemplate<String, EmailNotification> kafkaTemplate;

    @KafkaListener(topics = EMAIL_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void processEmailNotification(EmailNotification notification) {
        log.info("Received email notification: {}", notification);

        try {
            sendEmail(notification);
            notification.setStatus("SENT");
            log.info("Email sent successfully to: {}", notification.getTo());
        } catch (Exception e) {
            log.error("Failed to send email to: {}, error: {}", notification.getTo(), e.getMessage());
            handleFailedEmail(notification, e);
        }
    }

    private void sendEmail(EmailNotification notification) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(notification.getFrom());
        helper.setTo(notification.getTo());
        helper.setSubject(notification.getSubject());
        helper.setText(notification.getBody(), notification.isHtml());

        mailSender.send(message);
    }

    private void handleFailedEmail(EmailNotification notification, Exception e) {
        notification.setStatus("FAILED");

        if (notification.getRetryCount() < MAX_RETRY_COUNT) {
            notification.setRetryCount(notification.getRetryCount() + 1);
            notification.setCreatedAt(LocalDateTime.now());
            log.info("Retrying email notification (Attempt {}/{}): {}",
                    notification.getRetryCount(), MAX_RETRY_COUNT, notification.getId());

            kafkaTemplate.send(EMAIL_TOPIC, notification.getId(), notification);
        } else {
            log.error("Max retry count reached for email: {}, sending to dead letter topic", notification.getId());
            kafkaTemplate.send(DEAD_LETTER_TOPIC, notification.getId(), notification);
        }
    }
}