package ru.cs.vsu.social_network.messaging_service.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.cs.vsu.social_network.messaging_service.entity.enums.MessageStatus;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "messages", indexes = {
        @Index(name = "idx_messages_conversation_created",
                columnList = "conversation_id, created_at DESC"),
        @Index(name = "idx_messages_sender_conversation",
                columnList = "sender_id, conversation_id, created_at DESC"),
        @Index(name = "idx_messages_receiver_conversation",
                columnList = "receiver_id, conversation_id, created_at DESC"),
        @Index(name = "idx_messages_status_receiver",
                columnList = "status, receiver_id, created_at"),
        @Index(name = "idx_messages_created",
                columnList = "created_at"),
        @Index(name = "idx_messages_sender",
                columnList = "sender_id, created_at DESC"),
        @Index(name = "idx_messages_receiver",
                columnList = "receiver_id, created_at DESC")
})
public class Message extends BaseEntity {

    @Column(name = "sender_id", nullable = false, updatable = false)
    private UUID senderId;

    @Column(name = "receiver_id", nullable = false, updatable = false)
    private UUID receiverId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(name = "messaging", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MessageStatus status;
}