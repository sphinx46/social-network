package ru.cs.vsu.social_network.contents_service.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "posts")
public class Post extends BaseEntity {
    @Column(name = "owner_id",  nullable = false)
    private UUID ownerId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

//    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @BatchSize(size = 10)
//    private Set<Comment> comments = new HashSet<>();
//
//    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    @BatchSize(size = 10)
//    private Set<Like> likes = new HashSet<>();
}
