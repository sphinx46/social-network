package ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;

import java.util.Optional;

@Transactional
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.comments c " +
            "LEFT JOIN FETCH p.likes " +
            "LEFT JOIN FETCH c.likes " +
            "WHERE p.user = :user ")
    Page<Post> getAllPostsByUserWithCommentsAndLikes(@Param("user") User user, Pageable pageable);

    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.comments c " +
            "LEFT JOIN FETCH p.likes " +
            "LEFT JOIN FETCH c.likes " +
            "WHERE p.id = :postId")
    Optional<Post> findByIdWithCommentsAndLikes(@Param("postId") Long postId);
}
