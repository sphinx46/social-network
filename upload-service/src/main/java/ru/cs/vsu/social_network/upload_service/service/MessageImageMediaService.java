package ru.cs.vsu.social_network.upload_service.service;

import ru.cs.vsu.social_network.upload_service.dto.request.MediaUploadRequest;
import ru.cs.vsu.social_network.upload_service.dto.response.MediaResponse;

import java.util.UUID;

/**
 * Сервис для работы с изображениями сообщений.
 * Расширяет базовый функционал отправкой событий в Kafka.
 * Реализует бизнес-логику специфичную для изображений сообщений.
 */
public interface MessageImageMediaService {

    /**
     * Загружает изображение сообщения и отправляет событие в Kafka.
     * Добавляет специфичную валидацию и бизнес-логику для изображений сообщений.
     *
     * @param request параметры загрузки изображения сообщения
     * @param messageId идентификатор сообщения
     * @return данные сохранённого изображения сообщения
     */
    MediaResponse uploadMessageImage(MediaUploadRequest request, UUID messageId);
}