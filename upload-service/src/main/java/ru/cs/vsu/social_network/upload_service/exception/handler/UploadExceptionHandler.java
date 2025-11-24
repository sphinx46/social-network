package ru.cs.vsu.social_network.upload_service.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.cs.vsu.social_network.upload_service.dto.response.ApiErrorResponse;
import ru.cs.vsu.social_network.upload_service.exception.AccessDeniedException;
import ru.cs.vsu.social_network.upload_service.exception.InvalidFileException;
import ru.cs.vsu.social_network.upload_service.exception.MediaNotFoundException;
import ru.cs.vsu.social_network.upload_service.exception.MinioOperationException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public final class UploadExceptionHandler {

    @ExceptionHandler({InvalidFileException.class, MinioOperationException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            final RuntimeException ex,
            final HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(MediaNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            final MediaNotFoundException ex,
            final HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            final AccessDeniedException ex,
            final HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            final MethodArgumentNotValidException ex,
            final HttpServletRequest request) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.toList());
        return buildResponse(HttpStatus.BAD_REQUEST, "Ошибка валидации запроса", request, errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            final ConstraintViolationException ex,
            final HttpServletRequest request) {
        List<String> errors = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.toList());
        return buildResponse(HttpStatus.BAD_REQUEST, "Ошибка валидации запроса", request, errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            final Exception ex,
            final HttpServletRequest request) {
        log.error("UNEXPECTED_ERROR: path={}", request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Произошла непредвиденная ошибка.", request, List.of());
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            final HttpStatus status,
            final String message,
            final HttpServletRequest request,
            final List<String> errors) {
        ApiErrorResponse body = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .code(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .errors(errors)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}

