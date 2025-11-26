package ru.cs.vsu.social_network.contents_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.cs.vsu.social_network.contents_service.entity.Post;

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
}