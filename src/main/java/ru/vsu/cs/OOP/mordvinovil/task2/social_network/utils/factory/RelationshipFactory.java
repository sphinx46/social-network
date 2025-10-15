package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory;

import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Relationship;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.FriendshipStatus;

import java.time.LocalDateTime;

@Component
public class RelationshipFactory {

    public Relationship createPendingRelationship(User sender, User receiver) {
        return Relationship.builder()
                .sender(sender)
                .receiver(receiver)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status(FriendshipStatus.PENDING)
                .build();
    }

    public Relationship createBlockedRelationship(User sender, User receiver) {
        return Relationship.builder()
                .sender(sender)
                .receiver(receiver)
                .status(FriendshipStatus.BLOCKED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}