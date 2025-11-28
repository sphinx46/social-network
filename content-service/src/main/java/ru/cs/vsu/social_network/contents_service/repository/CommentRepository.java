package ru.cs.vsu.social_network.contents_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.cs.vsu.social_network.contents_service.entity.Comment;

import java.util.UUID;

/**
 * Репозиторий для работы с сущностью Comment.
 * Предоставляет методы для выполнения операций с постами в базе данных.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    /**
     * Находит все комментарии пользователя с пагинацией.
     *
     * @param ownerId идентификатор владельца постов
     * @param pageable параметры пагинации и сортировки
     * @return страница с комментариями пользователя
     */
    Page<Comment> findAllByOwnerId(UUID ownerId,
                                   Pageable pageable);

    /**
     * Находит все комментарии пользователя к определённому посту с пагинацией.
     *
     * @param ownerId идентификатор владельца постов
     * @param postId идентификатор поста
     * @param pageable параметры пагинации и сортировки
     * @return страница с комментариями пользователя к указанному посту
     */
    Page<Comment> findAllByOwnerIdAndPostId(UUID ownerId,
                                            UUID postId,
                                            Pageable pageable);
    /**
     * Находит все комментарии к определённому посту с пагинацией.
     *
     * @param postId идентификатор поста
     * @param pageable параметры пагинации и сортировки
     * @return страница с комментариями пользователей к указанному посту
     */
    Page<Comment> findAllByPostId(UUID postId,
                                  Pageable pageable);

    /**
     * Получает количество лайков поста
     *
     * @param postId идентификатор поста
     * @return количество лайков
     */
    long countByPostId(UUID postId);


    /**
     * Находит комментарии поста с пагинацией, отсортированные по дате создания (новые сначала)
     *
     * @param postId идентификатор поста
     * @param pageable параметры пагинации
     * @return страница с комментариями
     */
    Page<Comment> findByPostIdOrderByCreatedAtDesc(UUID postId,
                                                   Pageable pageable);

    /**
     * Находит комментарии пользователя с пагинацией, отсортированные по дате создания (новые сначала)
     *
     * @param ownerId идентификатор пользователя
     * @param pageable параметры пагинации
     * @return страница с комментариями
     */
    Page<Comment> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId,
                                                    Pageable pageable);

}