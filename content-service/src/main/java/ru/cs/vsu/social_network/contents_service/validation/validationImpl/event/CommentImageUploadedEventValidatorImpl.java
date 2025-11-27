// CommentImageUploadedEventValidatorImpl.java
package ru.cs.vsu.social_network.contents_service.validation.validationImpl.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.events.CommentImageUploadedEvent;
import ru.cs.vsu.social_network.contents_service.validation.event.CommentImageUploadedEventValidator;

@Slf4j
@Component
public final class CommentImageUploadedEventValidatorImpl
        implements CommentImageUploadedEventValidator {

    /**
     * Выполняет комплексную валидацию события загрузки изображения комментария.
     * Проверяет наличие обязательных полей и корректность их значений.
     *
     * @param event событие загрузки изображения комментария для валидации
     * @throws IllegalArgumentException если событие содержит некорректные данные
     */
    @Override
    public void validateEvent(final CommentImageUploadedEvent event) {
        log.debug("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_КОММЕНТАРИЯ_ВАЛИДАЦИЯ_НАЧАЛО: " +
                "eventId={}", event.getEventId());

        if (event.getEventId() == null) {
            log.error("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_КОММЕНТАРИЯ_ВАЛИДАЦИЯ_ОШИБКА: " +
                    "поле eventId не может быть пустым");
            throw new IllegalArgumentException("EventId не может быть пустым.");
        }

        if (event.getOwnerId() == null) {
            log.error("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_КОММЕНТАРИЯ_ВАЛИДАЦИЯ_ОШИБКА: " +
                            "eventId={}, поле ownerId не может быть пустым",
                    event.getEventId());
            throw new IllegalArgumentException("OwnerId не может быть пустым.");
        }

        if (event.getCommentId() == null) {
            log.error("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_КОММЕНТАРИЯ_ВАЛИДАЦИЯ_ОШИБКА: " +
                            "eventId={}, поле commentId не может быть пустым",
                    event.getEventId());
            throw new IllegalArgumentException("CommentId не может быть пустым.");
        }

        if (event.getPostId() == null) {
            log.error("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_КОММЕНТАРИЯ_ВАЛИДАЦИЯ_ОШИБКА: " +
                            "eventId={}, поле postId не может быть пустым",
                    event.getEventId());
            throw new IllegalArgumentException("PostId не может быть пустым.");
        }

        if (event.getPublicUrl() == null || event.getPublicUrl().trim().isEmpty()) {
            log.error("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_КОММЕНТАРИЯ_ВАЛИДАЦИЯ_ОШИБКА: " +
                            "eventId={}, поле publicUrl не может быть пустым",
                    event.getEventId());
            throw new IllegalArgumentException("PublicUrl не может быть пустым.");
        }

        if (event.getObjectName() == null || event.getObjectName().trim().isEmpty()) {
            log.error("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_КОММЕНТАРИЯ_ВАЛИДАЦИЯ_ОШИБКА: " +
                            "eventId={}, поле objectName не может быть пустым",
                    event.getEventId());
            throw new IllegalArgumentException("ObjectName не может быть пустым.");
        }

        if (event.getMimeType() == null || event.getMimeType().trim().isEmpty()) {
            log.error("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_КОММЕНТАРИЯ_ВАЛИДАЦИЯ_ОШИБКА: " +
                            "eventId={}, поле mimeType не может быть пустым",
                    event.getEventId());
            throw new IllegalArgumentException("MimeType не может быть пустым.");
        }

        if (event.getSize() == null || event.getSize() <= 0) {
            log.error("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_КОММЕНТАРИЯ_ВАЛИДАЦИЯ_ОШИБКА: " +
                            "eventId={}, поле size должно быть положительным числом",
                    event.getEventId());
            throw new IllegalArgumentException("Size должен быть положительным числом.");
        }

        log.debug("СОБЫТИЕ_ИЗОБРАЖЕНИЯ_КОММЕНТАРИЯ_ВАЛИДАЦИЯ_УСПЕХ:" +
                " eventId={}", event.getEventId());
    }
}