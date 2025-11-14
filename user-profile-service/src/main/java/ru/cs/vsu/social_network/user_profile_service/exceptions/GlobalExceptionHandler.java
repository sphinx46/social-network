package ru.cs.vsu.social_network.user_profile_service.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.cs.vsu.social_network.user_profile_service.exceptions.profile.ProfileAlreadyExistsException;
import ru.cs.vsu.social_network.user_profile_service.exceptions.profile.ProfileBioTooLongException;
import ru.cs.vsu.social_network.user_profile_service.exceptions.profile.ProfileCityTooLongException;
import ru.cs.vsu.social_network.user_profile_service.exceptions.profile.ProfileNotFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает исключение ProfileNotFoundException.
     *
     * @param ex исключение
     * @return ответ с ошибкой 404
     */
    @ExceptionHandler(ProfileNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleProfileNotFoundException(ProfileNotFoundException ex) {
        log.warn("ПРОФИЛЬ_ОШИБКА_НЕ_НАЙДЕН: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Profile Not Found");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Обрабатывает исключение ProfileAlreadyExistsException.
     *
     * @param ex исключение
     * @return ответ с ошибкой 409
     */
    @ExceptionHandler(ProfileAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleProfileAlreadyExistsException(ProfileAlreadyExistsException ex) {
        log.warn("ПРОФИЛЬ_ОШИБКА_УЖЕ_СУЩЕСТВУЕТ: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Profile Already Exists");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /**
     * Обрабатывает исключение ProfileBioTooLongException.
     *
     * @param ex исключение
     * @return ответ с ошибкой 400
     */
    @ExceptionHandler(ProfileBioTooLongException.class)
    public ResponseEntity<Map<String, Object>> handleProfileBioTooLongException(ProfileBioTooLongException ex) {
        log.warn("ПРОФИЛЬ_ОШИБКА_БИО_ДЛИННОЕ: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Обрабатывает исключение ProfileCityTooLongException.
     *
     * @param ex исключение
     * @return ответ с ошибкой 400
     */
    @ExceptionHandler(ProfileCityTooLongException.class)
    public ResponseEntity<Map<String, Object>> handleProfileCityTooLongException(ProfileCityTooLongException ex) {
        log.warn("ПРОФИЛЬ_ОШИБКА_ГОРОД_ДЛИННЫЙ: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Обрабатывает все необработанные исключения.
     *
     * @param ex исключение
     * @return ответ с ошибкой 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("ПРОФИЛЬ_ОШИБКА_НЕОЖИДАННАЯ: неожиданная ошибка", ex);
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
