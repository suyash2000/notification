package com.notification.notification.dto;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class NotificationSearchResultDTO {
    JsonNode results;
    Map<String, Map<String, Long>> facets;
    long totalCount;
}