package ru.cs.vsu.social_network.contents_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.cs.vsu.social_network.contents_service.entity.Comment;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностью Comment.
 * Предоставляет методы для выполнения операций с комментариями в базе данных.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    /**
     * Находит все комментарии пользователя с пагинацией.
     *
     * @param ownerId идентификатор владельца комментариев
     * @param pageable параметры пагинации и сортировки
     * @return страница с комментариями пользователя
     */
    Page<Comment> findAllByOwnerId(UUID ownerId, Pageable pageable);

    /**
     * Находит все комментарии пользователя к определённому посту с пагинацией.
     *
     * @param ownerId идентификатор владельца комментариев
     * @param postId идентификатор поста
     * @param pageable параметры пагинации и сортировки
     * @return страница с комментариями пользователя к указанному посту
     */
    @Query("SELECT c FROM Comment c WHERE c.ownerId = :ownerId AND c.post.id = :postId")
    Page<Comment> findAllByOwnerIdAndPostId(@Param("ownerId") UUID ownerId,
                                            @Param("postId") UUID postId,
                                            Pageable pageable);

    /**
     * Находит все комментарии к определённому посту с пагинацией.
     *
     * @param postId идентификатор поста
     * @param pageable параметры пагинации и сортировки
     * @return страница с комментариями пользователей к указанному посту
     */
    Page<Comment> findAllByPostId(UUID postId, Pageable pageable);

    /**
     * Получает количество комментариев поста.
     *
     * @param postId идентификатор поста
     * @return количество комментариев
     */
    long countByPostId(UUID postId);

    /**
     * Находит комментарии поста с пагинацией, отсортированные по дате создания (новые сначала).
     *
     * @param postId идентификатор поста
     * @param pageable параметры пагинации
     * @return страница с комментариями
     */
    Page<Comment> findByPostIdOrderByCreatedAtDesc(UUID postId, Pageable pageable);

    /**
     * Находит комментарии пользователя с пагинацией, отсортированные по дате создания (новые сначала).
     *
     * @param ownerId идентификатор пользователя
     * @param pageable параметры пагинации
     * @return страница с комментариями
     */
    Page<Comment> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId, Pageable pageable);

    /**
     * Получает количество комментариев для списка постов в одном запросе.
     * Устраняет проблему N+1 при подсчете комментариев для нескольких постов.
     *
     * @param postIds список идентификаторов постов
     * @return список кортежей [postId, count]
     */
    @Query("SELECT c.post.id, COUNT(c) FROM Comment c WHERE c.post.id IN :postIds GROUP BY c.post.id")
    List<Object[]> findCommentsCountByPostIds(@Param("postIds") List<UUID> postIds);

    /**
     * Находит последние комментарии для списка постов с лимитом на каждый пост.
     * Использует WINDOW FUNCTION для правильного лимитирования на каждый пост.
     *
     * @param postIds список идентификаторов постов
     * @param limit лимит комментариев на каждый пост
     * @return список комментариев с загруженными постами
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.post WHERE c.id IN (" +
            "SELECT c2.id FROM Comment c2 WHERE c2.post.id IN :postIds " +
            "AND (" +
            "  SELECT COUNT(*) FROM Comment c3 " +
            "  WHERE c3.post.id = c2.post.id AND c3.createdAt >= c2.createdAt" +
            ") <= :limit" +
            ") ORDER BY c.createdAt DESC")
    List<Comment> findRecentCommentsForPosts(@Param("postIds") List<UUID> postIds,
                                             @Param("limit") int limit);

    /**
     * Находит комментарии с предзагруженными постами для списка идентификаторов.
     * Использует JOIN FETCH для устранения проблемы N+1 при доступе к связанным постам.
     *
     * @param commentIds список идентификаторов комментариев
     * @param pageable параметры пагинации
     * @return список комментариев с загруженными постами
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.post WHERE c.id IN :commentIds ORDER BY c.createdAt DESC")
    List<Comment> findCommentsWithPosts(@Param("commentIds") List<UUID> commentIds, Pageable pageable);
}