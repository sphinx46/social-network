package ru.cs.vsu.social_network.contents_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.cs.vsu.social_network.contents_service.entity.LikePost;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LikePostRepository extends JpaRepository<LikePost, UUID> {

    /**
     * Находит лайк поста по идентификатору пользователя и поста
     *
     * @param ownerId идентификатор пользователя
     * @param postId идентификатор поста
     * @return Optional с лайком, если найден
     */
    Optional<LikePost> findByOwnerIdAndPostId(UUID ownerId,
                                              UUID postId);

    /**
     * Проверяет существование лайка по идентификатору пользователя и поста
     *
     * @param ownerId идентификатор пользователя
     * @param postId идентификатор поста
     * @return true если лайк существует
     */
    boolean existsByOwnerIdAndPostId(UUID ownerId,
                                     UUID postId);

    /**
     * Находит все лайки поста с пагинацией
     *
     * @param postId идентификатор поста
     * @param pageable параметры пагинации
     * @return страница с лайками
     */
    Page<LikePost> findAllByPostId(UUID postId,
                                   Pageable pageable);

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
    List<LikePost> findByPostIdOrderByCreatedAtDesc(UUID postId,
                                                    Pageable pageable);
}