package com.notification.notification.service;

import org.mvel2.MVEL;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MvelService {

    public boolean evaluateCondition(String expression, Map<String, Object> variables) {
        return (Boolean) MVEL.eval(expression, variables);
    }

    public Object evaluateExpression(String expression, Map<String, Object> variables) {
        return MVEL.eval(expression, variables);
    }
}
