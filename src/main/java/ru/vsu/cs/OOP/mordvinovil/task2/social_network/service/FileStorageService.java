package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String saveFile(MultipartFile file, String subDirectory);
    boolean deleteFile(String fileUrl);
    void validateImageFile(MultipartFile file);
    String savePostImage(MultipartFile file, Long postId);
    String saveAvatar(MultipartFile file, Long userId);
}
