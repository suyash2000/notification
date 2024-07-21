package com.notification.notification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.notification.notification.dto.NotificationSearchResultDTO;
import com.notification.notification.entity.NotificationSearchEntity;


public interface NotificationService {
  Object consumeNotification(JsonNode notificationDetails);

  NotificationSearchResultDTO searchNotifications(NotificationSearchEntity notificationSearchEntity) throws Exception;
}

