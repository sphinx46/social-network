package ru.cs.vsu.social_network.contents_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.cs.vsu.social_network.contents_service.entity.LikeComment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностью LikeComment.
 * Предоставляет методы для выполнения операций с лайками комментариев в базе данных.
 */
@Repository
public interface LikeCommentRepository extends JpaRepository<LikeComment, UUID> {

    /**
     * Находит лайк комментария по идентификатору пользователя и комментария
     *
     * @param ownerId идентификатор пользователя
     * @param commentId идентификатор комментария
     * @return Optional с лайком, если найден
     */
    Optional<LikeComment> findByOwnerIdAndCommentId(UUID ownerId, UUID commentId);

    /**
     * Проверяет существование лайка по идентификатору пользователя и комментария
     *
     * @param ownerId идентификатор пользователя
     * @param commentId идентификатор комментария
     * @return true если лайк существует
     */
    boolean existsByOwnerIdAndCommentId(UUID ownerId, UUID commentId);

    /**
     * Находит все лайки комментария с пагинацией
     *
     * @param commentId идентификатор комментария
     * @param pageable параметры пагинации
     * @return страница с лайками
     */
    Page<LikeComment> findAllByCommentId(UUID commentId, Pageable pageable);

    /**
     * Получает количество лайков комментария
     *
     * @param commentId идентификатор комментария
     * @return количество лайков
     */
    long countByCommentId(UUID commentId);

    /**
     * Получает количество лайков для списка комментариев в одном запросе
     *
     * @param commentIds список идентификаторов комментариев
     * @return список кортежей [commentId, count]
     */
    @Query("SELECT lc.comment.id, COUNT(lc) FROM LikeComment lc WHERE lc.comment.id IN :commentIds GROUP BY lc.comment.id")
    List<Object[]> findLikesCountByCommentIds(@Param("commentIds") List<UUID> commentIds);

    /**
     * Находит последние лайки для списка комментариев с лимитом на каждый комментарий.
     * Использует WINDOW FUNCTION для правильного лимитирования на каждый комментарий.
     *
     * @param commentIds список идентификаторов комментариев
     * @param limit лимит лайков на каждый комментарий
     * @return список лайков
     */
    @Query(value = """
        WITH ranked_likes AS (
            SELECT lc.*,
                   ROW_NUMBER() OVER (PARTITION BY lc.comment_id ORDER BY lc.created_at DESC) as rn
            FROM like_comment lc
            WHERE lc.comment_id IN (:commentIds)
        )
        SELECT * FROM ranked_likes WHERE rn <= :limit
        ORDER BY created_at DESC
        """, nativeQuery = true)
    List<LikeComment> findRecentLikesForComments(@Param("commentIds") List<UUID> commentIds,
                                                 @Param("limit") int limit);
}