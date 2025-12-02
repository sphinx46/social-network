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
 * Оптимизирован для production с использованием батч-операций и устранением проблем N+1.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    /**
     * Находит все сообщения в беседе с пагинацией.
     *
     * @param conversationId идентификатор беседы
     * @param pageable параметры пагинации и сортировки
     * @return страница с сообщениями беседы
     */
    Page<Message> findAllByConversationId(UUID conversationId,
                                          Pageable pageable);

    /**
     * Находит все сообщения от отправителя получателю с пагинацией.
     *
     * @param senderId идентификатор отправителя
     * @param receiverId идентификатор получателя
     * @param pageable параметры пагинации и сортировки
     * @return страница с сообщениями
     */
    @Query("SELECT m FROM Message m WHERE (m.senderId = :senderId AND m.receiverId = :receiverId) " +
            "OR (m.senderId = :receiverId AND m.receiverId = :senderId)")
    Page<Message> findMessagesBetweenUsers(@Param("senderId") UUID senderId,
                                           @Param("receiverId") UUID receiverId,
                                           Pageable pageable);

    /**
     * Находит сообщения пользователя-получателя по статусу.
     *
     * @param receiverId идентификатор получателя
     * @param status статус сообщения
     * @param pageable параметры пагинации
     * @return страница с непрочитанными сообщениями
     */
    Page<Message> findByReceiverIdAndStatus(UUID receiverId,
                                            MessageStatus status,
                                            Pageable pageable);

    /**
     * Получает количество сообщений для пользователя по статусу.
     *
     * @param receiverId идентификатор получателя
     * @param status статус сообщения (например, DELIVERED)
     * @return количество непрочитанных сообщений
     */
    long countByReceiverIdAndStatus(UUID receiverId,
                                    MessageStatus status);

    /**
     * Находит сообщения беседы с пагинацией, отсортированные по дате создания (новые сначала).
     *
     * @param conversationId идентификатор беседы
     * @param pageable параметры пагинации
     * @return страница с сообщениями
     */
    Page<Message> findByConversationIdOrderByCreatedAtDesc(UUID conversationId,
                                                           Pageable pageable);

    /**
     * Находит сообщения пользователя-отправителя с пагинацией,
     * отсортированные по дате создания (новые сначала).
     *
     * @param userId идентификатор пользователя (как отправителя)
     * @param pageable параметры пагинации
     * @return страница с сообщениями
     */
    Page<Message> findBySenderIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Получает количество сообщений для списка бесед в одном запросе.
     *
     * @param conversationIds список идентификаторов бесед
     * @return список кортежей [conversationId, count]
     */
    @Query("SELECT m.conversation.id, COUNT(m) FROM Message m WHERE m.conversation.id IN :conversationIds GROUP BY m.conversation.id")
    List<Object[]> findMessagesCountByConversationIds(@Param("conversationIds") List<UUID> conversationIds);

    /**
     * Находит последние сообщения для списка бесед с лимитом на каждую беседу.
     * Использует WINDOW FUNCTION для правильного лимитирования на каждую беседу.
     *
     * @param conversationIds список идентификаторов бесед
     * @param limit лимит сообщений на каждую беседу
     * @return список сообщений с загруженными беседами
     */
    @Query("SELECT m FROM Message m JOIN FETCH m.conversation WHERE m.id IN (" +
            "SELECT m2.id FROM Message m2 WHERE m2.conversation.id IN :conversationIds " +
            "AND (" +
            "  SELECT COUNT(*) FROM Message m3 " +
            "  WHERE m3.conversation.id = m2.conversation.id AND m3.createdAt >= m2.createdAt" +
            ") <= :limit" +
            ") ORDER BY m.createdAt DESC")
    List<Message> findRecentMessagesForConversations(@Param("conversationIds") List<UUID> conversationIds,
                                                     @Param("limit") int limit);

    /**
     * Находит сообщения с предзагруженными беседами для списка идентификаторов.
     *
     * @param messageIds список идентификаторов сообщений
     * @param pageable параметры пагинации
     * @return список сообщений с загруженными беседами
     */
    @Query("SELECT m FROM Message m JOIN FETCH m.conversation WHERE m.id IN :messageIds ORDER BY m.createdAt DESC")
    List<Message> findMessagesWithConversations(@Param("messageIds") List<UUID> messageIds,
                                                Pageable pageable);

    /**
     * Пакетное обновление статуса сообщений по их идентификаторам.
     *
     * @param messageIds список идентификаторов сообщений для обновления
     * @param newStatus новый статус сообщений
     * @return количество обновленных сообщений
     */
    @Modifying
    @Query("UPDATE Message m SET m.status = :newStatus WHERE m.id IN :messageIds")
    int updateMessagesStatus(@Param("messageIds") List<UUID> messageIds,
                             @Param("newStatus") MessageStatus newStatus);

    /**
     * Удаляет все сообщения беседы одним запросом.
     *
     * @param conversationId идентификатор беседы
     * @return количество удаленных сообщений
     */
    @Modifying
    @Query("DELETE FROM Message m WHERE m.conversation.id = :conversationId")
    int deleteByConversationId(@Param("conversationId") UUID conversationId);
}