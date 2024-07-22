package com.notification.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Slf4j
public class RuleService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String RULE_KEY_PREFIX = "rule:";

    @PostConstruct
    public void initializeRules() {
        saveRule("validationRule", "email != null && notificationId != null");
        saveRule("enrichmentRule", "variables['email'].toUpperCase()");
        saveRule("transformationRule", "variables['location'] = 'Updated Location'; variables");
        saveRule("routingRule", "type == 'email'");

        // Debug logs
        log.debug("Validation Rule: {}", getRule("validationRule"));
        log.debug("Enrichment Rule: {}", getRule("enrichmentRule"));
        log.debug("Transformation Rule: {}", getRule("transformationRule"));
        log.debug("Routing Rule: {}", getRule("routingRule"));
    }

    public void saveRule(String ruleId, String ruleExpression) {
        redisTemplate.opsForValue().set(RULE_KEY_PREFIX + ruleId, ruleExpression);
    }

    public String getRule(String ruleId) {
        return redisTemplate.opsForValue().get(RULE_KEY_PREFIX + ruleId);
    }

    public void deleteRule(String ruleId) {
        redisTemplate.delete(RULE_KEY_PREFIX + ruleId);
    }

}
