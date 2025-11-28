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
     * Находит лайк поста по идентификатору пользователя и поста
     *
     * @param ownerId идентификатор пользователя
     * @param postId идентификатор поста
     * @return Optional с лайком, если найден
     */
    Optional<LikePost> findByOwnerIdAndPostId(UUID ownerId, UUID postId);

    /**
     * Проверяет существование лайка по идентификатору пользователя и поста
     *
     * @param ownerId идентификатор пользователя
     * @param postId идентификатор поста
     * @return true если лайк существует
     */
    boolean existsByOwnerIdAndPostId(UUID ownerId, UUID postId);

    /**
     * Находит все лайки поста с пагинацией
     *
     * @param postId идентификатор поста
     * @param pageable параметры пагинации
     * @return страница с лайками
     */
    Page<LikePost> findAllByPostId(UUID postId, Pageable pageable);

    /**
     * Получает количество лайков поста
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
     * Получает количество лайков для списка постов в одном запросе
     *
     * @param postIds список идентификаторов постов
     * @return список кортежей [postId, count]
     */
    @Query("SELECT lp.post.id, COUNT(lp) FROM LikePost lp WHERE lp.post.id IN :postIds GROUP BY lp.post.id")
    List<Object[]> findLikesCountByPostIds(@Param("postIds") List<UUID> postIds);

    /**
     * Находит последние лайки для списка постов с лимитом на каждый пост
     *
     * @param postIds список идентификаторов постов
     * @param pageable параметры пагинации (лимит)
     * @return список лайков
     */
    @Query("SELECT lp FROM LikePost lp WHERE lp.post.id IN :postIds " +
            "AND lp.id IN (SELECT lp2.id FROM LikePost lp2 WHERE lp2.post.id = lp.post.id " +
            "ORDER BY lp2.createdAt DESC)")
    List<LikePost> findRecentLikesForPosts(@Param("postIds") List<UUID> postIds,
                                           Pageable pageable);
}