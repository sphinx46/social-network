package ru.cs.vsu.social_network.upload_service.event.publisher;

import ru.cs.vsu.social_network.upload_service.entity.MediaEntity;

import java.util.UUID;

/**
 * Интерфейс для публикации событий, связанных с медиа-файлами.
 * Определяет контракт для отправки событий в систему сообщений.
 * Позволяет абстрагироваться от конкретной реализации брокера сообщений.
 */
public interface MediaEventPublisher {

    /**
     * Публикует событие загрузки аватара пользователя.
     * Отправляет событие с информацией о загруженном аватаре для уведомления
     * заинтересованных сервисов (например, user-profile-service).
     *
     * @param mediaEntity сущность медиа, содержащая метаданные загруженного аватара
     */
    void publishAvatarUploaded(MediaEntity mediaEntity);

    /**
     * Публикует событие загрузки изображения поста.
     * Отправляет событие с информацией о загруженном изображении поста для уведомления
     * заинтересованных сервисов (например, messaging-service).
     *
     * @param mediaEntity сущность медиа, содержащая метаданные загруженного изображения поста
     * @param postId идентификатор поста
     */
    void publishPostImageUploaded(MediaEntity mediaEntity, UUID postId);

    /**
     * Публикует событие загрузки изображения комментария.
     * Отправляет событие с информацией о загруженном изображении комментария для уведомления
     * заинтересованных сервисов (например, messaging-service).
     *
     * @param mediaEntity сущность медиа, содержащая метаданные загруженного изображения комментария
     * @param commentId идентификатор комментария
     * @param postId идентификатор поста
     */
    void publishCommentImageUploaded(MediaEntity mediaEntity, UUID commentId, UUID postId);

    /**
     * Публикует событие загрузки изображения сообщения.
     * Отправляет событие с информацией о загруженном изображении сообщения для уведомления
     * заинтересованных сервисов (например, messaging-service).
     *
     * @param mediaEntity сущность медиа, содержащая метаданные загруженного изображения сообщения
     * @param messageId идентификатор сообщения
     */
    void publishMessageImageUploaded(MediaEntity mediaEntity, UUID messageId);
}