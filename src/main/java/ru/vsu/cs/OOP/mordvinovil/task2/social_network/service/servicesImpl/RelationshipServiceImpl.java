package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.RelationshipRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.RelationshipResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Relationship;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.FriendshipStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.notification.NotificationEventPublisherService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.relationship.RelationshipNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.RelationshipRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.RelationshipService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.RelationshipFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.RelationshipValidator;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RelationshipServiceImpl  implements RelationshipService {
    private final RelationshipRepository relationshipRepository;
    private final EntityUtils entityUtils;
    private final EntityMapper entityMapper;
    private final RelationshipFactory relationshipFactory;
    private final RelationshipValidator relationshipValidator;
    private final NotificationEventPublisherService notificationEventPublisherService;

    public RelationshipResponse sendFriendRequest(RelationshipRequest request, User currentUser) {
        relationshipValidator.validate(request, currentUser);

        User receiver = entityUtils.getUser(request.getTargetUserId());

        Relationship relationship = relationshipFactory.createPendingRelationship(currentUser, receiver);
        Relationship savedRelationship = relationshipRepository.save(relationship);

        notificationEventPublisherService.publishFriendRequest(this, request.getTargetUserId(), currentUser.getId());

        return entityMapper.map(savedRelationship, RelationshipResponse.class);
    }

    public PageResponse<RelationshipResponse> getFriendList(User currentUser, PageRequest pageRequest) {
        return getRelationshipsByStatus(currentUser, FriendshipStatus.ACCEPTED, pageRequest);
    }

    public PageResponse<RelationshipResponse> getBlackList(User currentUser, PageRequest pageRequest) {
        return getRelationshipsByStatus(currentUser, FriendshipStatus.BLOCKED, pageRequest);
    }

    public PageResponse<RelationshipResponse> getDeclinedList(User currentUser, PageRequest pageRequest) {
        return getRelationshipsByStatus(currentUser, FriendshipStatus.DECLINED, pageRequest);
    }

    public RelationshipResponse blockUser(RelationshipRequest request, User currentUser) {
        relationshipValidator.validateBlockUser(request, currentUser);

        User targetUser = entityUtils.getUser(request.getTargetUserId());

        Relationship relationship = relationshipRepository
                .findRelationshipBetweenUsers(currentUser.getId(), targetUser.getId())
                .map(existing -> updateRelationshipStatus(existing, FriendshipStatus.BLOCKED))
                .orElseGet(() -> relationshipFactory.createBlockedRelationship(currentUser, targetUser));

        Relationship savedRelationship = relationshipRepository.save(relationship);
        return entityMapper.map(savedRelationship, RelationshipResponse.class);
    }

    public RelationshipResponse acceptFriendRequest(RelationshipRequest request, User currentUser) {
        relationshipValidator.validateStatusChange(request, currentUser);

        notificationEventPublisherService.publishFriendRequestAccepted(this, request.getTargetUserId(), currentUser.getId());

        return changeRelationshipStatus(request, FriendshipStatus.ACCEPTED, currentUser);
    }

    public RelationshipResponse declineFriendRequest(RelationshipRequest request, User currentUser) {
        relationshipValidator.validateStatusChange(request, currentUser);

        return changeRelationshipStatus(request, FriendshipStatus.DECLINED, currentUser);
    }

    private PageResponse<RelationshipResponse> getRelationshipsByStatus(User currentUser, FriendshipStatus status, PageRequest pageRequest) {
        Page<Relationship> relationships = relationshipRepository.findByUserAndStatus(currentUser.getId(), status, pageRequest.toPageable());
        return PageResponse.of(relationships.map(
                relationship -> entityMapper.map(relationship, RelationshipResponse.class)
        ));
    }

    private RelationshipResponse changeRelationshipStatus(RelationshipRequest request, FriendshipStatus status, User currentUser) {
        Relationship relationship = relationshipRepository
                .findBySenderIdAndReceiverIdAndStatus(request.getTargetUserId(), currentUser.getId(), FriendshipStatus.PENDING)
                .orElseThrow(() -> new RelationshipNotFoundException(ResponseMessageConstants.FAILURE_RELATIONSHIP_NOT_FOUND));

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

