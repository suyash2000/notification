package com.notification.notification.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.notification.configuration.ElasticsearchConfig;
import com.notification.notification.dto.NotificationSearchResultDTO;
import com.notification.notification.entity.NotificationSearchEntity;
import com.notification.notification.service.EmailService;
import com.notification.notification.service.NotificationService;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;


import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private static final String INDEX_NAME = "notifications";

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private ElasticsearchConfig elasticsearchConfig;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmailService emailService;

//    @Override
//    public JsonNode createNotification(JsonNode notificationDetails) {
//        log.info("Creating notification");
//
//        try {
//            if (notificationDetails != null) {
//                String id = notificationDetails.has("notificationId") ? notificationDetails.get("notificationId").asText() : UUID.randomUUID().toString();
//                Map<String, Object> notificationMap = new HashMap<>();
//
//                // Convert JsonNode to Map
//                notificationDetails.fields().forEachRemaining(entry -> notificationMap.put(entry.getKey(), entry.getValue().asText()));
//
//                // Set default status to "pending" if not provided
//                notificationMap.putIfAbsent("status", "pending");
//
//                IndexRequest indexRequest = new IndexRequest(INDEX_NAME)
//                        .id(id)
//                        .source(notificationMap);
//
//                IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
//
//                log.info("Notification created with ID: " + id);
//
//                // Retrieve and return the notification details
//                return getNotificationById(id);
//            } else {
//                log.info("Missing JSON data.");
//            }
//        } catch (IOException e) {
//            log.error("Error occurred: ", e);
//        }
//        return null;
//    }

//    public void updateNotificationStatus(String notificationId, String newStatus) {
//        log.info("Updating notification status");
//
//        try {
//            UpdateRequest updateRequest = new UpdateRequest(INDEX_NAME, notificationId)
//                    .doc("status", newStatus);
//
//            UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
//
//            log.info("Notification status updated to " + newStatus);
//        } catch (IOException e) {
//            log.error("Error occurred while updating status: ", e);
//        }
//    }

//    private JsonNode getNotificationById(String notificationId) throws IOException {
//        GetResponse getResponse = restHighLevelClient.get(new GetRequest(INDEX_NAME, notificationId), RequestOptions.DEFAULT);
//
//        if (getResponse.isExists()) {
//            Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
//            return objectMapper.convertValue(sourceAsMap, JsonNode.class);
//        } else {
//            log.info("Notification with ID: " + notificationId + " not found.");
//            return null;
//        }
//    }

