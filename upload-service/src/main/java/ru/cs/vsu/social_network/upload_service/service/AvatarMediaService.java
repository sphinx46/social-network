package ru.cs.vsu.social_network.upload_service.service;

import ru.cs.vsu.social_network.upload_service.dto.request.MediaUploadRequest;
import ru.cs.vsu.social_network.upload_service.dto.response.MediaResponse;

/**
 * Сервис для работы с аватарами пользователей.
 * Расширяет базовый функционал отправкой событий в Kafka.
 * Реализует бизнес-логику специфичную для аватаров.
 *
 */
public interface AvatarMediaService {

    /**
     * Загружает аватар пользователя и отправляет событие в Kafka.
     * Добавляет специфичную валидацию и бизнес-логику для аватаров.
     *
     * @param request параметры загрузки аватара
     * @return данные сохранённого аватара
     */
    MediaResponse uploadAvatar(MediaUploadRequest request);
}
