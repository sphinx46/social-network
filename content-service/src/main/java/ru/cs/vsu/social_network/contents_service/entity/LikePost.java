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
@Table(name = "like")
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

