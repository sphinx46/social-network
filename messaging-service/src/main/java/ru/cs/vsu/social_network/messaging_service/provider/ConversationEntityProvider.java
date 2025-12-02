package ru.cs.vsu.social_network.messaging_service.provider;

import ru.cs.vsu.social_network.messaging_service.entity.Conversation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Специализированный провайдер для доступа к сущностям Conversation.
 * Обеспечивает получение бесед по идентификатору с обработкой ошибок.
 */
public interface ConversationEntityProvider extends EntityProvider<Conversation> {

    /**
     * Получает беседу между двумя пользователями.
     * Если беседа не существует, возвращает пустой Optional.
     *
     * @param user1Id идентификатор первого пользователя
     * @param user2Id идентификатор второго пользователя
     * @return Optional беседы между пользователями
     */
    Optional<Conversation> getConversationBetweenUsers(UUID user1Id, UUID user2Id);

    /**
     * Проверяет существование беседы между пользователями.
     * Используется для валидации перед созданием новой беседы.
     *
     * @param user1Id идентификатор первого пользователя
     * @param user2Id идентификатор второго пользователя
     * @return true, если беседа существует, иначе false
     */
    boolean existsConversationBetweenUsers(UUID user1Id, UUID user2Id);

    /**
     * Получает все беседы пользователя с пагинацией.
     * Используется для отображения списка чатов.
     *
     * @param userId идентификатор пользователя
     * @param page номер страницы (начиная с 0)
     * @param size размер страницы
     * @return список бесед пользователя
     */
    List<Conversation> getConversationsByUser(UUID userId, int page, int size);

    /**
     * Получает количество бесед пользователя.
     * Используется для пагинации и статистики.
     *
     * @param userId идентификатор пользователя
     * @return количество бесед пользователя
     */
    Long getConversationsCountByUser(UUID userId);

    /**
     * Получает беседы по списку идентификаторов.
     * Используется для батч-операций и предзагрузки данных.
     *
     * @param conversationIds список идентификаторов бесед
     * @return список бесед
     */
    List<Conversation> getConversationsByIds(List<UUID> conversationIds);

    /**
     * Получает идентификатор собеседника для беседы.
     * Если текущий пользователь - user1, возвращает user2, и наоборот.
     *
     * @param conversationId идентификатор беседы
     * @param currentUserId идентификатор текущего пользователя
     * @return идентификатор собеседника
     */
    Optional<UUID> getInterlocutorId(UUID conversationId, UUID currentUserId);

    /**
     * Получает последние беседы пользователя с ограничением по количеству.
     * Используется для предпросмотра в интерфейсе мессенджера.
     *
     * @param userId идентификатор пользователя
     * @param limit максимальное количество бесед
     * @return список последних бесед
     */
    List<Conversation> getRecentConversationsByUser(UUID userId, int limit);
}