//    @Override
//    public Object createNotification(JsonNode notificationDetails) {
//        // Extract details from the notificationDetails JSON node
//        String notificationId = notificationDetails.get("notificationId").asText();
//
//        // Send the email notification
//        emailService.sendNotificationEmail(notificationDetails.toString());
//
//        // Index the notification in Elasticsearch (optional)
//        try {
//            IndexRequest indexRequest = new IndexRequest("notifications")
//                    .id(notificationId)
//                    .source(notificationDetails.toString(), XContentType.JSON);
//            IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
//            return indexResponse.getResult();
//        } catch (IOException e) {
//            log.error("Error indexing notification in Elasticsearch: ", e);
//            return null;
//        }
//    }

    @Override
    public Object createNotification(JsonNode notificationDetails) {
        String notificationId = notificationDetails.get("notificationId").asText();

        try {
            // Send the email notification
            // TODO add if  for type = Email
            emailService.sendNotificationEmail(notificationDetails.toString());
            // Check if the document exists
            GetRequest getRequest = new GetRequest("notifications", notificationId);
            boolean exists = restHighLevelClient.exists(getRequest, RequestOptions.DEFAULT);
            // If the document doesn't exist, create it
            if (!exists) {
                createNotificationDocument(notificationId, notificationDetails);
            }
            // Update the status to "sent" in Elasticsearch
            updateNotificationStatus(notificationId, "sent");
            // Retrieve the updated document from Elasticsearch
            GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
            if (getResponse.isExists()) {
                return getResponse.getSource();
            } else {
                log.error("Notification with ID {} not found in Elasticsearch", notificationId);
                return null;
            }
        } catch (Exception e) {
            log.error("Error processing notification: ", e);
            return null;
        }
    }

    private void updateNotificationStatus(String notificationId, String status) {
        try {
            UpdateRequest updateRequest = new UpdateRequest("notifications", notificationId)
                    .doc("status", status);
            restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
            log.info("Notification status updated to: " + status);
        } catch (IOException e) {
            log.error("Error updating notification status in Elasticsearch: ", e);
        }
    }

    private void createNotificationDocument(String notificationId, JsonNode notificationDetails) {
        try {
            IndexRequest indexRequest = new IndexRequest("notifications")
                    .id(notificationId)
                    .source(notificationDetails.toString(), XContentType.JSON);
            restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            log.info("Notification document created with ID: " + notificationId);
        } catch (IOException e) {
            log.error("Error creating notification document in Elasticsearch: ", e);
        }
    }

    public NotificationSearchResultDTO searchNotifications(NotificationSearchEntity notificationSearchEntity) throws Exception {
        SearchSourceBuilder searchSourceBuilder = buildSearchSourceBuilder(notificationSearchEntity);

        RestHighLevelClient restHighLevelClient = elasticsearchConfig.getRestHighLevelClient();
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        searchRequest.source(searchSourceBuilder);

        try {
            // Get the total count without retrieving hits
            searchSourceBuilder.size(0);
            SearchResponse totalHitsSearchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            long totalCount = totalHitsSearchResponse.getHits().getTotalHits().value;

            Map<String, Map<String, Long>> fieldAggregations = new HashMap<>();

            if (notificationSearchEntity.getFacets() != null && !notificationSearchEntity.getFacets().isEmpty()) {
                for (String field : notificationSearchEntity.getFacets()) {
                    Terms fieldAggregation = totalHitsSearchResponse.getAggregations().get(field + "_agg");

                    Map<String, Long> fieldMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                    for (Terms.Bucket bucket : fieldAggregation.getBuckets()) {
                        if (!bucket.getKeyAsString().isEmpty()) {
                            fieldMap.put(bucket.getKeyAsString(), bucket.getDocCount());
                        }
                    }
                    fieldAggregations.put(field, fieldMap);
                }
            }

            // Configure pagination
            int startIndex = (notificationSearchEntity.getOffset() != null && notificationSearchEntity.getLimit() != null)
                    ? notificationSearchEntity.getOffset() * notificationSearchEntity.getLimit()
                    : 0;
            int endIndex = (notificationSearchEntity.getOffset() != null && notificationSearchEntity.getLimit() != null)
                    ? Math.min(startIndex + notificationSearchEntity.getLimit(), (int) totalCount)
                    : 10000;

            if (startIndex > endIndex) {
                throw new RuntimeException("ERROR: Requested page is beyond the available data.");
            }

            // Perform the paginated search
            searchSourceBuilder.from(startIndex);
            searchSourceBuilder.size(endIndex - startIndex);

            SearchResponse paginatedSearchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] hits = paginatedSearchResponse.getHits().getHits();

            List<Map<String, Object>> paginatedResult = new ArrayList<>();
            for (SearchHit hit : hits) {
                paginatedResult.add(hit.getSourceAsMap());
            }

            NotificationSearchResultDTO searchResultDTO = new NotificationSearchResultDTO();
            searchResultDTO.setResults(objectMapper.valueToTree(paginatedResult));
            searchResultDTO.setFacets(fieldAggregations);
            searchResultDTO.setTotalCount(totalCount);

            return searchResultDTO;
        } catch (IOException e) {
            throw new RuntimeException("Error executing search", e);
        }
    }

    private SearchSourceBuilder buildSearchSourceBuilder(NotificationSearchEntity notificationSearchEntity) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        if (notificationSearchEntity.getFilters() != null) {
            notificationSearchEntity.getFilters().forEach((field, value) -> {
                if (value instanceof Boolean) {
                    boolQueryBuilder.must(QueryBuilders.termQuery(field, value));
                } else if (value instanceof List) {
                    boolQueryBuilder.must(QueryBuilders.termsQuery(field + ".keyword", (List<?>) value));
                } else if (value instanceof String) {
                    boolQueryBuilder.must(QueryBuilders.matchQuery(field, value));
                } else if (value instanceof Date) {
                    boolQueryBuilder.must(QueryBuilders.rangeQuery(field).gte(value));
                }
            });
        }

        if (notificationSearchEntity.getQuery() != null) {
            String searchQuery = notificationSearchEntity.getQuery();
            boolQueryBuilder.must(
                    QueryBuilders.multiMatchQuery(searchQuery, "title", "message")
                            .type(MultiMatchQueryBuilder.Type.BEST_FIELDS)
            );
        }

        if (notificationSearchEntity.getSortField() != null && notificationSearchEntity.getSortDirection() != null) {
            SortOrder sortOrder = notificationSearchEntity.getSortDirection().equalsIgnoreCase("asc")
                    ? SortOrder.ASC
                    : SortOrder.DESC;
            searchSourceBuilder.sort(SortBuilders.fieldSort(notificationSearchEntity.getSortField()).order(sortOrder));
        } else {
            searchSourceBuilder.sort(SortBuilders.fieldSort("timestamp").order(SortOrder.DESC));
        }

        // Adjust fetchSource based on fields
        if (notificationSearchEntity.getFields() != null && !notificationSearchEntity.getFields().isEmpty()) {
            searchSourceBuilder.fetchSource(notificationSearchEntity.getFields().toArray(new String[0]), null);
        } else {
            // Exclude the "message" field if fields are not provided
            searchSourceBuilder.fetchSource(new String[]{"notificationId", "recipientId", "title", "timestamp", "status", "type", "mobileNumber", "email"}, null);
        }

        if (notificationSearchEntity.getFacets() != null && !notificationSearchEntity.getFacets().isEmpty()) {
            for (String field : notificationSearchEntity.getFacets()) {
                searchSourceBuilder.aggregation(AggregationBuilders.terms(field + "_agg").field(field + ".keyword"));
            }
        }

        return searchSourceBuilder;
    }

}