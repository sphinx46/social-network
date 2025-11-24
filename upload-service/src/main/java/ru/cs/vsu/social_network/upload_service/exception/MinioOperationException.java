package ru.cs.vsu.social_network.upload_service.exception;

public class MinioOperationException extends RuntimeException {
    public MinioOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
