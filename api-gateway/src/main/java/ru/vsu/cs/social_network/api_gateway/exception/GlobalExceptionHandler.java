package ru.vsu.cs.social_network.api_gateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Order(-2)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    /**
     * Обрабатывает все исключения, возникающие в Gateway, и возвращает структурированный JSON ответ.
     *
     * @param exchange обмен данными веб-запроса
     * @param ex исключение для обработки
     * @return Mono с пустым результатом после отправки ответа об ошибке
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Internal server error";
        
        if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            status = HttpStatus.resolve(rse.getStatusCode().value()) != null 
                    ? HttpStatus.resolve(rse.getStatusCode().value()) 
                    : HttpStatus.INTERNAL_SERVER_ERROR;
            message = rse.getReason() != null ? rse.getReason() : message;
        } else if (ex instanceof IllegalStateException) {
            status = HttpStatus.BAD_REQUEST;
            message = ex.getMessage() != null ? ex.getMessage() : "Invalid request";
        } else if (ex instanceof IllegalArgumentException) {
            status = HttpStatus.BAD_REQUEST;
            message = ex.getMessage() != null ? ex.getMessage() : "Invalid argument";
        }
        
        log.error("ШЛЮЗ_ОШИБКА: обработка исключения для пути {}: {}", 
                exchange.getRequest().getPath(), ex.getMessage(), ex);
        
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", LocalDateTime.now().toString());
        errorBody.put("status", status.value());
        errorBody.put("error", status.getReasonPhrase());
        errorBody.put("message", message);
        errorBody.put("path", exchange.getRequest().getPath().value());
        
        String json = "{\"timestamp\":\"" + errorBody.get("timestamp") + 
                      "\",\"status\":" + errorBody.get("status") + 
                      ",\"error\":\"" + errorBody.get("error") + 
                      "\",\"message\":\"" + message.replace("\"", "\\\"") + 
                      "\",\"path\":\"" + errorBody.get("path") + "\"}";
        
        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}

