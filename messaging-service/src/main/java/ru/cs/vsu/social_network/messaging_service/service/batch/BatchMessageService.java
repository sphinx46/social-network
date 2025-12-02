package ru.cs.vsu.social_network.messaging_service.service.batch;

import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.MessageResponse;
import ru.cs.vsu.social_network.messaging_service.entity.enums.MessageStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Сервис для пакетных операций с сообщениями.
 * Обеспечивает эффективное получение сообщений для множества бесед и пользователей.
 */
public interface BatchMessageService {

    /**
     * Получает количество непрочитанных сообщений для пользователя по статусу.
     * Используется для отображения общего количества непрочитанных сообщений.
     *
     * @param receiverId идентификатор пользователя-получателя
     * @param status статус сообщения (например, DELIVERED для непрочитанных)
     * @return количество непрочитанных сообщений
     */
    Long getUnreadMessagesCountByUser(UUID receiverId, MessageStatus status);

    /**
     * Получает количество непрочитанных сообщений для пользователя в конкретных беседах.
     * Используется для отображения badge с количеством непрочитанных в списке чатов.
     *
     * @param receiverId идентификатор пользователя-получателя
     * @param conversationIds список идентификаторов бесед
     * @param status статус сообщения
     * @return маппинг ID беседы -> количество непрочитанных сообщений
     */
    Map<UUID, Long> getUnreadMessagesCountInConversations(UUID receiverId,
                                                          List<UUID> conversationIds,
                                                          MessageStatus status);

    /**
     * Получает последние сообщения для списка бесед с ограничением на каждую беседу.
     * Используется для предпросмотра сообщений в списке чатов.
     *
     * @param conversationIds список идентификаторов бесед
     * @param messagesLimit лимит сообщений на каждую беседу
     * @return маппинг ID беседы -> список сообщений
     */
    Map<UUID, List<MessageResponse>> getRecentMessagesForConversations(List<UUID> conversationIds,
                                                                       int messagesLimit);

    /**
     * Получает сообщения для беседы с пагинацией.
     * Используется для загрузки истории сообщений в чате.
     *
     * @param conversationId идентификатор беседы
     * @param page номер страницы (начиная с 0)
     * @param size размер страницы
     * @return список сообщений в беседе
     */
    List<MessageResponse> getMessagesByConversation(UUID conversationId,
                                                    int page,
                                                    int size);

    /**
     * Получает сообщения между двумя пользователями с пагинацией.
     * Возвращает историю переписки между указанными пользователями.
     *
     * @param senderId идентификатор пользователя-отправителя
     * @param receiverId идентификатор пользователя-получателя
     * @param page номер страницы
     * @param size размер страницы
     * @return список сообщений между пользователями
     */
    List<MessageResponse> getMessagesBetweenUsers(UUID senderId,
                                                  UUID receiverId,
                                                  int page,
                                                  int size);

    /**
     * Получает сообщения с предзагруженными беседами для списка идентификаторов.
     * Устраняет проблему N+1 при доступе к связанным беседам.
     *
     * @param messageIds список идентификаторов сообщений
     * @return список сообщений с информацией о беседах
     */
    List<MessageResponse> getMessagesWithConversations(List<UUID> messageIds);

    /**
     * Получает непрочитанные сообщения для пользователя в указанной беседе.
     * Используется для отметки сообщений как прочитанных при открытии чата.
     *
     * @param receiverId идентификатор пользователя-получателя
     * @param conversationId идентификатор беседы
     * @param status статус сообщения (например, DELIVERED для непрочитанных)
     * @return список непрочитанных сообщений
     */
    List<MessageResponse> getUnreadMessagesInConversation(UUID receiverId,
                                                          UUID conversationId,
                                                          MessageStatus status);
}