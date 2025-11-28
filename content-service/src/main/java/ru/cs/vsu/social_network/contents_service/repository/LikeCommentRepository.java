package ru.cs.vsu.social_network.contents_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.cs.vsu.social_network.contents_service.entity.LikeComment;

import java.util.Optional;
import java.util.UUID;

public interface LikeCommentRepository extends JpaRepository<LikeComment, UUID> {

    /**
     * Находит лайк комментария по идентификатору пользователя и комментария
     *
     * @param ownerId идентификатор пользователя
     * @param commentId идентификатор комментария
     * @return Optional с лайком, если найден
     */
    Optional<LikeComment> findByOwnerIdAndCommentId(UUID ownerId,
                                                    UUID commentId);

    /**
     * Проверяет существование лайка по идентификатору пользователя и комментария
     *
     * @param ownerId идентификатор пользователя
     * @param commentId идентификатор комментария
     * @return true если лайк существует
     */
    boolean existsByOwnerIdAndCommentId(UUID ownerId,
                                        UUID commentId);

    /**
     * Находит все лайки комментария с пагинацией
     *
     * @param commentId идентификатор комментария
     * @param pageable параметры пагинации
     * @return страница с лайками
     */
    Page<LikeComment> findAllByCommentId(UUID commentId,
                                         Pageable pageable);

    /**
     * Получает количество лайков комментария
     *
     * @param commentId идентификатор комментария
     * @return количество лайков
     */
    long countByCommentId(UUID commentId);
}