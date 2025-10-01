package ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Like;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    List<Like> findByPostId(Long postId);
    List<Like> findByCommentId(Long commentId);
    Optional<Like> findByUserIdAndCommentId(Long userId, Long commentId);
    Optional<Like> findByUserIdAndPostId(Long userId, Long commentId);
}
