package ru.cs.vsu.social_network.messaging_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.cs.vsu.social_network.messaging_service.entity.Conversation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностью Conversation.
 * Предоставляет методы для выполнения операций с беседами в базе данных.
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    /**
     * Находит беседу между двумя пользователями.
     *
     * @param user1Id идентификатор первого пользователя
     * @param user2Id идентификатор второго пользователя
     * @return Optional беседы между пользователями
     */
    @Query("SELECT c FROM Conversation c WHERE (c.user1Id = :user1Id AND c.user2Id = :user2Id) " +
            "OR (c.user1Id = :user2Id AND c.user2Id = :user1Id)")
    Optional<Conversation> findConversationBetweenUsers(@Param("user1Id") UUID user1Id,
                                                        @Param("user2Id") UUID user2Id);

    /**
     * Проверяет существование беседы между двумя пользователями.
     *
     * @param user1Id идентификатор первого пользователя
     * @param user2Id идентификатор второго пользователя
     * @return true, если беседа существует, иначе false
     */
    @Query("SELECT COUNT(c) > 0 FROM Conversation c WHERE (c.user1Id = :user1Id AND c.user2Id = :user2Id) " +
            "OR (c.user1Id = :user2Id AND c.user2Id = :user1Id)")
    boolean existsConversationBetweenUsers(@Param("user1Id") UUID user1Id,
                                           @Param("user2Id") UUID user2Id);

    /**
     * Находит все беседы пользователя с пагинацией,
     * отсортированные по дате обновления (новые сначала).
     *
     * @param userId идентификатор пользователя
     * @param pageable параметры пагинации
     * @return страница с беседами пользователя
     */
    @Query("SELECT c FROM Conversation c WHERE c.user1Id = :userId OR c.user2Id = :userId " +
            "ORDER BY c.updatedAt DESC")
    Page<Conversation> findByUserIdOrderByUpdatedAtDesc(@Param("userId") UUID userId,
                                                        Pageable pageable);

    /**
     * Получает количество бесед пользователя.
     *
     * @param userId идентификатор пользователя
     * @return количество бесед пользователя
     */
    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.user1Id = :userId OR c.user2Id = :userId")
    long countByUserId(@Param("userId") UUID userId);

    /**
     * Находит беседы по списку идентификаторов.
     * Используется для батч-операций.
     *
     * @param conversationIds список идентификаторов бесед
     * @return список бесед
     */
    List<Conversation> findByIdIn(List<UUID> conversationIds);

    /**
     * Получает собеседника для беседы.
     * Если текущий пользователь - user1, возвращает user2, и наоборот.
     *
     * @param conversationId идентификатор беседы
     * @param currentUserId идентификатор текущего пользователя
     * @return идентификатор собеседника
     */
    @Query("SELECT CASE " +
            "WHEN c.user1Id = :currentUserId THEN c.user2Id " +
            "WHEN c.user2Id = :currentUserId THEN c.user1Id " +
            "ELSE null END " +
            "FROM Conversation c WHERE c.id = :conversationId")
    Optional<UUID> findInterlocutorId(@Param("conversationId") UUID conversationId,
                                      @Param("currentUserId") UUID currentUserId);

    /**
     * Получает последние беседы пользователя с лимитом.
     * Используется для предпросмотра в интерфейсе мессенджера.
     *
     * @param userId идентификатор пользователя
     * @param limit лимит бесед
     * @return список последних бесед
     */
    @Query("SELECT c FROM Conversation c WHERE c.user1Id = :userId OR c.user2Id = :userId " +
            "ORDER BY c.updatedAt DESC")
    List<Conversation> findRecentConversationsByUserId(@Param("userId") UUID userId,
                                                       int limit,
                                                       Pageable pageable);

    /**
     * Удаляет все сообщения беседы по её ID
     * @param conversationId ID беседы
     * @return количество удаленных сообщений
     */
    @Modifying
    @Query("DELETE FROM Message m WHERE m.conversation.id = :conversationId")
    int deleteAllByConversationId(@Param("conversationId") UUID conversationId);

}