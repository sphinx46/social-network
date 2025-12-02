package ru.cs.vsu.social_network.messaging_service.service;

import ru.cs.vsu.social_network.messaging_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.ConversationDetailsResponse;
import ru.cs.vsu.social_network.messaging_service.dto.response.messaging.ConversationResponse;
import ru.cs.vsu.social_network.messaging_service.dto.response.pageable.PageResponse;

import java.util.UUID;

/**
 * Сервис для работы с беседами (переписками).
 * Предоставляет методы для создания, получения и управления беседами между пользователями.
 * Обеспечивает бизнес-логику работы с диалогами.
 */
public interface ConversationService {

    /**
     * Создает новую беседу между двумя пользователями.
     * Если беседа уже существует, возвращает существующую беседу.
     *
     * @param user1Id идентификатор первого пользователя
     * @param user2Id идентификатор второго пользователя
     * @return DTO созданной или найденной беседы
     */
    ConversationResponse createOrGetConversation(UUID user1Id, UUID user2Id);

    /**
     * Получает беседу по ее идентификатору.
     * Возвращает полную информацию о беседе включая метаданные.
     *
     * @param conversationId идентификатор беседы
     * @return DTO найденной беседы
     */
    ConversationResponse getConversationById(UUID conversationId);

    /**
     * Получает беседу между двумя конкретными пользователями.
     * Возвращает беседу, если она существует между указанными пользователями.
     *
     * @param user1Id идентификатор первого пользователя
     * @param user2Id идентификатор второго пользователя
     * @return DTO беседы между пользователями
     */
    ConversationResponse getConversationBetweenUsers(UUID user1Id, UUID user2Id);

    /**
     * Получает страницу бесед пользователя.
     * Возвращает список бесед, в которых участвует пользователь, с пагинацией.
     *
     * @param userId идентификатор пользователя
     * @param pageRequest параметры пагинации и сортировки
     * @return страница с беседами пользователя
     */
    PageResponse<ConversationResponse> getUserConversations(UUID userId,
                                                            PageRequest pageRequest);

    /**
     * Получает беседу с последними сообщениями.
     * Возвращает беседу вместе с ограниченным количеством последних сообщений.
     *
     * @param conversationId идентификатор беседы
     * @param messagesLimit максимальное количество возвращаемых сообщений
     * @return DTO беседы с сообщениями
     */
    ConversationDetailsResponse getConversationWithMessages(UUID conversationId,
                                                            int messagesLimit);

    /**
     * Получает подробную пагинированную беседу между двумя пользователями с сообщениями.
     * Используется для отображения полной истории переписки в интерфейсе чата.
     *
     * @param user1Id идентификатор первого пользователя
     * @param user2Id идентификатор второго пользователя
     * @param pageRequest параметры пагинации для сообщений
     * @return DTO беседы с пагинированными сообщениями
     */
    PageResponse<ConversationDetailsResponse> getConversationBetweenUsersWithMessages(UUID user1Id,
                                                                                       UUID user2Id,
                                                                                       PageRequest pageRequest);
    /**
     * Удаляет беседу и все связанные с ней сообщения.
     * Используется для полного удаления истории переписки между пользователями.
     *
     * @param conversationId идентификатор пользователя
     * @param conversationId идентификатор беседы
     */
    void deleteConversation(UUID keycloakUserId,
                            UUID conversationId);

    /**
     * Проверяет существование беседы между пользователями.
     * Используется для валидации перед созданием новой беседы.
     *
     * @param user1Id идентификатор первого пользователя
     * @param user2Id идентификатор второго пользователя
     * @return true, если беседа существует, иначе false
     */
    boolean existsConversationBetweenUsers(UUID user1Id, UUID user2Id);
}