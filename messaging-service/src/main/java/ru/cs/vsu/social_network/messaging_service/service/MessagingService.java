package ru.cs.vsu.social_network.messaging_service.service;

import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.MessageCreateRequest;
import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.MessageUploadImageRequest;
import ru.cs.vsu.social_network.messaging_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.ConversationDetailsResponse;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.MessageResponse;
import ru.cs.vsu.social_network.messaging_service.dto.response.pageable.PageResponse;

import java.util.UUID;

/**
 * Комплексный сервис для работы с мессенджером.
 * Объединяет функциональность работы с сообщениями и беседами.
 * Предоставляет высокоуровневые методы для типичных сценариев использования мессенджера.
 */
public interface MessagingService {

    /**
     * Отправляет новое сообщение пользователю.
     * Автоматически создает беседу, если она не существует.
     *
     * @param senderId идентификатор пользователя-отправителя
     * @param request DTO с данными для создания сообщения
     * @return DTO отправленного сообщения
     */
    MessageResponse sendMessage(UUID senderId, MessageCreateRequest request);

    /**
     * Получает полную переписку между двумя пользователями.
     * Возвращает беседу со всеми сообщениями с пагинацией.
     *
     * @param user1Id идентификатор первого пользователя (обычно текущий пользователь)
     * @param user2Id идентификатор второго пользователя (собеседник)
     * @param pageRequest параметры пагинации для сообщений
     * @return DTO беседы с пагинированными сообщениями
     */
    PageResponse<ConversationDetailsResponse> getConversationWithUser(UUID user1Id,
                                                        UUID user2Id,
                                                        PageRequest pageRequest);

    /**
     * Получает список бесед пользователя с предпросмотром последних сообщений.
     * Используется для отображения списка чатов в интерфейсе мессенджера.
     *
     * @param userId идентификатор пользователя
     * @param pageRequest параметры пагинации для бесед
     * @param previewMessagesLimit лимит сообщений для предпросмотра в каждой беседе
     * @return страница с беседами и предпросмотром сообщений
     */
    PageResponse<ConversationDetailsResponse> getUserConversationsWithPreview(UUID userId,
                                                                              PageRequest pageRequest,
                                                                              int previewMessagesLimit);

    /**
     * Отмечает сообщения как прочитанные при открытии чата.
     * Автоматически обновляет статус всех непрочитанных сообщений в беседе.
     *
     * @param userId идентификатор пользователя, открывающего чат
     * @param conversationId идентификатор беседы
     * @return количество отмеченных как прочитанные сообщений
     */
    int markConversationAsRead(UUID userId, UUID conversationId);

    /**
     * Загружает изображение в сообщение.
     * Объединяет функциональность поиска сообщения и обновления медиа.
     *
     * @param userId идентификатор пользователя, загружающего изображение
     * @param request DTO с данными для загрузки изображения
     * @return DTO сообщения с обновленным изображением
     */
    MessageResponse uploadMessageImage(UUID userId,
                                       MessageUploadImageRequest request);

    /**
     * Удаляет всю переписку с пользователем.
     * Удаляет беседу и все связанные сообщения.
     *
     * @param userId идентификатор текущего пользователя
     * @param otherUserId идентификатор собеседника
     */
    void deleteConversationWithUser(UUID userId, UUID otherUserId);

    /**
     * Получает информацию о беседе для отображения в интерфейсе чата.
     * Включает информацию о собеседнике и метаданные беседы.
     *
     * @param userId идентификатор текущего пользователя
     * @param conversationId идентификатор беседы
     * @return расширенная информация о беседе
     */
    ConversationDetailsResponse getChatInfo(UUID userId, UUID conversationId);

    /**
     * Получает страницу бесед пользователя с детальной информацией.
     *
     * @param userId идентификатор пользователя
     * @param pageRequest параметры пагинации
     * @return страница с детализированными беседами
     */
    PageResponse<ConversationDetailsResponse> getUserConversationsDetailed(UUID userId,
                                                                           PageRequest pageRequest);

    /**
     * Получает количество непрочитанных сообщений в конкретной беседе.
     *
     * @param userId идентификатор пользователя
     * @param conversationId идентификатор беседы
     * @return количество непрочитанных сообщений
     */
    Long getUnreadMessagesCountInConversation(UUID userId, UUID conversationId);
}