package ru.cs.vsu.social_network.messaging_service.provider;

import ru.cs.vsu.social_network.messaging_service.entity.Message;
import ru.cs.vsu.social_network.messaging_service.entity.enums.MessageStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Специализированный провайдер для доступа к сущностям Message.
 * Обеспечивает получение сообщений по идентификатору с обработкой ошибок.
 * Оптимизирован для работы с сообщениями в мессенджере.
 */
public interface MessageEntityProvider extends EntityProvider<Message> {

    /**
     * Получает количество непрочитанных сообщений для пользователя.
     *
     * @param receiverId идентификатор получателя
     * @param status статус сообщения
     * @return количество непрочитанных сообщений
     */
    Long getUnreadMessagesCountByUser(UUID receiverId,
                                      MessageStatus status);

    /**
     * Получает количество сообщений для списка бесед в пакетном режиме.
     *
     * @param conversationIds список идентификаторов бесед
     * @return маппинг conversationId -> количество сообщений
     */
    Map<UUID, Long> getMessagesCountsForConversations(List<UUID> conversationIds);

    /**
     * Получает последние сообщения для списка бесед с ограничением на каждую беседу.
     *
     * @param conversationIds список идентификаторов бесед
     * @param limit лимит сообщений на каждую беседу
     * @return список сообщений
     */
    List<Message> getRecentMessagesForConversations(List<UUID> conversationIds,
                                                    int limit);

    /**
     * Получает сообщения с предзагруженными беседами для списка идентификаторов.
     *
     * @param messageIds список идентификаторов сообщений
     * @param limit максимальное количество сообщений
     * @return список сообщений с загруженными беседами
     */
    List<Message> getMessagesWithConversations(List<UUID> messageIds,
                                               int limit);

    /**
     * Получает последние сообщения для беседы с ограничением.
     *
     * @param conversationId идентификатор беседы
     * @param limit максимальное количество возвращаемых сообщений
     * @return список последних сообщений для указанной беседы
     */
    List<Message> getRecentMessagesForConversation(UUID conversationId,
                                                   int limit);

    /**
     * Получает сообщения между двумя пользователями с пагинацией.
     *
     * @param senderId идентификатор отправителя
     * @param receiverId идентификатор получателя
     * @param page номер страницы
     * @param size размер страницы
     * @return список сообщений между пользователями
     */
    List<Message> getMessagesBetweenUsers(UUID senderId,
                                          UUID receiverId,
                                          int page,
                                          int size);

    /**
     * Получает сообщения пользователя-отправителя с пагинацией.
     *
     * @param userId идентификатор пользователя (как отправителя)
     * @param page номер страницы
     * @param size размер страницы
     * @return список сообщений пользователя
     */
    List<Message> getMessagesBySender(UUID userId,
                                      int page,
                                      int size);
}