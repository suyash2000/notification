package com.notification.notification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.notification.notification.dto.NotificationSearchResultDTO;
import com.notification.notification.entity.NotificationSearchEntity;


public interface NotificationService {
  Object createNotification(JsonNode notificationDetails);

  NotificationSearchResultDTO searchNotifications(NotificationSearchEntity notificationSearchEntity) throws Exception;
}

