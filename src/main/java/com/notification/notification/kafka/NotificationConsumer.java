package com.notification.notification.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.notification.notification.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationConsumer {

    @Autowired
    private EmailService emailService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private static final String DEAD_LETTER_TOPIC = "failed_notifications";
    private static final String ENRICHED_TOPIC = "enriched_notifications";

    @KafkaListener(topics = "send.notification", groupId = "notification_group")
    public void consume(String message) {
        try {
            // Convert String to JsonNode
            JsonNode notificationDetails = objectMapper.readTree(message);

            // Validation
            if (!isValidNotification(notificationDetails)) {
                log.error("Invalid message: {}", message);
                kafkaTemplate.send(DEAD_LETTER_TOPIC, message);
                return;
            }

            // Enrichment
            JsonNode enrichedNotification = enrichNotification(notificationDetails);

            // Transformation
            JsonNode transformedNotification = transformNotification(enrichedNotification);

            // Routing
            if (shouldRouteToEmail(transformedNotification)) {
                emailService.sendNotificationEmail(transformedNotification.toString());
            } else {
                kafkaTemplate.send(ENRICHED_TOPIC, transformedNotification.toString());
            }

        } catch (Exception e) {
            log.error("Error processing message: ", e);
            kafkaTemplate.send(DEAD_LETTER_TOPIC, message);
        }
    }

    private boolean isValidNotification(JsonNode notification) {
        if (!notification.has("notificationId") || !notification.has("type")) {
            return false;
        }

        String type = notification.get("type").asText();
        if ("email".equalsIgnoreCase(type) && !notification.has("email")) {
            return false;
        } else if ("sms".equalsIgnoreCase(type) && !notification.has("mobileNumber")) {
            return false;
        }

        return true;
    }

    private JsonNode enrichNotification(JsonNode notification) {
        // Implement enrichment logic, e.g., add additional data
        ((ObjectNode) notification).put("enrichedField", "someValue");
        return notification;
    }

    private JsonNode transformNotification(JsonNode notification) {
        // Implement transformation logic, e.g., modify fields
        ((ObjectNode) notification).put("status", "processed");
        return notification;
    }

    private boolean shouldRouteToEmail(JsonNode notification) {
        // Implement routing logic, e.g., based on type or status
        return "email".equalsIgnoreCase(notification.get("type").asText());
    }
}
