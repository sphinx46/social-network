package ru.cs.vsu.social_network.messaging_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "conversations", indexes = {
        @Index(name = "idx_conversations_users",
                columnList = "user1_id, user2_id"),

        @Index(name = "idx_conversations_user1",
                columnList = "user1_id"),

        @Index(name = "idx_conversations_user2",
                columnList = "user2_id"),
})
public class Conversation extends BaseEntity {
    @Column(name = "user1_id", nullable = false, updatable = false)
    private UUID user1Id;

    @Column(name = "user2_id", nullable = false, updatable = false)
    private UUID user2Id;

    @OneToMany(mappedBy = "conversation", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Message> messages = new HashSet<>();

    @Column(name = "last_message_id")
    private UUID lastMessageId;

    @Column(name = "message_count", nullable = false)
    @Builder.Default
    private Long messageCount = 0L;
}