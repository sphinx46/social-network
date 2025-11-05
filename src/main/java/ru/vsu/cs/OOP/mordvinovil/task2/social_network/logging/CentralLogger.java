package ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class CentralLogger {

    private final ObjectMapper objectMapper;

    public CentralLogger(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void logInfo(String event, String message, Map<String, Object> context) {
        MDC.put("event", event);
        MDC.put("logger_type", "central");
        MDC.put("service", "social-network");

        try {
            String jsonLog = createJsonLog(event, "INFO", message, context, null);
            log.info(jsonLog);
        } catch (JsonProcessingException e) {
            log.info("EVENT={} | MESSAGE={} | CONTEXT={}", event, message, context);
        } finally {
            MDC.clear();
        }
    }

    public void logError(String event, String message, Map<String, Object> context, Throwable error) {
        MDC.put("event", event);
        MDC.put("logger_type", "central");
        MDC.put("service", "social-network");
        MDC.put("error_type", error.getClass().getSimpleName());

        try {
            String jsonLog = createJsonLog(event, "ERROR", message, context, error);
            log.error(jsonLog);
        } catch (JsonProcessingException e) {
            log.error("EVENT={} | MESSAGE={} | CONTEXT={} | ERROR={}",
                    event, message, context, error.getMessage());
        } finally {
            MDC.clear();
        }
    }

    private String createJsonLog(String event, String level, String message,
                                 Map<String, Object> context, Throwable error) throws JsonProcessingException {
        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("event", event);
        logEntry.put("message", message);
        logEntry.put("context", context != null ? context : Map.of());
        logEntry.put("timestamp", Instant.now().toString());
        logEntry.put("logger", "CentralLogger");
        logEntry.put("service", "social-network");
        logEntry.put("level", level);

        if (error != null) {
            logEntry.put("error", error.getMessage());
            logEntry.put("error_type", error.getClass().getSimpleName());
        }

        return objectMapper.writeValueAsString(logEntry);
    }
}