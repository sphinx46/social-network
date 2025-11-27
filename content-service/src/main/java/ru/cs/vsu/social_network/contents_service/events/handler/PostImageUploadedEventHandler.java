package ru.cs.vsu.social_network.contents_service.events.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.cs.vsu.social_network.contents_service.dto.request.post.PostUploadImageRequest;
import ru.cs.vsu.social_network.contents_service.events.PostImageUploadedEvent;
import ru.cs.vsu.social_network.contents_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.contents_service.service.PostService;
import ru.cs.vsu.social_network.contents_service.validation.event.PostImageUploadedEventValidator;


@Slf4j
@Component
@RequiredArgsConstructor
public class PostImageUploadedEventHandler implements EventHandler<PostImageUploadedEvent> {

    private final PostService postService;
    private final PostImageUploadedEventValidator validator;
    private final EntityMapper mapper;

    /**
     * Обрабатывает событие загрузки изображения поста:
     * 1. Валидирует входящее событие
     * 2. Преобразует в DTO для сервиса
     * 3. Вызывает бизнес-логику обновления изображения поста
     * 4. Управляет транзакцией и кешированием
     *
     * @param event событие загрузки изображения поста для обработки
     */
    @Override
    @Transactional
    @CacheEvict(value = "posts", key = "#event.ownerId")
    public void handle(final PostImageUploadedEvent event) {
        try {
            log.info("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_ПОСТА_ОБРАБОТКА_НАЧАЛО: eventId={}, userId={}",
                    event.getEventId(), event.getOwnerId());

            validator.validateEvent(event);
            log.debug("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_ПОСТА_ВАЛИДАЦИЯ_ПРОЙДЕНА: eventId={}", event.getEventId());

            final PostUploadImageRequest request = mapper.map(event, PostUploadImageRequest.class);
            log.debug("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_ПОСТА_ПРЕОБРАЗОВАНИЕ_УСПЕХ: eventId={}", event.getEventId());

            postService.uploadImage(event.getOwnerId(), request);

            log.info("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_ПОСТА_ОБРАБОТКА_УСПЕХ: eventId={}, userId={}",
                    event.getEventId(), event.getOwnerId());

        } catch (Exception e) {
            log.error("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_ПОСТА_ОБРАБОТКА_ОШИБКА: eventId={}, userId={}",
                    event.getEventId(), event.getOwnerId(), e);
            throw e;
        }
    }
}