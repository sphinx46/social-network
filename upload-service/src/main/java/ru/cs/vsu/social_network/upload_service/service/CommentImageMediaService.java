package ru.cs.vsu.social_network.upload_service.service;

import ru.cs.vsu.social_network.upload_service.dto.request.MediaUploadRequest;
import ru.cs.vsu.social_network.upload_service.dto.response.MediaResponse;

import java.util.UUID;

/**
 * Сервис для работы с изображениями комментариев.
 * Расширяет базовый функционал отправкой событий в Kafka.
 * Реализует бизнес-логику специфичную для изображений комментариев.
 */
public interface CommentImageMediaService {

    /**
     * Загружает изображение комментария и отправляет событие в Kafka.
     * Добавляет специфичную валидацию и бизнес-логику для изображений комментариев.
     *
     * @param request параметры загрузки изображения комментария
     * @param commentId идентификатор комментария
     * @param postId идентификатор поста
     * @return данные сохранённого изображения комментария
     */
    MediaResponse uploadCommentImage(MediaUploadRequest request,
                                     UUID commentId,
                                     UUID postId);
}