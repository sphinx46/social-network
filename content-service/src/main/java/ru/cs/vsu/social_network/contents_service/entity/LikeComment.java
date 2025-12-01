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
@Table(name = "like_comment", indexes = {
        @Index(name = "uk_like_comment_user_comment", columnList = "owner_id, comment_id", unique = true),

        @Index(name = "idx_like_comment_comment_id", columnList = "comment_id"),

        @Index(name = "idx_like_comment_owner_id", columnList = "owner_id"),

        @Index(name = "idx_like_comment_created_at_desc", columnList = "created_at DESC"),

        @Index(name = "idx_like_comment_comment_created_desc", columnList = "comment_id, created_at DESC")
})
public class LikeComment extends BaseEntity {
    @Column(name = "owner_id")
    private UUID ownerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LikeComment)) return false;
        LikeComment like = (LikeComment) o;
        return getId() != null && getId().equals(like.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}