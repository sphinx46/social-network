package ru.cs.vsu.social_network.messaging_service.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.cs.vsu.social_network.messaging_service.exception.conversation.ConversationNotFoundException;
import ru.cs.vsu.social_network.messaging_service.exception.conversation.ConversationSelfException;
import ru.cs.vsu.social_network.messaging_service.exception.conversation.InterlocutorNotFoundException;
import ru.cs.vsu.social_network.messaging_service.exception.message.MessageNotFoundException;
import ru.cs.vsu.social_network.messaging_service.exception.message.MessageUploadImageException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Глобальный обработчик исключений для сервиса мессенджера.
 * Обеспечивает единообразную обработку исключений и возврат структурированных ответов.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает исключение ConversationNotFoundException.
     *
     * @param ex исключение
     * @return ответ с ошибкой 404
     */
    @ExceptionHandler(ConversationNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleConversationNotFoundException(
            final ConversationNotFoundException ex) {
        log.warn("БЕСЕДА_ОШИБКА_НЕ_НАЙДЕНА: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Conversation Not Found");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Обрабатывает исключение MessageNotFoundException.
     *
     * @param ex исключение
     * @return ответ с ошибкой 404
     */
    @ExceptionHandler(MessageNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleMessageNotFoundException(
            final MessageNotFoundException ex) {
        log.warn("СООБЩЕНИЕ_ОШИБКА_НЕ_НАЙДЕНО: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Message Not Found");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Обрабатывает исключение InterlocutorNotFoundException.
     *
     * @param ex исключение
     * @return ответ с ошибкой 404
     */
    @ExceptionHandler(InterlocutorNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleInterlocutorNotFoundException(
            final InterlocutorNotFoundException ex) {
        log.warn("СОБЕСЕДНИК_ОШИБКА_НЕ_НАЙДЕН: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Interlocutor Not Found");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Обрабатывает исключение ConversationSelfException.
     *
     * @param ex исключение
     * @return ответ с ошибкой 400
     */
    @ExceptionHandler(ConversationSelfException.class)
    public ResponseEntity<Map<String, Object>> handleConversationSelfException(
            final ConversationSelfException ex) {
        log.warn("БЕСЕДА_ОШИБКА_С_СОБОЙ: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Обрабатывает исключение MessageUploadImageException.
     *
     * @param ex исключение
     * @return ответ с ошибкой 400
     */
    @ExceptionHandler(MessageUploadImageException.class)
    public ResponseEntity<Map<String, Object>> handleMessageUploadImageException(
            final MessageUploadImageException ex) {
        log.warn("СООБЩЕНИЕ_ОШИБКА_ЗАГРУЗКА_ИЗОБРАЖЕНИЯ: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Обрабатывает исключение MissingRequestHeaderException.
     *
     * @param ex исключение
     * @return ответ с ошибкой 400
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String, Object>> handleMissingRequestHeaderException(
            final MissingRequestHeaderException ex) {
        log.warn("МЕССЕНДЖЕР_ОШИБКА_ОТСУТСТВУЕТ_ЗАГОЛОВОК: " +
                        "отсутствует обязательный заголовок - {}",
                ex.getHeaderName());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", "Отсутствует обязательный заголовок: " + ex.getHeaderName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Обрабатывает исключения валидации @Valid.
     *
     * @param ex исключение валидации
     * @return ответ с ошибкой 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            final MethodArgumentNotValidException ex) {
        log.warn("МЕССЕНДЖЕР_ОШИБКА_ВАЛИДАЦИИ: {}", ex.getMessage());

        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("Некорректные данные в запросе");

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Обрабатывает исключение AccessDeniedException.
     *
     * @param ex исключение
     * @return ответ с ошибкой 403
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            final AccessDeniedException ex) {
        log.warn("МЕССЕНДЖЕР_ОШИБКА_ДОСТУП_ЗАПРЕЩЕН: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Access Denied");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    /**
     * Обрабатывает исключение MissingServletRequestParameterException.
     *
     * @param ex исключение
     * @return ответ с ошибкой 400
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingServletRequestParameterException(
            final MissingServletRequestParameterException ex) {
        log.warn("МЕССЕНДЖЕР_ОШИБКА_ОТСУТСТВУЕТ_ПАРАМЕТР: " +
                        "отсутствует обязательный параметр - {}",
                ex.getParameterName());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", "Отсутствует обязательный параметр: " + ex.getParameterName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Обрабатывает все необработанные исключения.
     *
     * @param ex исключение
     * @return ответ с ошибкой 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            final Exception ex) {
        log.error("МЕССЕНДЖЕР_ОШИБКА_НЕОЖИДАННАЯ: неожиданная ошибка", ex);
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "Внутренняя ошибка сервера");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }
}