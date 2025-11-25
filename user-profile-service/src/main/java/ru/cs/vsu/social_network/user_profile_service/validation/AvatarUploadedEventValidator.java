package ru.cs.vsu.social_network.user_profile_service.validation;

import ru.cs.vsu.social_network.user_profile_service.events.AvatarUploadedEvent;

/**
 * Специализированный интерфейс для валидации событий загрузки аватаров.
 * Расширяет базовый функционал валидации для работы с AvatarUploadedEvent.
 */
public interface AvatarUploadedEventValidator extends EventValidator<AvatarUploadedEvent> {
}