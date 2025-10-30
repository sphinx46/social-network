package ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Like;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserIdAndCommentId(Long userId, Long commentId);
    Optional<Like> findByUserIdAndPostId(Long userId, Long commentId);

    Page<Like> findByPostId(Long postId, Pageable pageable);
    Page<Like> findByCommentId(Long commentId, Pageable pageable);
}
