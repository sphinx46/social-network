package ru.cs.vsu.social_network.upload_service.validation;

import org.springframework.web.multipart.MultipartFile;
public interface MediaValidator {
    void validateFile(MultipartFile file);
}
