package ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities;


import jakarta.persistence.*;
import lombok.*;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.FriendshipStatus;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "relation_ship")
public class Relationship extends BaseEntity {

    @ManyToOne(optional = false, targetEntity = User.class)
    @JoinColumn(name = "user_sender_id", referencedColumnName = "id" )
    private User sender;

    @ManyToOne(optional = false, targetEntity = User.class)
    @JoinColumn(name = "user_receiver_id", referencedColumnName = "id" )
    private User receiver;

    @Column(name = "status")
    private FriendshipStatus status;

    @Column(name = "time_added", nullable = false)
    private LocalDate createdAt;

    @Column(name = "time_updated", nullable = false)
    private LocalDate updatedAt;
}
