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
@Table(name = "like_post", indexes = {
        @Index(name = "uk_like_post_user_post", columnList = "owner_id, post_id", unique = true),

        @Index(name = "idx_like_post_post_id", columnList = "post_id"),

        @Index(name = "idx_like_post_owner_id", columnList = "owner_id"),

        @Index(name = "idx_like_post_created_at_desc", columnList = "created_at DESC"),

        @Index(name = "idx_like_post_post_created_desc", columnList = "post_id, created_at DESC")
})
public class LikePost extends BaseEntity {
    @Column(name = "owner_id")
    private UUID ownerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LikePost)) return false;
        LikePost like = (LikePost) o;
        return getId() != null && getId().equals(like.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}