package ru.cs.vsu.social_network.messaging_service.provider;

import ru.cs.vsu.social_network.messaging_service.entity.Message;
import ru.cs.vsu.social_network.messaging_service.entity.enums.MessageStatus;

import java.util.List;
import java.util.UUID;

/**
 * Специализированный провайдер для доступа к сущностям Message.
 * Обеспечивает получение сообщений по идентификатору с обработкой ошибок.
 */
public interface MessageEntityProvider extends EntityProvider<Message> {

    /**
     * Получает количество непрочитанных сообщений для пользователя.
     * Используется для отображения badge с количеством непрочитанных сообщений.
     *
     * @param receiverId идентификатор получателя
     * @param status статус сообщения
     * @return количество непрочитанных сообщений
     */
    Long getUnreadMessagesCountByUser(UUID receiverId,
                                      MessageStatus status);

    /**
     * Получает количество непрочитанных сообщений для пользователя в конкретной беседе.
     * Используется для отображения количества непрочитанных в конкретном чате.
     *
     * @param receiverId идентификатор получателя
     * @param conversationId идентификатор беседы
     * @param status статус сообщения
     * @return количество непрочитанных сообщений в беседе
     */
    Long getUnreadMessagesCountInConversation(UUID receiverId,
                                              UUID conversationId,
                                              MessageStatus status);

    /**
     * Получает последние сообщения для списка бесед с ограничением на каждую беседу.
     * Использует оконные функции для оптимальной производительности.
     *
     * @param conversationIds список идентификаторов бесед
     * @param limit лимит сообщений на каждую беседу
     * @return список сообщений
     */
    List<Message> getRecentMessagesForConversations(List<UUID> conversationIds,
                                                    int limit);

    /**
     * Получает сообщения с предзагруженными беседами для списка идентификаторов.
     * Использует JOIN FETCH для устранения проблемы N+1.
     *
     * @param messageIds список идентификаторов сообщений
     * @return список сообщений с загруженными беседами
     */
    List<Message> getMessagesWithConversations(List<UUID> messageIds);

    /**
     * Получает последние сообщения для беседы с пагинацией.
     * Используется для загрузки истории сообщений в чате.
     *
     * @param conversationId идентификатор беседы
     * @param page номер страницы
     * @param size размер страницы
     * @return список сообщений в беседе
     */
    List<Message> getMessagesByConversation(UUID conversationId,
                                            int page,
                                            int size);

    /**
     * Получает сообщения между двумя пользователями с пагинацией.
     * Возвращает историю переписки между указанными пользователями.
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
     * Используется для отображения отправленных сообщений в профиле.
     *
     * @param userId идентификатор пользователя (как отправителя)
     * @param page номер страницы
     * @param size размер страницы
     * @return список сообщений пользователя
     */
    List<Message> getMessagesBySender(UUID userId,
                                      int page,
                                      int size);

    /**
     * Получает непрочитанные сообщения для пользователя в указанной беседе.
     * Используется для отметки сообщений как прочитанных при открытии чата.
     *
     * @param receiverId идентификатор пользователя-получателя
     * @param conversationId идентификатор беседы
     * @param status статус сообщения (например, DELIVERED для непрочитанных)
     * @return список непрочитанных сообщений
     */
    List<Message> getUnreadMessagesInConversation(UUID receiverId,
                                                  UUID conversationId,
                                                  MessageStatus status);

    /**
     * Получает сообщения по списку идентификаторов с пагинацией.
     * Оптимизирован для работы с большими списками идентификаторов.
     *
     * @param messageIds список идентификаторов сообщений
     * @param page номер страницы
     * @param size размер страницы
     * @return список сообщений
     */
    List<Message> getMessagesByIds(List<UUID> messageIds,
                                   int page,
                                   int size);
}