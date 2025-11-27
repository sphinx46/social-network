package ru.cs.vsu.social_network.contents_service.validation.event;

import ru.cs.vsu.social_network.contents_service.events.CommentImageUploadedEvent;

/**
 * Специализированный интерфейс для валидации событий загрузки изображений комментариев.
 * Расширяет базовый функционал валидации для работы с CommentImageUploadedEvent.
 */
public interface CommentImageUploadedEventValidator extends EventValidator<CommentImageUploadedEvent> {
}