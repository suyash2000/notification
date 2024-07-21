package com.notification.notification.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.notification.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Override
    public void sendNotificationEmail(String notificationDetails) {
        try {
            // Convert notificationDetails JSON string to an object
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(notificationDetails);

            String recipient = jsonNode.get("email").asText();
            String messageBody = jsonNode.get("message").asText();
            String subject = jsonNode.get("title").asText();

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail); // Set the sender email from properties
            message.setTo(recipient);
            message.setSubject(subject);
            message.setText(messageBody);

            // Log the email details for debugging
            log.info("Email in send from : " + fromEmail + " to " + recipient + " subject " + subject);
            javaMailSender.send(message);

        } catch (Exception e) {
            log.error("Error sending email: ", e);
            throw new RuntimeException("Email sending failed", e);
        }
    }
}