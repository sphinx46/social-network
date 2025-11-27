package ru.cs.vsu.social_network.contents_service.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.cs.vsu.social_network.contents_service.exception.comment.CommentNotFoundException;
import ru.cs.vsu.social_network.contents_service.exception.comment.CommentUploadImageException;
import ru.cs.vsu.social_network.contents_service.exception.like.LikeNotFoundException;
import ru.cs.vsu.social_network.contents_service.exception.post.PostNotFoundException;
import ru.cs.vsu.social_network.contents_service.exception.post.PostUploadImageException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Глобальный обработчик исключений для сервиса контента.
 * Обеспечивает единообразную обработку исключений и возврат структурированных ответов.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает исключение PostNotFoundException.
     *
     * @param ex исключение
     * @return ответ с ошибкой 404
     */
    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePostNotFoundException(
            final PostNotFoundException ex) {
        log.warn("ПОСТ_ОШИБКА_НЕ_НАЙДЕН: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Post Not Found");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Обрабатывает исключение LikeNotFoundException.
     *
     * @param ex исключение
     * @return ответ с ошибкой 404
     */
    @ExceptionHandler(LikeNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleLikeNotFoundException(
            final LikeNotFoundException ex) {
        log.warn("ЛАЙК_ОШИБКА_НЕ_НАЙДЕН: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Like Not Found");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Обрабатывает исключение CommentNotFoundException.
     *
     * @param ex исключение
     * @return ответ с ошибкой 404
     */
    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCommentNotFoundException(
            final CommentNotFoundException ex) {
        log.warn("КОММЕНТАРИЙ_ОШИБКА_НЕ_НАЙДЕН: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Comment Not Found");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Обрабатывает исключение PostUploadImageException.
     *
     * @param ex исключение
     * @return ответ с ошибкой 400
     */
    @ExceptionHandler(PostUploadImageException.class)
    public ResponseEntity<Map<String, Object>> handlePostUploadImageException(
            final PostUploadImageException ex) {
        log.warn("ПОСТ_ОШИБКА_ЗАГРУЗКА_ИЗОБРАЖЕНИЯ: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Обрабатывает исключение CommentUploadImageException.
     *
     * @param ex исключение
     * @return ответ с ошибкой 400
     */
    @ExceptionHandler(CommentUploadImageException.class)
    public ResponseEntity<Map<String, Object>> handleCommentUploadImageException(
            final CommentUploadImageException ex) {
        log.warn("КОММЕНТАРИЙ_ОШИБКА_ЗАГРУЗКА_ИЗОБРАЖЕНИЯ: {}", ex.getMessage());
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
        log.warn("КОНТЕНТ_ОШИБКА_ОТСУТСТВУЕТ_ЗАГОЛОВОК: " +
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
        log.warn("КОНТЕНТ_ОШИБКА_ВАЛИДАЦИИ: {}", ex.getMessage());

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
     * Обрабатывает все необработанные исключения.
     *
     * @param ex исключение
     * @return ответ с ошибкой 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            final Exception ex) {
        log.error("КОНТЕНТ_ОШИБКА_НЕОЖИДАННАЯ: неожиданная ошибка", ex);
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "Внутренняя ошибка сервера");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }
}