package ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class Logger {

    private final ObjectMapper objectMapper;

    public Logger(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void logInfo(String event, String message, Map<String, Object> context) {
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("event", event);
        logEntry.put("message", message);
        logEntry.put("context", context);
        logEntry.put("timestamp", Instant.now().toString());

        try {
            log.info(objectMapper.writeValueAsString(logEntry));
        } catch (JsonProcessingException e) {
            log.info("Event: {}, Message: {}, Context: {}", event, message, context);
        }
    }

    public void logError(String event, String message, Map<String, Object> context, Throwable error) {
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("event", event);
        logEntry.put("message", message);
        logEntry.put("context", context);
        logEntry.put("error", error.getMessage());
        logEntry.put("stackTrace", getStackTrace(error));
        logEntry.put("timestamp", Instant.now().toString());

        try {
            log.error(objectMapper.writeValueAsString(logEntry));
        } catch (JsonProcessingException e) {
            log.error("Event: {}, Message: {}, Context: {}, Error: {}",
                    event, message, context, error.getMessage());
        }
    }

    private String getStackTrace(Throwable error) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : error.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}