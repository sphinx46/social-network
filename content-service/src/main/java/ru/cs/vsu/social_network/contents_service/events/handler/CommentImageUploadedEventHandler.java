package ru.cs.vsu.social_network.contents_service.events.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.cs.vsu.social_network.contents_service.dto.request.comment.CommentUploadImageRequest;
import ru.cs.vsu.social_network.contents_service.events.CommentImageUploadedEvent;
import ru.cs.vsu.social_network.contents_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.contents_service.service.CommentService;
import ru.cs.vsu.social_network.contents_service.validation.event.CommentImageUploadedEventValidator;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentImageUploadedEventHandler implements EventHandler<CommentImageUploadedEvent> {

    private final CommentService commentService;
    private final CommentImageUploadedEventValidator validator;
    private final EntityMapper mapper;

    /**
     * Обрабатывает событие загрузки изображения комментария:
     * 1. Валидирует входящее событие
     * 2. Преобразует в DTO для сервиса
     * 3. Вызывает бизнес-логику обновления изображения комментария
     * 4. Управляет транзакцией и кешированием
     *
     * @param event событие загрузки изображения комментария для обработки
     */
    @Override
    @Transactional
    @CacheEvict(value = "comments", key = "#event.ownerId")
    public void handle(final CommentImageUploadedEvent event) {
        try {
            log.info("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_КОММЕНТАРИЯ_ОБРАБОТКА_НАЧАЛО: " +
                            "eventId={}, userId={}",
                    event.getEventId(), event.getOwnerId());

            validator.validateEvent(event);
            log.debug("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_КОММЕНТАРИЯ_ВАЛИДАЦИЯ_ПРОЙДЕНА:" +
                    " eventId={}", event.getEventId());

            final CommentUploadImageRequest request = mapper.map(event,
                    CommentUploadImageRequest.class);
            log.debug("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_КОММЕНТАРИЯ_ПРЕОБРАЗОВАНИЕ_УСПЕХ: " +
                    "eventId={}", event.getEventId());

            commentService.uploadImage(event.getOwnerId(), request);

            log.info("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_КОММЕНТАРИЯ_ОБРАБОТКА_УСПЕХ: " +
                            "eventId={}, userId={}",
                    event.getEventId(), event.getOwnerId());

        } catch (Exception e) {
            log.error("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_КОММЕНТАРИЯ_ОБРАБОТКА_ОШИБКА: " +
                            "eventId={}, userId={}",
                    event.getEventId(), event.getOwnerId(), e);
            throw e;
        }
    }
}