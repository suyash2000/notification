package com.notification.notification.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface EmailService {
    void sendNotificationEmail(JsonNode notificationDetails);
}
