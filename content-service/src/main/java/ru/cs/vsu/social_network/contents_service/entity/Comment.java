package ru.cs.vsu.social_network.contents_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "comments", indexes = {
        @Index(name = "idx_comments_post_id", columnList = "post_id"),

        @Index(name = "idx_comments_owner_id", columnList = "owner_id"),

        @Index(name = "idx_comments_post_created_desc", columnList = "post_id, created_at DESC"),

        @Index(name = "idx_comments_owner_created_desc", columnList = "owner_id, created_at DESC"),

        @Index(name = "idx_comments_image_url", columnList = "image_url")
})
public class Comment extends BaseEntity {
    @Column(name = "owner_id")
    private UUID ownerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "messaging", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comment)) return false;
        Comment comment = (Comment) o;
        return getId() != null && getId().equals(comment.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}