package ru.cs.vsu.social_network.contents_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.cs.vsu.social_network.contents_service.entity.Post;

import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с сущностью Post.
 * Предоставляет методы для выполнения операций с постами в базе данных.
 */
@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    /**
     * Находит все посты пользователя с пагинацией.
     *
     * @param ownerId идентификатор владельца постов
     * @param pageable параметры пагинации и сортировки
     * @return страница с постами пользователя
     */
    Page<Post> findAllByOwnerId(UUID ownerId, Pageable pageable);

    /**
     * Находит идентификаторы постов пользователя.
     *
     * @param ownerId идентификатор владельца постов
     * @return список идентификаторов постов
     */
    @Query("SELECT p.id FROM Post p WHERE p.ownerId = :ownerId")
    List<UUID> findPostIdsByOwnerId(@Param("ownerId") UUID ownerId);

    /**
     * Получает количество постов для списка пользователей в одном запросе.
     *
     * @param ownerIds список идентификаторов владельцев
     * @return список кортежей [ownerId, count]
     */
    @Query("SELECT p.ownerId, COUNT(p) FROM Post p WHERE p.ownerId IN :ownerIds GROUP BY p.ownerId")
    List<Object[]> countPostsByOwnerIds(@Param("ownerIds") List<UUID> ownerIds);

    /**
     * Находит последние посты для списка пользователей с пагинацией.
     *
     * @param ownerIds список идентификаторов владельцев
     * @param pageable параметры пагинации
     * @return список постов
     */
    @Query("SELECT p FROM Post p WHERE p.ownerId IN :ownerIds ORDER BY p.createdAt DESC")
    List<Post> findRecentPostsByOwnerIds(@Param("ownerIds") List<UUID> ownerIds, Pageable pageable);
}