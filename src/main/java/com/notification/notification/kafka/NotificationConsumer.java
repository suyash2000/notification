package com.notification.notification.kafka;

import com.notification.notification.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

@Autowired
 private EmailService emailService;

  @KafkaListener(topics = "send.notification", groupId = "notification_group")
  public void consume(String message) {
     // Assume message is JSON string containing notification details
      emailService.sendNotificationEmail(message);
   }
}