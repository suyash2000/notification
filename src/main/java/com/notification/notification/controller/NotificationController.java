package com.notification.notification.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.notification.notification.dto.NotificationSearchResultDTO;
import com.notification.notification.entity.NotificationSearchEntity;
import com.notification.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // TODO change name to notification cusumers
    @PostMapping("/create")
    public ResponseEntity<Object> createNotification(@RequestBody JsonNode notificationDetails) {
        Object response = notificationService.createNotification(notificationDetails);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body("Error occurred while creating notification.");
        }
    }

    @PostMapping("/search")
    public ResponseEntity<NotificationSearchResultDTO> searchNotifications(@RequestBody NotificationSearchEntity notificationSearchEntity) {
        try {
            NotificationSearchResultDTO result = notificationService.searchNotifications(notificationSearchEntity);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Log the exception and return an appropriate error response
            // For a real-world application, you might want to use a more sophisticated error handling strategy
            return ResponseEntity.status(500).body(null);
        }
    }
}


