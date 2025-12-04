package ru.cs.vsu.social_network.messaging_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.cs.vsu.social_network.messaging_service.entity.Message;
import ru.cs.vsu.social_network.messaging_service.entity.enums.MessageStatus;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностью Message.
 * Предоставляет методы для выполнения операций с сообщениями в базе данных.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    /**
     * Находит все сообщения в беседе с пагинацией.
     *
     * @param conversationId идентификатор беседы
     * @param pageable       параметры пагинации и сортировки
     * @return страница с сообщениями беседы
     */
    Page<Message> findAllByConversationId(UUID conversationId,
                                          Pageable pageable);

    /**
     * Находит все сообщения между двумя пользователями с пагинацией.
     *
     * @param senderId   идентификатор отправителя
     * @param receiverId идентификатор получателя
     * @param pageable   параметры пагинации и сортировки
     * @return страница с сообщениями
     */
    @Query("SELECT m FROM Message m WHERE (m.senderId = :senderId AND m.receiverId = :receiverId) " +
            "OR (m.senderId = :receiverId AND m.receiverId = :senderId)")
    Page<Message> findMessagesBetweenUsers(@Param("senderId") UUID senderId,
                                           @Param("receiverId") UUID receiverId,
                                           Pageable pageable);

    /**
     * Получает количество сообщений для пользователя по статусу.
     *
     * @param receiverId идентификатор получателя
     * @param status     статус сообщения (например, DELIVERED)
     * @return количество сообщений с указанным статусом
     */
    long countByReceiverIdAndStatus(UUID receiverId,
                                    MessageStatus status);

    /**
     * Получает количество сообщений для пользователя по статусу в конкретной беседе.
     *
     * @param receiverId     идентификатор получателя
     * @param conversationId идентификатор беседы
     * @param status         статус сообщения (например, SENT, DELIVERED)
     * @return количество сообщений с указанным статусом в беседе
     */
    long countByReceiverIdAndConversationIdAndStatus(UUID receiverId,
                                                     UUID conversationId,
                                                     MessageStatus status);

    /**
     * Находит сообщения пользователя-отправителя с пагинацией,
     * отсортированные по дате создания (новые сначала).
     *
     * @param userId   идентификатор пользователя (как отправителя)
     * @param pageable параметры пагинации
     * @return страница с сообщениями
     */
    Page<Message> findBySenderIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Находит последние сообщения для списка бесед с лимитом на каждую беседу.
     * Использует оконные функции для правильного лимитирования на каждую беседу.
     *
     * @param conversationIds список идентификаторов бесед
     * @param limit           лимит сообщений на каждую беседу
     * @return список сообщений с загруженными беседами
     */
    @Query(value = """
            SELECT * FROM (
                SELECT m.*, 
                       ROW_NUMBER() OVER (PARTITION BY m.conversation_id ORDER BY m.created_at DESC) as rn
                FROM messages m 
                WHERE m.conversation_id IN :conversationIds
            ) ranked 
            WHERE ranked.rn <= :limit
            ORDER BY ranked.created_at DESC
            """, nativeQuery = true)
    List<Message> findRecentMessagesForConversations(@Param("conversationIds") List<UUID> conversationIds,
                                                     @Param("limit") int limit);

    /**
     * Находит все непрочитанные сообщения для пользователя в указанной беседе.
     *
     * @param receiverId     идентификатор пользователя-получателя
     * @param conversationId идентификатор беседы
     * @param status         статус сообщения (например, DELIVERED для непрочитанных)
     * @return список непрочитанных сообщений
     */
    List<Message> findByReceiverIdAndConversationIdAndStatus(UUID receiverId,
                                                             UUID conversationId,
                                                             MessageStatus status);

    /**
     * Находит сообщения по списку идентификаторов с предзагрузкой бесед.
     * Используется для эффективной загрузки сообщений с связанными данными.
     *
     * @param messageIds список идентификаторов сообщений
     * @return список сообщений с загруженными беседами
     */
    @Query("SELECT m FROM Message m JOIN FETCH m.conversation WHERE m.id IN :messageIds")
    List<Message> findMessagesWithConversations(@Param("messageIds") List<UUID> messageIds);

    /**
     * Батч-метод для поиска сообщений по идентификаторам с пагинацией.
     *
     * @param messageIds список идентификаторов сообщений
     * @param pageable   параметры пагинации
     * @return страница с сообщениями
     */
    Page<Message> findByIdIn(List<UUID> messageIds, Pageable pageable);


    /**
     * Массовое обновление статуса сообщений для беседы с проверкой получателя.
     * Используется для массовой отметки сообщений как доставленных/прочитанных.
     *
     * @param receiverId идентификатор получателя
     * @param conversationId идентификатор беседы
     * @param oldStatus текущий статус (для валидации)
     * @param newStatus новый статус
     * @return количество обновленных сообщений
     */
    @Modifying
    @Query("UPDATE Message m SET m.status = :newStatus " +
            "WHERE m.receiverId = :receiverId " +
            "AND m.conversation.id = :conversationId " +
            "AND m.status = :oldStatus")
    int updateMessagesStatusByConversation(@Param("receiverId") UUID receiverId,
                                           @Param("conversationId") UUID conversationId,
                                           @Param("oldStatus") MessageStatus oldStatus,
                                           @Param("newStatus") MessageStatus newStatus);

    @Modifying
    @Query("UPDATE Message m SET m.status = :newStatus " +
            "WHERE (m.receiverId = :receiverId OR m.senderId = :receiverId) " +
            "AND m.conversation.id = :conversationId " +
            "AND m.status IN :statuses")
    int updateMessagesStatusesByConversation(@Param("receiverId") UUID receiverId,
                                             @Param("conversationId") UUID conversationId,
                                             @Param("statuses") List<MessageStatus> statuses,
                                             @Param("newStatus") MessageStatus newStatus);

    /**
     * Пакетное обновление статуса сообщений с проверкой получателя и текущего статуса.
     * Оптимизированный запрос для массового обновления статусов.
     *
     * @param messageIds список идентификаторов сообщений
     * @param receiverId идентификатор получателя для валидации
     * @param oldStatus текущий статус (для проверки)
     * @param newStatus новый статус
     * @return количество обновленных сообщений
     */
    @Modifying
    @Query("UPDATE Message m SET m.status = :newStatus " +
            "WHERE m.id IN :messageIds " +
            "AND m.receiverId = :receiverId " +
            "AND m.status = :oldStatus")
    int updateMessagesStatusWithReceiverCheck(@Param("messageIds") List<UUID> messageIds,
                                              @Param("receiverId") UUID receiverId,
                                              @Param("oldStatus") MessageStatus oldStatus,
                                              @Param("newStatus") MessageStatus newStatus);
}