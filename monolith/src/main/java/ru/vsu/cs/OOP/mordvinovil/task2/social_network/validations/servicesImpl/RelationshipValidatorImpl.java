package ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.servicesImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.relationship.RelationshipRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.FriendshipStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.relationship.DuplicateRelationshipException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.relationship.RelationshipNoPendingRequestsException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.relationship.RelationshipToSelfException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.user.UserNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.RelationshipRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.UserRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.RelationshipValidator;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RelationshipValidatorImpl implements RelationshipValidator {
    private final UserRepository userRepository;
    private final RelationshipRepository relationshipRepository;

    @Override
    public void validate(RelationshipRequest request, User currentUser) {
        validateFriendRequest(request, currentUser);
    }

    @Override
    public void validateFriendRequest(RelationshipRequest request, User currentUser) {
        User targetUser = userRepository.findById(request.getTargetUserId())
                .orElseThrow(() -> new UserNotFoundException(ResponseMessageConstants.FAILURE_USER_NOT_FOUND));

        validateNotSelfOperation(currentUser, targetUser);
        validateNoExistingRelationship(currentUser, targetUser);
    }

    @Override
    public void validateBlockUser(RelationshipRequest request, User currentUser) {
        User targetUser = userRepository.findById(request.getTargetUserId())
                .orElseThrow(() -> new UserNotFoundException(ResponseMessageConstants.FAILURE_USER_NOT_FOUND));

        validateNotSelfOperation(currentUser, targetUser);
    }

    @Override
    public void validateStatusChange(RelationshipRequest request, User currentUser) {
        User targetUser = userRepository.findById(request.getTargetUserId())
                .orElseThrow(() -> new UserNotFoundException(ResponseMessageConstants.FAILURE_USER_NOT_FOUND));

        validateNotSelfOperation(currentUser, targetUser);

        boolean hasPendingRequest = relationshipRepository
                .findBySenderIdAndReceiverIdAndStatus(targetUser.getId(), currentUser.getId(), FriendshipStatus.PENDING)
                .isPresent();

        if (!hasPendingRequest) {
            throw new RelationshipNoPendingRequestsException(ResponseMessageConstants.FAILURE_RELATIONSHIP_PENDING_REQUESTS_NOT_FOUND);
        }
    }

    private void validateNotSelfOperation(User currentUser, User targetUser) {
        if (currentUser.getId().equals(targetUser.getId())) {
            throw new RelationshipToSelfException(ResponseMessageConstants.FAILURE_RELATIONSHIP_TO_SELF_OPERATION );
        }
    }

    private void validateNoExistingRelationship(User currentUser, User targetUser) {
        Optional<?> existingRelationship = relationshipRepository
                .findRelationshipBetweenUsers(currentUser.getId(), targetUser.getId());

        if (existingRelationship.isPresent()) {
            throw new DuplicateRelationshipException(ResponseMessageConstants.FAILURE_RELATIONSHIP_ALREADY_EXISTS );
        }
    }
}