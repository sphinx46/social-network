package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.RelationshipRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.RelationshipResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Relationship;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.FriendshipStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.custom.AccessDeniedException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.DuplicateRelationshipException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.RelationshipNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.SelfRelationshipException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.UserNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.RelationshipRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.UserRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RelationshipService {
    private final RelationshipRepository relationShipRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public RelationshipResponse sendFriendRequest(RelationshipRequest request, User currentUser) {
        User receiver = userRepository.findById(request.getUserReceiverId())
                .orElseThrow(() -> new UserNotFoundException(ResponseMessageConstants.NOT_FOUND));

        if (currentUser.getId().equals(request.getUserReceiverId())) {
            throw new SelfRelationshipException("Нельзя отправить запрос самому себе");
        }

        Optional<Relationship> existingRelationship = relationShipRepository
                .findRelationshipBetweenUsers(currentUser.getId(), request.getUserReceiverId());

        if (existingRelationship.isPresent()) {
            throw new DuplicateRelationshipException("Связь между пользователями уже существует");
        }

        var relationship = Relationship.builder()
                .sender(currentUser)
                .receiver(receiver)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .status(FriendshipStatus.PENDING)
                .build();

        Relationship savedRelationship = relationShipRepository.save(relationship);
        return modelMapper.map(savedRelationship, RelationshipResponse.class);
    }

    private List<RelationshipResponse> getRequestsByStatus(User currentUser, FriendshipStatus status) {
        List<Relationship> relationships = relationShipRepository.findByUserAndStatus(currentUser.getId(), status);
        return relationships.stream()
                .map(relationship -> modelMapper.map(relationship, RelationshipResponse.class))
                .toList();
    }

    public List<RelationshipResponse> getFriendList(User currentUser) {
        return this.getRequestsByStatus(currentUser, FriendshipStatus.ACCEPTED);
    }

    public List<RelationshipResponse> getBlackList(User currentUser) {
        return this.getRequestsByStatus(currentUser, FriendshipStatus.BLOCKED);
    }

    public List<RelationshipResponse> getDeclinedList(User currentUser) {
        return this.getRequestsByStatus(currentUser, FriendshipStatus.DECLINED);
    }


    private RelationshipResponse changeStatusAndSave(RelationshipRequest request, FriendshipStatus toStatus, User currentUser) {
        Relationship relationship = relationShipRepository
                .findBySenderIdAndReceiverIdAndStatus(request.getUserReceiverId(), currentUser.getId(), FriendshipStatus.PENDING)
                .orElseThrow(() -> new RelationshipNotFoundException(ResponseMessageConstants.NOT_FOUND));

        if (!relationship.getReceiver().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Вы не можете изменить этот запрос");
        }

        relationship.setStatus(toStatus);
        relationship.setUpdatedAt(LocalDate.now());

        Relationship savedRelationship = relationShipRepository.save(relationship);
        return modelMapper.map(savedRelationship, RelationshipResponse.class);
    }

    public RelationshipResponse blockUser(RelationshipRequest request, User currentUser) {
        Optional<Relationship> existing = relationShipRepository
                .findRelationshipBetweenUsers(currentUser.getId(), request.getUserReceiverId());

        Relationship relationship;
        if (existing.isPresent()) {
            relationship = existing.get();
            relationship.setStatus(FriendshipStatus.BLOCKED);
        } else {
            relationship = Relationship.builder()
                    .sender(currentUser)
                    .receiver(userRepository.findById(request.getUserReceiverId())
                            .orElseThrow(() -> new UserNotFoundException("Пользователь не найден")))
                    .status(FriendshipStatus.BLOCKED)
                    .createdAt(LocalDate.now())
                    .updatedAt(LocalDate.now())
                    .build();
        }
        return modelMapper.map(relationShipRepository.save(relationship), RelationshipResponse.class);
    }

    public RelationshipResponse acceptFriendRequest(RelationshipRequest request, User currentUser) {
        return this.changeStatusAndSave(request, FriendshipStatus.ACCEPTED, currentUser);
    }

    public RelationshipResponse declineFriendRequest(RelationshipRequest request, User currentUser) {
        return this.changeStatusAndSave(request, FriendshipStatus.DECLINED, currentUser);
    }
}
