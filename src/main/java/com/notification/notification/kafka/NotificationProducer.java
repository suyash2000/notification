package com.notification.notification.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {

    private static final String TOPIC = "send.notification";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(JsonNode notificationDetails) {
        kafkaTemplate.send(TOPIC, String.valueOf(notificationDetails));
    }
}
