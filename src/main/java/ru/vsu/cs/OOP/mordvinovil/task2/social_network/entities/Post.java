package ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "posts")
public class Post extends BaseEntity {

    @ManyToOne(optional = false, targetEntity = User.class)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    private String imageUrl;

    @Column(name = "time_added", nullable = false)
    private LocalDate time;
}
