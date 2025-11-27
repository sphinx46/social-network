package ru.cs.vsu.social_network.upload_service.service;

import ru.cs.vsu.social_network.upload_service.dto.request.MediaUploadRequest;
import ru.cs.vsu.social_network.upload_service.dto.response.MediaResponse;

import java.util.UUID;

/**
 * Сервис для работы с изображениями постов.
 * Расширяет базовый функционал отправкой событий в Kafka.
 * Реализует бизнес-логику специфичную для изображений постов.
 */
public interface PostImageMediaService {

    /**
     * Загружает изображение поста и отправляет событие в Kafka.
     * Добавляет специфичную валидацию и бизнес-логику для изображений постов.
     *
     * @param request параметры загрузки изображения поста
     * @param postId идентификатор поста
     * @return данные сохранённого изображения поста
     */
    MediaResponse uploadPostImage(MediaUploadRequest request, UUID postId);
}