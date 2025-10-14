package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.RelationshipRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.RelationshipResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Relationship;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Role;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RelationshipServiceTest {
    @Mock
    private RelationshipRepository relationshipRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private RelationshipService relationshipService;

    @Test
    void sendFriendRequest_whenRequestIsValid() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        User receiverUser = createTestUser(2L, "receiver", "123@example.com");

        RelationshipRequest request = createTestRelationshipRequest(receiverUser.getId());
        Relationship relationship = createTestRelationship(currentUser, receiverUser, FriendshipStatus.PENDING);
        RelationshipResponse expectedResponse = createTestRelationshipResponse(relationship);

        when(userRepository.findById(receiverUser.getId())).thenReturn(Optional.of(receiverUser));
        when(relationshipRepository.findRelationshipBetweenUsers(currentUser.getId(), receiverUser.getId())).thenReturn(Optional.empty());
        when(relationshipRepository.save(any(Relationship.class))).thenReturn(relationship);
        when(modelMapper.map(relationship, RelationshipResponse.class)).thenReturn(expectedResponse);

        RelationshipResponse result = relationshipService.sendFriendRequest(request, currentUser);

        assertNotNull(result);

        verify(userRepository).findById(receiverUser.getId());
        verify(relationshipRepository).findRelationshipBetweenUsers(currentUser.getId(), receiverUser.getId());
        verify(relationshipRepository).save(any(Relationship.class));
        verify(modelMapper).map(relationship, RelationshipResponse.class);
    }

    @Test
    void sendFriendRequest_whenUserIsNotExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        RelationshipRequest request = createTestRelationshipRequest(2L);

        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class,
                () -> relationshipService.sendFriendRequest(request, currentUser));

        assertEquals(ResponseMessageConstants.NOT_FOUND, userNotFoundException.getMessage());
    }

    @Test
    void sendFriendRequest_whenMessageForSelf() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        RelationshipRequest request = createTestRelationshipRequest(currentUser.getId());

        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));

        SelfRelationshipException selfRelationshipException = assertThrows(SelfRelationshipException.class,
                () -> relationshipService.sendFriendRequest(request, currentUser));

        assertEquals("Нельзя отправить запрос самому себе", selfRelationshipException.getMessage());
    }

    @Test
    void sendFriendRequest_whenRelationshipAlreadyExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        User receiverUser = createTestUser(2L, "receiver", "123@example.com");
        RelationshipRequest request = createTestRelationshipRequest(receiverUser.getId());
        Relationship existingRelationship = createTestRelationship(currentUser, receiverUser, FriendshipStatus.PENDING);

        when(userRepository.findById(receiverUser.getId())).thenReturn(Optional.of(receiverUser));
        when(relationshipRepository.findRelationshipBetweenUsers(currentUser.getId(), receiverUser.getId())).thenReturn(Optional.of(existingRelationship));

        DuplicateRelationshipException duplicateRelationshipException = assertThrows(DuplicateRelationshipException.class,
                () -> relationshipService.sendFriendRequest(request, currentUser));

        assertEquals("Связь между пользователями уже существует", duplicateRelationshipException.getMessage());
    }

    @Test
    void acceptFriendRequest_whenRequestIsValid() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        User senderUser = createTestUser(2L, "sender", "sender@example.com");
        RelationshipRequest request = createTestRelationshipRequest(senderUser.getId());
        Relationship relationship = createTestRelationship(senderUser, currentUser, FriendshipStatus.PENDING);
        RelationshipResponse expectedResponse = createTestRelationshipResponse(relationship);
        expectedResponse.setStatus(FriendshipStatus.ACCEPTED);

        when(relationshipRepository.findBySenderIdAndReceiverIdAndStatus(senderUser.getId(), currentUser.getId(), FriendshipStatus.PENDING))
                .thenReturn(Optional.of(relationship));
        when(relationshipRepository.save(any(Relationship.class))).thenReturn(relationship);
        when(modelMapper.map(relationship, RelationshipResponse.class)).thenReturn(expectedResponse);

        RelationshipResponse result = relationshipService.acceptFriendRequest(request, currentUser);

        assertNotNull(result);
        assertEquals(FriendshipStatus.ACCEPTED, result.getStatus());

        verify(relationshipRepository).findBySenderIdAndReceiverIdAndStatus(senderUser.getId(), currentUser.getId(), FriendshipStatus.PENDING);
        verify(relationshipRepository).save(any(Relationship.class));
        verify(modelMapper).map(relationship, RelationshipResponse.class);
    }

    @Test
    void acceptFriendRequest_whenRelationshipNotExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        RelationshipRequest request = createTestRelationshipRequest(2L);

        when(relationshipRepository.findBySenderIdAndReceiverIdAndStatus(2L, currentUser.getId(), FriendshipStatus.PENDING))
                .thenReturn(Optional.empty());

        RelationshipNotFoundException relationshipNotFoundException = assertThrows(RelationshipNotFoundException.class,
                () -> relationshipService.acceptFriendRequest(request, currentUser));

        assertEquals(ResponseMessageConstants.NOT_FOUND, relationshipNotFoundException.getMessage());
    }

    @Test
    void acceptFriendRequest_whenUserIsNotReceiver() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        User senderUser = createTestUser(2L, "sender", "sender@example.com");
        User otherUser = createTestUser(3L, "other", "other@example.com");
        RelationshipRequest request = createTestRelationshipRequest(senderUser.getId());
        Relationship relationship = createTestRelationship(senderUser, otherUser, FriendshipStatus.PENDING);

        when(relationshipRepository.findBySenderIdAndReceiverIdAndStatus(senderUser.getId(), currentUser.getId(), FriendshipStatus.PENDING))
                .thenReturn(Optional.of(relationship));

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> relationshipService.acceptFriendRequest(request, currentUser));

        assertEquals("Вы не можете изменить этот запрос", accessDeniedException.getMessage());
    }

    @Test
    void blockUser_whenRelationshipExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        User targetUser = createTestUser(2L, "target", "target@example.com");
        RelationshipRequest request = createTestRelationshipRequest(targetUser.getId());
        Relationship existingRelationship = createTestRelationship(currentUser, targetUser, FriendshipStatus.PENDING);
        RelationshipResponse expectedResponse = createTestRelationshipResponse(existingRelationship);
        expectedResponse.setStatus(FriendshipStatus.BLOCKED);

        when(relationshipRepository.findRelationshipBetweenUsers(currentUser.getId(), targetUser.getId()))
                .thenReturn(Optional.of(existingRelationship));
        when(relationshipRepository.save(any(Relationship.class))).thenReturn(existingRelationship);
        when(modelMapper.map(existingRelationship, RelationshipResponse.class)).thenReturn(expectedResponse);

        RelationshipResponse result = relationshipService.blockUser(request, currentUser);

        assertNotNull(result);
        assertEquals(FriendshipStatus.BLOCKED, result.getStatus());

        verify(relationshipRepository).findRelationshipBetweenUsers(currentUser.getId(), targetUser.getId());
        verify(relationshipRepository).save(any(Relationship.class));
        verify(modelMapper).map(existingRelationship, RelationshipResponse.class);
    }

    @Test
    void blockUser_whenRelationshipNotExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        User targetUser = createTestUser(2L, "target", "target@example.com");
        RelationshipRequest request = createTestRelationshipRequest(targetUser.getId());
        Relationship newRelationship = createTestRelationship(currentUser, targetUser, FriendshipStatus.BLOCKED);
        RelationshipResponse expectedResponse = createTestRelationshipResponse(newRelationship);

        when(relationshipRepository.findRelationshipBetweenUsers(currentUser.getId(), targetUser.getId()))
                .thenReturn(Optional.empty());
        when(userRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
        when(relationshipRepository.save(any(Relationship.class))).thenReturn(newRelationship);
        when(modelMapper.map(newRelationship, RelationshipResponse.class)).thenReturn(expectedResponse);

        RelationshipResponse result = relationshipService.blockUser(request, currentUser);

        assertNotNull(result);

        verify(relationshipRepository).findRelationshipBetweenUsers(currentUser.getId(), targetUser.getId());
        verify(userRepository).findById(targetUser.getId());
        verify(relationshipRepository).save(any(Relationship.class));
        verify(modelMapper).map(newRelationship, RelationshipResponse.class);
    }

    @Test
    void getFriendList() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        List<Relationship> relationships = List.of(
                createTestRelationship(currentUser, createTestUser(2L, "friend1", "friend1@example.com"), FriendshipStatus.ACCEPTED),
                createTestRelationship(currentUser, createTestUser(3L, "friend2", "friend2@example.com"), FriendshipStatus.ACCEPTED)
        );
        List<RelationshipResponse> expectedResponses = relationships.stream()
                .map(this::createTestRelationshipResponse)
                .toList();

        when(relationshipRepository.findByUserAndStatus(currentUser.getId(), FriendshipStatus.ACCEPTED)).thenReturn(relationships);
        when(modelMapper.map(any(Relationship.class), eq(RelationshipResponse.class)))
                .thenReturn(expectedResponses.get(0), expectedResponses.get(1));

        List<RelationshipResponse> result = relationshipService.getFriendList(currentUser);

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(relationshipRepository).findByUserAndStatus(currentUser.getId(), FriendshipStatus.ACCEPTED);
        verify(modelMapper, times(2)).map(any(Relationship.class), eq(RelationshipResponse.class));
    }

    private User createTestUser(Long id, String username, String email) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("password");
        user.setCity("Moscow");
        user.setRole(Role.ROLE_USER);
        user.setCreatedAt(LocalDateTime.now());
        user.setOnline(false);
        return user;
    }

    private RelationshipRequest createTestRelationshipRequest(Long targetUserId) {
        RelationshipRequest request = new RelationshipRequest();
        request.setTargetUserId(targetUserId);
        return request;
    }

    private Relationship createTestRelationship(User sender, User receiver, FriendshipStatus status) {
        return Relationship.builder()
                .sender(sender)
                .receiver(receiver)
                .status(status)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private RelationshipResponse createTestRelationshipResponse(Relationship relationship) {
        RelationshipResponse response = new RelationshipResponse();
        response.setId(relationship.getId());
        response.setSenderId(relationship.getSender().getId());
        response.setReceiverId(relationship.getReceiver().getId());
        response.setStatus(relationship.getStatus());
        response.setCreatedAt(relationship.getCreatedAt());
        response.setUpdatedAt(relationship.getUpdatedAt());
        return response;
    }
}