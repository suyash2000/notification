package com.notification.notification.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.notification.notification.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Override
    public void sendNotificationEmail(JsonNode notificationDetails) {
        try {
            // Extract the necessary fields from the nested JSON structure
            JsonNode emailConfig = notificationDetails.path("action").path("template").path("config");
            String sender = emailConfig.path("sender").asText(fromEmail); // Default to app config sender if not provided
            String subject = emailConfig.path("subject").asText();
            JsonNode recipientsNode = emailConfig.path("toEmail");
            List<String> recipients = new ArrayList<>();
            if (recipientsNode.isArray()) {
                for (JsonNode recipientNode : recipientsNode) {
                    recipients.add(recipientNode.asText());
                }
            }
            String messageBody = notificationDetails.path("action").path("template").path("message").asText();

            // Send an email to each recipient
            for (String recipient : recipients) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(sender);
                message.setTo(recipient);
                message.setSubject(subject);
                message.setText(messageBody);

                log.info("Sending email from: " + sender + " to: " + recipient + " with subject: " + subject);
                javaMailSender.send(message);
            }

        } catch (Exception e) {
            log.error("Error sending email: ", e);
            throw new RuntimeException("Email sending failed", e);
        }
    }
}