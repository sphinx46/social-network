package ru.cs.vsu.social_network.contents_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "posts", indexes = {
        @Index(name = "idx_posts_owner_id", columnList = "owner_id"),
        @Index(name = "idx_posts_created_at_desc", columnList = "created_at DESC"),
        @Index(name = "idx_posts_owner_created_desc", columnList = "owner_id, created_at DESC"),
        @Index(name = "idx_posts_image_url", columnList = "image_url"),
        @Index(name = "idx_posts_updated_at", columnList = "updated_at")
})
public class Post extends BaseEntity {
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "messaging", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url")
    private String imageUrl;
}