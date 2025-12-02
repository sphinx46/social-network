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

    /**
     * Получает ВСЕ непрочитанные сообщения для пользователя в указанной беседе
     * (со статусами SENT и DELIVERED).
     * Используется для массовой отметки сообщений как прочитанных при открытии чата.
     *
     * @param receiverId идентификатор пользователя-получателя
     * @param conversationId идентификатор беседы
     * @return список всех непрочитанных сообщений (SENT и DELIVERED)
     */
    List<MessageResponse> getAllUnreadMessagesInConversation(UUID receiverId, UUID conversationId);

    /**
     * Получает количество ВСЕХ непрочитанных сообщений для пользователя в указанной беседе
     * (сумма SENT и DELIVERED статусов).
     * Оптимизированная батч-версия для быстрого получения общего количества.
     *
     * @param receiverId идентификатор пользователя-получателя
     * @param conversationId идентификатор беседы
     * @return общее количество непрочитанных сообщений
     */
    Long getAllUnreadMessagesCountInConversation(UUID receiverId, UUID conversationId);

    /**
     * Получает количество ВСЕХ непрочитанных сообщений для пользователя
     * во множестве бесед (сумма SENT и DELIVERED статусов).
     * Оптимизированный батч-метод для получения бейджей в списке чатов.
     *
     * @param receiverId идентификатор пользователя-получателя
     * @param conversationIds список идентификаторов бесед
     * @return маппинг ID беседы -> общее количество непрочитанных сообщений
     */
    Map<UUID, Long> getAllUnreadMessagesCountInConversations(UUID receiverId,
                                                             List<UUID> conversationIds);

    /**
     * Получает сообщения для пользователя по списку ID с проверкой прав доступа
     * (что пользователь является получателем и сообщения в статусах SENT/DELIVERED).
     * Используется для валидации перед массовым обновлением статусов.
     *
     * @param receiverId идентификатор пользователя-получателя
     * @param messageIds список идентификаторов сообщений
     * @param status требуемый статус для фильтрации
     * @return список валидных сообщений для обновления статуса
     */
    List<MessageResponse> getValidMessagesForStatusUpdate(UUID receiverId,
                                                          List<UUID> messageIds,
                                                          MessageStatus status);

    /**
     * Получает сообщения между двумя пользователями с фильтрацией по статусам
     * (SENT и DELIVERED) для быстрого получения "непрочитанных".
     * Используется для оптимизации загрузки истории с подсветкой непрочитанных.
     *
     * @param senderId идентификатор пользователя-отправителя
     * @param receiverId идентификатор пользователя-получателя
     * @param statuses список статусов для фильтрации
     * @param page номер страницы
     * @param size размер страницы
     * @return список сообщений с указанными статусами
     */
    List<MessageResponse> getMessagesBetweenUsersWithStatuses(UUID senderId,
                                                              UUID receiverId,
                                                              List<MessageStatus> statuses,
                                                              int page,
                                                              int size);

    /**
     * Пакетное обновление статусов всех непрочитанных сообщений в беседе.
     * Обновляет сообщения со статусами SENT и DELIVERED на READ.
     *
     * @param receiverId идентификатор пользователя-получателя
     * @param conversationId идентификатор беседы
     * @return количество обновленных сообщений
     */
    int batchMarkConversationAsRead(UUID receiverId, UUID conversationId);

    /**
     * Пакетное обновление статуса сообщений в беседе с предварительной валидацией.
     * Более производительная версия для массового обновления в контексте одной беседы.
     *
     * @param receiverId идентификатор пользователя-получателя
     * @param conversationId идентификатор беседы
     * @param oldStatus текущий статус (для валидации)
     * @param newStatus новый статус
     * @return количество обновленных сообщений
     */
    int batchUpdateMessagesStatusInConversation(UUID receiverId,
                                                UUID conversationId,
                                                MessageStatus oldStatus,
                                                MessageStatus newStatus);

    /**
     * Пакетное обновление нескольких статусов на один новый статус в беседе.
     * Используется для прямой конвертации SENT -> READ без промежуточного DELIVERED.
     *
     * @param receiverId идентификатор пользователя-получателя
     * @param conversationId идентификатор беседы
     * @param oldStatuses список текущих статусов для обновления
     * @param newStatus новый статус
     * @return количество обновленных сообщений
     */
    int batchUpdateMessagesStatusesInConversation(UUID receiverId,
                                                  UUID conversationId,
                                                  List<MessageStatus> oldStatuses,
                                                  MessageStatus newStatus);

    /**
     * Массово обновляет статусы сообщений для пользователя с предварительной валидацией.
     * Оптимизированный метод для отметки множества сообщений как доставленных/прочитанных.
     *
     * @param receiverId идентификатор пользователя-получателя
     * @param messageIds список идентификаторов сообщений
     * @param oldStatus текущий статус (для валидации)
     * @param newStatus новый статус
     * @return количество успешно обновленных сообщений
     */
    int batchUpdateMessagesStatus(UUID receiverId,
                                  List<UUID> messageIds,
                                  MessageStatus oldStatus,
                                  MessageStatus newStatus);
}