package ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.MessageStatus;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "messages")
public class Message extends BaseEntity {
    @ManyToOne(optional = false, targetEntity = User.class)
    @JoinColumn(name = "sender_id", referencedColumnName = "id")
    private User sender;

    @ManyToOne(optional = false, targetEntity = User.class)
    @JoinColumn(name = "receiver_id", referencedColumnName = "id")
    private User receiver;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "status", nullable = false)
    private MessageStatus status;

    @Column(name = "time_create", nullable = false)
    private LocalDate createdAt;

    @Column(name = "time_update", nullable = false)
    private LocalDate updatedAt;
}
