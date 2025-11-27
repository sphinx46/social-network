package ru.cs.vsu.social_network.contents_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.cs.vsu.social_network.contents_service.entity.LikePost;

import java.util.UUID;

public interface LikePostRepository extends JpaRepository<LikePost, UUID> {
}
