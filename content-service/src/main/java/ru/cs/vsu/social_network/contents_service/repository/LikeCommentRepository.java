package ru.cs.vsu.social_network.contents_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.cs.vsu.social_network.contents_service.entity.LikeComment;

import java.util.UUID;

public interface LikeCommentRepository extends JpaRepository<LikeComment, UUID> {
}
