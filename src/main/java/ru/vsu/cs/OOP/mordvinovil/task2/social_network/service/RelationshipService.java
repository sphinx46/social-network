package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.RelationshipRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.RelationshipResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Relationship;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.FriendshipStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.DuplicateRelationshipException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.RelationshipNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.SelfRelationshipException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.UserNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.RelationshipFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.RelationshipRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.UserRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.AccessValidator;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RelationshipService {
    private final RelationshipRepository relationshipRepository;
    private final UserRepository userRepository;
    private final EntityMapper entityMapper;
    private final RelationshipFactory relationshipFactory;
    private final AccessValidator accessValidator;

    public RelationshipResponse sendFriendRequest(RelationshipRequest request, User currentUser) {
        User receiver = userRepository.findById(request.getTargetUserId())
                .orElseThrow(() -> new UserNotFoundException(ResponseMessageConstants.NOT_FOUND));

        validateSelfRelationship(currentUser, receiver);
        validateDuplicateRelationship(currentUser, receiver);

        Relationship relationship = relationshipFactory.createPendingRelationship(currentUser, receiver);
        Relationship savedRelationship = relationshipRepository.save(relationship);

        return entityMapper.map(savedRelationship, RelationshipResponse.class);
    }

    public List<RelationshipResponse> getFriendList(User currentUser) {
        return getRelationshipsByStatus(currentUser, FriendshipStatus.ACCEPTED);
    }

    public List<RelationshipResponse> getBlackList(User currentUser) {
        return getRelationshipsByStatus(currentUser, FriendshipStatus.BLOCKED);
    }

    public List<RelationshipResponse> getDeclinedList(User currentUser) {
        return getRelationshipsByStatus(currentUser, FriendshipStatus.DECLINED);
    }

    public RelationshipResponse blockUser(RelationshipRequest request, User currentUser) {
        User targetUser = userRepository.findById(request.getTargetUserId())
                .orElseThrow(() -> new UserNotFoundException(ResponseMessageConstants.NOT_FOUND));

        Relationship relationship = relationshipRepository
                .findRelationshipBetweenUsers(currentUser.getId(), targetUser.getId())
                .map(existing -> updateRelationshipStatus(existing, FriendshipStatus.BLOCKED))
                .orElseGet(() -> relationshipFactory.createBlockedRelationship(currentUser, targetUser));

        Relationship savedRelationship = relationshipRepository.save(relationship);
        return entityMapper.map(savedRelationship, RelationshipResponse.class);
    }

    public RelationshipResponse acceptFriendRequest(RelationshipRequest request, User currentUser) {
        return changeRelationshipStatus(request, FriendshipStatus.ACCEPTED, currentUser);
    }

    public RelationshipResponse declineFriendRequest(RelationshipRequest request, User currentUser) {
        return changeRelationshipStatus(request, FriendshipStatus.DECLINED, currentUser);
    }

    private List<RelationshipResponse> getRelationshipsByStatus(User currentUser, FriendshipStatus status) {
        List<Relationship> relationships = relationshipRepository.findByUserAndStatus(currentUser.getId(), status);
        return entityMapper.mapList(relationships, RelationshipResponse.class);
    }

    private RelationshipResponse changeRelationshipStatus(RelationshipRequest request, FriendshipStatus status, User currentUser) {
        Relationship relationship = relationshipRepository
                .findBySenderIdAndReceiverIdAndStatus(request.getTargetUserId(), currentUser.getId(), FriendshipStatus.PENDING)
                .orElseThrow(() -> new RelationshipNotFoundException(ResponseMessageConstants.NOT_FOUND));

        accessValidator.validateRelationshipAccess(currentUser, relationship);
        accessValidator.validateRelationshipStatus(relationship, FriendshipStatus.PENDING);

        relationship.setStatus(status);
        relationship.setUpdatedAt(LocalDateTime.now());
        Relationship savedRelationship = relationshipRepository.save(relationship);
        return entityMapper.map(savedRelationship, RelationshipResponse.class);
    }

    private void validateSelfRelationship(User currentUser, User targetUser) {
        if (currentUser.getId().equals(targetUser.getId())) {
            throw new SelfRelationshipException("Нельзя отправить запрос самому себе");
        }
    }

    private void validateDuplicateRelationship(User currentUser, User targetUser) {
        Optional<Relationship> existingRelationship = relationshipRepository
                .findRelationshipBetweenUsers(currentUser.getId(), targetUser.getId());
        if (existingRelationship.isPresent()) {
            throw new DuplicateRelationshipException("Связь между пользователями уже существует");
        }
    }

    private Relationship updateRelationshipStatus(Relationship relationship, FriendshipStatus status) {
        relationship.setStatus(status);
        relationship.setUpdatedAt(LocalDateTime.now());
        return relationship;
    }
}