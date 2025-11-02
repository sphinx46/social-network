package ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Like;

import java.util.Optional;
import java.util.Set;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserIdAndCommentId(Long userId, Long commentId);

    Optional<Like> findByUserIdAndPostId(Long userId, Long commentId);

    @Query("SELECT l FROM Like l " +
            "LEFT JOIN FETCH l.user " +
            "WHERE l.post.id = :postId")
    Page<Like> findByPostId(@Param("postId") Long postId,
                            Pageable pageable);

    @Query("SELECT l FROM Like l " +
            "LEFT JOIN FETCH l.user " +
            "WHERE l.comment.id = :commentId")
    Page<Like> findByCommentId(@Param("commentId") Long commentId,
                               Pageable pageable);


    @Query("SELECT l FROM Like l JOIN l.post p " +
            "WHERE (p.user.id = :userId AND l.user.id = :targetUserId) " +
            "OR (p.user.id = :targetUserId AND l.user.id = :userId)")
    Set<Like> getMutualLikes(@Param("userId") Long userId,
                             @Param("targetUserId") Long targetUserId);
}
