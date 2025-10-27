package ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;

import java.util.List;
import java.util.Optional;

@Transactional
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.comments " +
            "LEFT JOIN FETCH p.likes " +
            "WHERE p.user = :user ")
    List<Post> getAllPostsByUserWithCommentsAndLikes(@Param("user") User user);

    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.comments " +
            "LEFT JOIN FETCH p.likes " +
            "WHERE p.id = :postId")
    Optional<Post> findByIdWithCommentsAndLikes(@Param("postId") Long postId);
}
