package ru.cs.vsu.social_network.contents_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.cs.vsu.social_network.contents_service.entity.LikePost;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностью LikePost.
 * Предоставляет методы для выполнения операций с лайками постов в базе данных.
 */
@Repository
public interface LikePostRepository extends JpaRepository<LikePost, UUID> {

    /**
     * Находит лайк поста по идентификатору пользователя и поста.
     *
     * @param ownerId идентификатор пользователя
     * @param postId идентификатор поста
     * @return Optional с лайком, если найден
     */
    Optional<LikePost> findByOwnerIdAndPostId(UUID ownerId, UUID postId);

    /**
     * Проверяет существование лайка по идентификатору пользователя и поста.
     *
     * @param ownerId идентификатор пользователя
     * @param postId идентификатор поста
     * @return true если лайк существует
     */
    boolean existsByOwnerIdAndPostId(UUID ownerId, UUID postId);

    /**
     * Находит все лайки поста с пагинацией.
     *
     * @param postId идентификатор поста
     * @param pageable параметры пагинации
     * @return страница с лайками
     */
    Page<LikePost> findAllByPostId(UUID postId, Pageable pageable);

    /**
     * Получает количество лайков поста.
     *
     * @param postId идентификатор поста
     * @return количество лайков
     */
    long countByPostId(UUID postId);

    /**
     * Находит топ N лайков для поста с сортировкой по дате создания.
     *
     * @param postId идентификатор поста
     * @param pageable параметры пагинации
     * @return список лайков
     */
    List<LikePost> findByPostIdOrderByCreatedAtDesc(UUID postId, Pageable pageable);

    /**
     * Получает количество лайков для списка постов в одном запросе.
     *
     * @param postIds список идентификаторов постов
     * @return список кортежей [postId, count]
     */
    @Query("SELECT lp.post.id, COUNT(lp) FROM LikePost lp WHERE lp.post.id IN :postIds GROUP BY lp.post.id")
    List<Object[]> findLikesCountByPostIds(@Param("postIds") List<UUID> postIds);

    /**
     * Находит последние лайки для списка постов с лимитом на каждый пост.
     * Использует WINDOW FUNCTION для правильного лимитирования на каждый пост.
     *
     * @param postIds список идентификаторов постов
     * @param limit лимит лайков на каждый пост
     * @return список лайков
     */
    @Query(value = """
        WITH ranked_likes AS (
            SELECT lp.*,
                   ROW_NUMBER() OVER (PARTITION BY lp.post_id ORDER BY lp.created_at DESC) as rn
            FROM like_post lp
            WHERE lp.post_id IN (:postIds)
        )
        SELECT * FROM ranked_likes WHERE rn <= :limit
        """, nativeQuery = true)
    List<LikePost> findRecentLikesForPosts(@Param("postIds") List<UUID> postIds,
                                           @Param("limit") int limit);

    /**
     * Находит лайки с предзагруженными постами для списка идентификаторов.
     * Использует JOIN FETCH для устранения проблемы N+1 при доступе к связанным постам.
     *
     * @param postIds список идентификаторов постов
     * @param pageable параметры пагинации
     * @return список лайков с загруженными постами
     */
    @Query("SELECT lp FROM LikePost lp JOIN FETCH lp.post WHERE lp.post.id IN :postIds ORDER BY lp.createdAt DESC")
    List<LikePost> findLikesWithPosts(@Param("postIds") List<UUID> postIds, Pageable pageable);

    /**
     * Находит лайки пользователя для списка постов.
     * Эффективный метод для проверки, какие посты пользователь лайкнул.
     *
     * @param ownerId идентификатор пользователя
     * @param postIds список идентификаторов постов
     * @return список лайков пользователя для указанных постов
     */
    @Query("SELECT lp FROM LikePost lp WHERE lp.ownerId = :ownerId AND lp.post.id IN :postIds")
    List<LikePost> findByOwnerIdAndPostIds(@Param("ownerId") UUID ownerId,
                                           @Param("postIds") List<UUID> postIds);

    /**
     * Находит последние лайки для списка постов с лимитом на каждый пост.
     * Использует JPQL с подзапросом для правильного лимитирования на каждый пост.
     * Устраняет проблему N+1 при получении лайков для нескольких постов.
     *
     * @param postIds список идентификаторов постов
     * @param limit лимит лайков на каждый пост
     * @return список лайков с загруженными постами
     */
    @Query("SELECT lp FROM LikePost lp JOIN FETCH lp.post WHERE lp.id IN (" +
            "SELECT lp2.id FROM LikePost lp2 WHERE lp2.post.id IN :postIds " +
            "AND (" +
            "  SELECT COUNT(*) FROM LikePost lp3 " +
            "  WHERE lp3.post.id = lp2.post.id AND lp3.createdAt >= lp2.createdAt" +
            ") <= :limit" +
            ") ORDER BY lp.createdAt DESC")
    List<LikePost> findRecentLikesForPostsWithPosts(@Param("postIds") List<UUID> postIds,
                                                    @Param("limit") int limit);
}