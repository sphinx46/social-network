package ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Comment;

import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c " +
            "LEFT JOIN FETCH c.creator " +
            "LEFT JOIN FETCH c.likes cl " +
            "LEFT JOIN FETCH cl.user " +
            "WHERE c.post.id = :postId")
    Page<Comment> findByPostIdWithLikes(@Param("postId") Long postId, Pageable pageable);

    @Query("SELECT c FROM Comment c " +
            "LEFT JOIN FETCH c.creator " +
            "LEFT JOIN FETCH c.likes cl " +
            "LEFT JOIN FETCH cl.user " +
            "WHERE c.id = :commentId")
    @Transactional(readOnly = true)
    Optional<Comment> findByIdWithLikes(@Param("commentId") Long commentId);


    @Query("SELECT COUNT(DISTINCT c1.post.id) FROM Comment c1 " +
            "WHERE c1.creator.id = :user1 " +
            "AND EXISTS (" +
            "    SELECT 1 FROM Comment c2 " +
            "    WHERE c2.creator.id = :user2 " +
            "    AND c2.post.id = c1.post.id" +
            ")")
    int countCommonComments(@Param("user1") Long user1, @Param("user2") Long user2);
}