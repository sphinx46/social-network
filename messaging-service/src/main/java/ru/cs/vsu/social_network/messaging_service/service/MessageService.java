package ru.cs.vsu.social_network.messaging_service.service;

import ru.cs.vsu.social_network.messaging_service.dto.request.messaging.*;
import ru.cs.vsu.social_network.messaging_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.MessageResponse;
import ru.cs.vsu.social_network.messaging_service.dto.response.pageable.PageResponse;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с сообщениями.
 * Предоставляет методы для создания, редактирования, удаления и управления сообщениями.
 * Обеспечивает бизнес-логику обмена сообщениями между пользователями.
 */
public interface MessageService {

    /**
     * Создает новое сообщение с указанием беседы.
     * Автоматически создает или находит существующую беседу между пользователями.
     *
     * @param senderId идентификатор пользователя-отправителя
     * @param request DTO с данными для создания сообщения
     * @return DTO созданного сообщения
     */
    MessageResponse createMessage(UUID senderId,
                                  MessageCreateRequest request);

    /**
     * Редактирует существующее сообщение.
     * Проверяет права доступа пользователя к сообщению перед редактированием.
     *
     * @param keycloakUserId идентификатор пользователя, редактирующего сообщение
     * @param messageEditRequest DTO с данными для редактирования сообщения
     * @return DTO отредактированного сообщения
     */
    MessageResponse editMessage(UUID keycloakUserId,
                                MessageEditRequest messageEditRequest);

    /**
     * Удаляет сообщение.
     * Проверяет права доступа пользователя перед удалением сообщения.
     *
     * @param keycloakUserId идентификатор пользователя, удаляющего сообщение
     * @param messageDeleteRequest DTO с данными для удаления сообщения
     * @return DTO удаленного сообщения
     */
    MessageResponse deleteMessage(UUID keycloakUserId,
                                  MessageDeleteRequest messageDeleteRequest);

    /**
     * Загружает изображение для сообщения.
     * Обновляет URL изображения для указанного сообщения после проверки прав доступа.
     *
     * @param keycloakUserId идентификатор пользователя, загружающего изображение
     * @param request DTO с данными для загрузки изображения сообщения
     * @return DTO сообщения с обновленным изображением
     */
    MessageResponse uploadImage(UUID keycloakUserId,
                                MessageUploadImageRequest request);

    /**
     * Удаляет изображение из сообщения.
     * Устанавливает значение imageUrl в null для указанного сообщения.
     *
     * @param keycloakUserId идентификатор пользователя, удаляющего изображение
     * @param messageRemoveImageRequest DTO с данными для удаления изображения
     * @return DTO сообщения с удаленным изображением
     */
    MessageResponse removeImage(UUID keycloakUserId,
                                MessageRemoveImageRequest messageRemoveImageRequest);

    /**
     * Получает сообщение по его идентификатору.
     * Возвращает полную информацию о сообщении включая метаданные.
     *
     * @param messageId идентификатор сообщения
     * @return DTO найденного сообщения
     */
    MessageResponse getMessageById(UUID messageId);

    /**
     * Обновляет статус сообщений на DELIVERED (доставлено).
     * Используется при получении уведомления о доставке сообщения получателю.
     *
     * @param receiverId идентификатор пользователя-получателя
     * @param messageIds список идентификаторов сообщений для обновления статуса
     * @return количество обновленных сообщений
     */
    int markMessagesAsDelivered(UUID receiverId, List<UUID> messageIds);

    /**
     * Обновляет статус сообщений на READ (прочитано) для всей беседы.
     * Используется при открытии чата пользователем-получателем.
     * Обновляет все сообщения со статусами SENT и DELIVERED на READ.
     *
     * @param userId идентификатор пользователя-получателя
     * @param conversationId идентификатор беседы
     * @return количество обновленных сообщений
     */
    int markConversationAsRead(UUID userId, UUID conversationId);

    /**
     * Получает количество непрочитанных сообщений для пользователя.
     * Используется для отображения badge с количеством непрочитанных сообщений.
     *
     * @param receiverId идентификатор пользователя-получателя
     * @return количество непрочитанных сообщений
     */
    Long getUnreadMessagesCount(UUID receiverId);

    /**
     * Получает список сообщений между двумя пользователями с пагинацией.
     * Возвращает историю переписки между указанными пользователями.
     *
     * @param senderId идентификатор пользователя-отправителя
     * @param receiverId идентификатор пользователя-получателя
     * @param pageRequest параметры пагинации и сортировки
     * @return страница с сообщениями между пользователями
     */
    PageResponse<MessageResponse> getMessagesBetweenUsers(UUID senderId,
                                                          UUID receiverId,
                                                          PageRequest pageRequest);

    /**
     * Получает последние сообщения пользователя (как отправителя).
     * Используется для отображения отправленных сообщений в профиле.
     *
     * @param userId идентификатор пользователя
     * @param pageRequest параметры пагинации и сортировки
     * @return страница с сообщениями пользователя
     */
    PageResponse<MessageResponse> getMessagesBySender(UUID userId,
                                                      PageRequest pageRequest);
}