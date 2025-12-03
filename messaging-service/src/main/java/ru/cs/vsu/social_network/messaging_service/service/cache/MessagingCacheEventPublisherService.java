package ru.cs.vsu.social_network.messaging_service.service.cache;

import java.util.UUID;

/**
 * Сервис публикации событий инвалидации кэша мессенджера.
 * Обеспечивает асинхронную публикацию событий для инвалидации кэша переписок
 * с гарантией выполнения после коммита транзакции.
 * Использует TransactionSynchronization для правильного порядка операций.
 */
public interface MessagingCacheEventPublisherService {

    /**
     * Публикует событие создания нового сообщения.
     * Выполняется асинхронно после успешного коммита транзакции.
     *
     * @param source         источник события
     * @param target         целевая сущность
     * @param conversationId ID беседы
     * @param messageId      ID сообщения
     * @param senderId       ID отправителя
     * @param receiverId     ID получателя
     */
    void publishMessageCreated(Object source,
                               Object target,
                               UUID conversationId,
                               UUID messageId,
                               UUID senderId,
                               UUID receiverId);

    /**
     * Публикует событие обновления сообщения.
     * Выполняется асинхронно после успешного коммита транзакции.
     *
     * @param source         источник события
     * @param target         целевая сущность
     * @param conversationId ID беседы
     * @param messageId      ID сообщения
     */
    void publishMessageUpdated(Object source,
                               Object target,
                               UUID conversationId,
                               UUID messageId);

    /**
     * Публикует событие удаления сообщения.
     * Выполняется асинхронно после успешного коммита транзакции.
     *
     * @param source         источник события
     * @param target         целевая сущность
     * @param conversationId ID беседы
     * @param messageId      ID сообщения
     */
    void publishMessageDeleted(Object source,
                               Object target,
                               UUID conversationId,
                               UUID messageId);

    /**
     * Публикует событие создания новой беседы.
     * Выполняется асинхронно после успешного коммита транзакции.
     *
     * @param source         источник события
     * @param target         целевая сущность
     * @param conversationId ID беседы
     * @param user1Id        ID первого пользователя
     * @param user2Id        ID второго пользователя
     */
    void publishConversationCreated(Object source,
                                    Object target,
                                    UUID conversationId,
                                    UUID user1Id,
                                    UUID user2Id);

    /**
     * Публикует событие обновления статуса прочтения сообщений.
     * Выполняется асинхронно после успешного коммита транзакции.
     *
     * @param source         источник события
     * @param target         целевая сущность
     * @param conversationId ID беседы
     * @param userId         ID пользователя, прочитавшего сообщения
     */
    void publishMessagesRead(Object source,
                             Object target,
                             UUID conversationId,
                             UUID userId);

    /**
     * Публикует событие загрузки изображения.
     * Выполняется асинхронно после успешного коммита транзакции.
     *
     * @param source         источник события
     * @param target         целевая сущность
     * @param conversationId ID беседы
     * @param messageId      ID сообщения, для которго загружается изображение
     */
    void publishMessageImageUploaded(Object source,
                                     Object target,
                                     UUID conversationId,
                                     UUID messageId);
}
