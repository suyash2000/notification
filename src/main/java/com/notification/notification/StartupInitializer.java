package com.notification.notification;

import com.notification.notification.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StartupInitializer {

    @Autowired
    private RuleService ruleService;

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        ruleService.initializeRules();
    }
}
