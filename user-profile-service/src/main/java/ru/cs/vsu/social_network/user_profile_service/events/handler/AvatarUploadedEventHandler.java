package ru.cs.vsu.social_network.user_profile_service.events.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.cs.vsu.social_network.user_profile_service.dto.request.ProfileUploadAvatarRequest;
import ru.cs.vsu.social_network.user_profile_service.events.AvatarUploadedEvent;
import ru.cs.vsu.social_network.user_profile_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.user_profile_service.service.ProfileService;
import ru.cs.vsu.social_network.user_profile_service.validation.AvatarUploadedEventValidator;

@Slf4j
@Component
@RequiredArgsConstructor
public class AvatarUploadedEventHandler implements EventHandler<AvatarUploadedEvent> {

    private final ProfileService profileService;
    private final AvatarUploadedEventValidator validator;
    private final EntityMapper mapper;

    /**
     * Обрабатывает событие загрузки аватара:
     * 1. Валидирует входящее событие
     * 2. Преобразует в DTO для сервиса
     * 3. Вызывает бизнес-логику обновления аватара
     * 4. Управляет транзакцией и кешированием
     *
     * @param event событие загрузки аватара для обработки
     */
    @Override
    @Transactional
    @CacheEvict(value = "profile", key = "#event.ownerId")
    public void handle(final AvatarUploadedEvent event) {
        try {
            log.info("СОБЫТИЕ_АВАТАРА_ОБРАБОТКА_НАЧАЛО: eventId={}, userId={}",
                    event.getEventId(), event.getOwnerId());

            validator.validateEvent(event);
            log.debug("СОБЫТИЕ_АВАТАРА_ВАЛИДАЦИЯ_ПРОЙДЕНА: eventId={}", event.getEventId());

            final ProfileUploadAvatarRequest request = mapper.map(event, ProfileUploadAvatarRequest.class);
            log.debug("СОБЫТИЕ_АВАТАРА_ПРЕОБРАЗОВАНИЕ_УСПЕХ: eventId={}", event.getEventId());

            profileService.uploadAvatar(event.getOwnerId(), request);

            log.info("СОБЫТИЕ_АВАТАРА_ОБРАБОТКА_УСПЕХ: eventId={}, userId={}",
                    event.getEventId(), event.getOwnerId());

        } catch (Exception e) {
            log.error("СОБЫТИЕ_АВАТАРА_ОБРАБОТКА_ОШИБКА: eventId={}, userId={}",
                    event.getEventId(), event.getOwnerId(), e);
            throw e;
        }
    }
}