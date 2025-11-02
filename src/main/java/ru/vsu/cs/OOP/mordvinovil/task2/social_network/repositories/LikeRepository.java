package ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Like;

import java.util.Optional;

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


    @Query("SELECT COUNT(DISTINCT l1.post) FROM Like l1 " +
            "WHERE l1.user.id = :user1 " +
            "AND l1.post IS NOT NULL " +
            "AND EXISTS (" +
            "    SELECT 1 FROM Like l2 " +
            "    WHERE l2.user.id = :user2 " +
            "    AND l2.post.id = l1.post.id " +
            "    AND l2.post IS NOT NULL" +
            ")")
    int countCommonLikes(@Param("user1") Long user1, @Param("user2") Long user2);
}
