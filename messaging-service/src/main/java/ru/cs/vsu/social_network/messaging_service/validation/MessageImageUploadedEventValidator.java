package ru.cs.vsu.social_network.messaging_service.validation;

import ru.cs.vsu.social_network.messaging_service.event.MessageImageUploadedEvent;

/**
 * Специализированный интерфейс для валидации событий загрузки изображений в сообщениях.
 * Расширяет базовый функционал валидации для работы с MessageImageUploadedEvent.
 */
public interface MessageImageUploadedEventValidator extends EventValidator<MessageImageUploadedEvent> {
}