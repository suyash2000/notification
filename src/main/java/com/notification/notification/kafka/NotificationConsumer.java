package com.notification.notification.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.notification.notification.service.EmailService;
import com.notification.notification.service.MvelService;
import com.notification.notification.service.RuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class NotificationConsumer {

    @Autowired
    private EmailService emailService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private RuleService ruleService;

    @Autowired
    private MvelService mvelService;

    private static final String DEAD_LETTER_TOPIC = "failed_notifications";
    private static final String ENRICHED_TOPIC = "enriched_notifications";

    @KafkaListener(topics = "send.notification", groupId = "notification_group")
    public void consume(String message) {
        try {
            // Convert String to JsonNode
            JsonNode notificationDetails = objectMapper.readTree(message);

            // Convert JsonNode to Map
            Map<String, Object> variables = objectMapper.convertValue(notificationDetails, Map.class);

            // Validation
            if (!isValidNotification(variables)) {
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
                // Route to a different topic if needed
                kafkaTemplate.send("send.notification", transformedNotification.toString());
            }

        } catch (Exception e) {
            log.error("Error processing message: ", e);
            kafkaTemplate.send(DEAD_LETTER_TOPIC, message);
        }
    }

    private boolean isValidNotification(Map<String, Object> variables) {
        try {
            String validationRuleExpression = ruleService.getRule("validationRule");
            if (validationRuleExpression == null) {
                return false;
            }

            log.debug("Evaluating validation rule: {}", validationRuleExpression);
            return mvelService.evaluateCondition(validationRuleExpression, variables);
        } catch (Exception e) {
            log.error("Error evaluating validation rule: ", e);
            return false;
        }
    }

    private JsonNode enrichNotification(JsonNode notification) {
        // Retrieve enrichment rule from Redis
        String enrichmentRuleExpression = ruleService.getRule("enrichmentRule");
        if (enrichmentRuleExpression != null) {
            // Evaluate enrichment rule using MVEL and apply enrichment
            Map<String, Object> variables = objectMapper.convertValue(notification, Map.class);
            Object enrichedValue = mvelService.evaluateExpression(enrichmentRuleExpression, variables);

            // Assuming enrichment adds the capitalized email to the payload
            ((ObjectNode) notification).put("enrichedEmail", enrichedValue.toString());
        }
        return notification;
    }

    private JsonNode transformNotification(JsonNode notification) {
        try {
            // Retrieve transformation rule from Redis
            String transformationRuleExpression = ruleService.getRule("transformationRule");
            if (transformationRuleExpression != null) {
                // Evaluate transformation rule using MVEL and apply transformation
                Map<String, Object> variables = objectMapper.convertValue(notification, Map.class);
                Object transformedValue = mvelService.evaluateExpression(transformationRuleExpression, variables);

                // Assuming the transformedValue is a Map, update the notification JsonNode
                if (transformedValue instanceof Map) {
                    ((ObjectNode) notification).setAll((ObjectNode) objectMapper.valueToTree(transformedValue));
                }

                // Log transformed notification for verification
                log.debug("Transformed notification: {}", notification.toString());
            }
        } catch (Exception e) {
            log.error("Error applying transformation rule: ", e);
        }
        return notification;
    }

    private boolean shouldRouteToEmail(JsonNode notification) {
        String routingRuleExpression = ruleService.getRule("routingRule");
        if (routingRuleExpression == null) {
            log.warn("Routing rule is not set.");
            return false;
        }

        Map<String, Object> variables = objectMapper.convertValue(notification, Map.class);
        log.debug("Evaluating routing rule: {}", routingRuleExpression);
        log.debug("Variables: {}", variables);

        boolean result = false;
        try {
            result = mvelService.evaluateCondition(routingRuleExpression, variables);
        } catch (Exception e) {
            log.error("Error evaluating routing rule: {}", e.getMessage());
        }

        log.debug("Routing result: {}", result);
        return result;
    }

}
