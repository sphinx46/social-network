package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.RelationshipRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.RelationshipResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Relationship;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.FriendshipStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.EventPublisherService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.RelationshipNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.UserNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.RelationshipRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.UserRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.RelationshipFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.RelationshipValidator;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelationshipService {
    private final RelationshipRepository relationshipRepository;
    private final UserRepository userRepository;
    private final EntityMapper entityMapper;
    private final RelationshipFactory relationshipFactory;
    private final RelationshipValidator relationshipValidator;
    private final EventPublisherService eventPublisherService;

    public RelationshipResponse sendFriendRequest(RelationshipRequest request, User currentUser) {
        relationshipValidator.validate(request, currentUser);

        User receiver = userRepository.findById(request.getTargetUserId())
                .orElseThrow(() -> new UserNotFoundException(ResponseMessageConstants.NOT_FOUND));

        Relationship relationship = relationshipFactory.createPendingRelationship(currentUser, receiver);
        Relationship savedRelationship = relationshipRepository.save(relationship);

        eventPublisherService.publishFriendRequest(this, request.getTargetUserId(), currentUser.getId());

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
        relationshipValidator.validateBlockUser(request, currentUser);

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
        relationshipValidator.validateStatusChange(request, currentUser);

        eventPublisherService.publishFriendRequestAccepted(this, request.getTargetUserId(), currentUser.getId());

        return changeRelationshipStatus(request, FriendshipStatus.ACCEPTED, currentUser);
    }

    public RelationshipResponse declineFriendRequest(RelationshipRequest request, User currentUser) {
        relationshipValidator.validateStatusChange(request, currentUser);

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

        relationship.setStatus(status);
        relationship.setUpdatedAt(LocalDateTime.now());
        Relationship savedRelationship = relationshipRepository.save(relationship);
        return entityMapper.map(savedRelationship, RelationshipResponse.class);
    }

    private Relationship updateRelationshipStatus(Relationship relationship, FriendshipStatus status) {
        relationship.setStatus(status);
        relationship.setUpdatedAt(LocalDateTime.now());
        return relationship;
    }
}