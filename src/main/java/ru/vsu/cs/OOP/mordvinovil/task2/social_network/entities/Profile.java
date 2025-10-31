package ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "profile")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Profile extends BaseEntity {
    @Column(length = 500)
    private String bio;

    @Column(name = "profile_picture_url")
    private String imageUrl;

    @Column(name = "city")
    private String city;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;
}
