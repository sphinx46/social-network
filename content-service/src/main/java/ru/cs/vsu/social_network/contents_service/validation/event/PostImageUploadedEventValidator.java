package ru.cs.vsu.social_network.contents_service.validation.event;

import ru.cs.vsu.social_network.contents_service.events.PostImageUploadedEvent;

/**
 * Специализированный интерфейс для валидации событий загрузки изображений постов.
 * Расширяет базовый функционал валидации для работы с PostImageUploadedEvent.
 */
public interface PostImageUploadedEventValidator extends EventValidator<PostImageUploadedEvent> {
}