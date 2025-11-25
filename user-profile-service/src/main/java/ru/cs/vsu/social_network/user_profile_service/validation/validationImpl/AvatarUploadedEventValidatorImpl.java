package ru.cs.vsu.social_network.user_profile_service.validation.validationImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.user_profile_service.events.AvatarUploadedEvent;
import ru.cs.vsu.social_network.user_profile_service.validation.AvatarUploadedEventValidator;

@Slf4j
@Component
public final class AvatarUploadedEventValidatorImpl implements AvatarUploadedEventValidator {

    /**
     * Выполняет комплексную валидацию события загрузки аватара.
     * Проверяет наличие обязательных полей и корректность их значений.
     *
     * @param event событие загрузки аватара для валидации
     * @throws IllegalArgumentException если событие содержит некорректные данные
     */
    @Override
    public void validateEvent(final AvatarUploadedEvent event) {
        log.debug("СОБЫТИЕ_АВАТАРА_ВАЛИДАЦИЯ_НАЧАЛО: eventId={}", event.getEventId());

        if (event.getOwnerId() == null) {
            log.error("СОБЫТИЕ_АВАТАРА_ВАЛИДАЦИЯ_ОШИБКА: eventId={}, поле ownerId не может быть пустым",
                    event.getEventId());
            throw new IllegalArgumentException("OwnerId не может быть пустым.");
        }

        if (event.getPublicUrl() == null || event.getPublicUrl().trim().isEmpty()) {
            log.error("СОБЫТИЕ_АВАТАРА_ВАЛИДАЦИЯ_ОШИБКА: eventId={}, поле publicUrl не может быть пустым",
                    event.getEventId());
            throw new IllegalArgumentException("PublicUrl не может быть пустым.");
        }

        log.debug("СОБЫТИЕ_АВАТАРА_ВАЛИДАЦИЯ_УСПЕХ: eventId={}", event.getEventId());
    }
}