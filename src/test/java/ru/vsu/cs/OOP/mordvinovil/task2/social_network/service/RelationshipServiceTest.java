package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.AccessValidator;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.RelationshipFactory;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory.*;

@ExtendWith(MockitoExtension.class)
public class RelationshipServiceTest {

    @Mock
    private RelationshipRepository relationshipRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private RelationshipFactory relationshipFactory;

    @Mock
    private AccessValidator accessValidator;

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
        when(relationshipFactory.createPendingRelationship(currentUser, receiverUser)).thenReturn(relationship);
        when(relationshipRepository.save(any(Relationship.class))).thenReturn(relationship);
        when(entityMapper.map(relationship, RelationshipResponse.class)).thenReturn(expectedResponse);

        RelationshipResponse result = relationshipService.sendFriendRequest(request, currentUser);

        assertNotNull(result);

        verify(userRepository).findById(receiverUser.getId());
        verify(relationshipRepository).findRelationshipBetweenUsers(currentUser.getId(), receiverUser.getId());
        verify(relationshipFactory).createPendingRelationship(currentUser, receiverUser);
        verify(relationshipRepository).save(any(Relationship.class));
        verify(entityMapper).map(relationship, RelationshipResponse.class);
    }

    @Test
    void sendFriendRequest_whenReceiverUserNotFound() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        RelationshipRequest request = createTestRelationshipRequest(2L);

        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> relationshipService.sendFriendRequest(request, currentUser));

        assertEquals(ResponseMessageConstants.NOT_FOUND, exception.getMessage());
    }

    @Test
    void sendFriendRequest_whenSendingToSelf() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        RelationshipRequest request = createTestRelationshipRequest(currentUser.getId());

        when(userRepository.findById(currentUser.getId())).thenReturn(Optional.of(currentUser));

        SelfRelationshipException exception = assertThrows(SelfRelationshipException.class,
                () -> relationshipService.sendFriendRequest(request, currentUser));

        assertEquals("Нельзя отправить запрос самому себе", exception.getMessage());
    }

    @Test
    void sendFriendRequest_whenRelationshipAlreadyExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        User receiverUser = createTestUser(2L, "receiver", "123@example.com");
        RelationshipRequest request = createTestRelationshipRequest(receiverUser.getId());
        Relationship existingRelationship = createTestRelationship(currentUser, receiverUser, FriendshipStatus.PENDING);

        when(userRepository.findById(receiverUser.getId())).thenReturn(Optional.of(receiverUser));
        when(relationshipRepository.findRelationshipBetweenUsers(currentUser.getId(), receiverUser.getId())).thenReturn(Optional.of(existingRelationship));

        DuplicateRelationshipException exception = assertThrows(DuplicateRelationshipException.class,
                () -> relationshipService.sendFriendRequest(request, currentUser));

        assertEquals("Связь между пользователями уже существует", exception.getMessage());
    }

    @Test
    void acceptFriendRequest_whenRequestIsValid() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        User senderUser = createTestUser(2L, "sender", "sender@example.com");
        RelationshipRequest request = createTestRelationshipRequest(senderUser.getId());
        Relationship relationship = createTestRelationship(senderUser, currentUser, FriendshipStatus.PENDING);
        Relationship updatedRelationship = createTestRelationship(senderUser, currentUser, FriendshipStatus.ACCEPTED);
        RelationshipResponse expectedResponse = createTestRelationshipResponse(updatedRelationship);

        when(relationshipRepository.findBySenderIdAndReceiverIdAndStatus(senderUser.getId(), currentUser.getId(), FriendshipStatus.PENDING))
                .thenReturn(Optional.of(relationship));
        doNothing().when(accessValidator).validateRelationshipAccess(eq(currentUser), eq(relationship));
        doNothing().when(accessValidator).validateRelationshipStatus(eq(relationship), eq(FriendshipStatus.PENDING));
        when(relationshipRepository.save(any(Relationship.class))).thenReturn(updatedRelationship);
        when(entityMapper.map(updatedRelationship, RelationshipResponse.class)).thenReturn(expectedResponse);

        RelationshipResponse result = relationshipService.acceptFriendRequest(request, currentUser);

        assertNotNull(result);
        assertEquals(FriendshipStatus.ACCEPTED, result.getStatus());

        verify(relationshipRepository).findBySenderIdAndReceiverIdAndStatus(senderUser.getId(), currentUser.getId(), FriendshipStatus.PENDING);
        verify(accessValidator).validateRelationshipAccess(currentUser, relationship);
        verify(accessValidator).validateRelationshipStatus(relationship, FriendshipStatus.PENDING);
        verify(relationshipRepository).save(any(Relationship.class));
        verify(entityMapper).map(updatedRelationship, RelationshipResponse.class);
    }

    @Test
    void acceptFriendRequest_whenRelationshipNotFound() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        RelationshipRequest request = createTestRelationshipRequest(2L);

        when(relationshipRepository.findBySenderIdAndReceiverIdAndStatus(2L, currentUser.getId(), FriendshipStatus.PENDING))
                .thenReturn(Optional.empty());

        RelationshipNotFoundException exception = assertThrows(RelationshipNotFoundException.class,
                () -> relationshipService.acceptFriendRequest(request, currentUser));

        assertEquals(ResponseMessageConstants.NOT_FOUND, exception.getMessage());
    }

    @Test
    void acceptFriendRequest_whenUserIsNotReceiver() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        User senderUser = createTestUser(2L, "sender", "sender@example.com");
        RelationshipRequest request = createTestRelationshipRequest(senderUser.getId());
        Relationship relationship = createTestRelationship(senderUser, createTestUser(3L, "other", "other@example.com"), FriendshipStatus.PENDING);

        when(relationshipRepository.findBySenderIdAndReceiverIdAndStatus(senderUser.getId(), currentUser.getId(), FriendshipStatus.PENDING))
                .thenReturn(Optional.of(relationship));
        doThrow(new AccessDeniedException("Вы не можете изменить этот запрос"))
                .when(accessValidator).validateRelationshipAccess(eq(currentUser), eq(relationship));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> relationshipService.acceptFriendRequest(request, currentUser));

        assertEquals("Вы не можете изменить этот запрос", exception.getMessage());

        verify(accessValidator).validateRelationshipAccess(currentUser, relationship);
        verify(relationshipRepository, never()).save(any());
    }

    @Test
    void blockUser_whenRelationshipExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        User targetUser = createTestUser(2L, "target", "target@example.com");
        RelationshipRequest request = createTestRelationshipRequest(targetUser.getId());
        Relationship existingRelationship = createTestRelationship(currentUser, targetUser, FriendshipStatus.PENDING);
        Relationship updatedRelationship = createTestRelationship(currentUser, targetUser, FriendshipStatus.BLOCKED);
        RelationshipResponse expectedResponse = createTestRelationshipResponse(updatedRelationship);

        when(userRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
        when(relationshipRepository.findRelationshipBetweenUsers(currentUser.getId(), targetUser.getId()))
                .thenReturn(Optional.of(existingRelationship));
        when(relationshipRepository.save(any(Relationship.class))).thenReturn(updatedRelationship);
        when(entityMapper.map(updatedRelationship, RelationshipResponse.class)).thenReturn(expectedResponse);

        RelationshipResponse result = relationshipService.blockUser(request, currentUser);

        assertNotNull(result);
        assertEquals(FriendshipStatus.BLOCKED, result.getStatus());

        verify(relationshipRepository).findRelationshipBetweenUsers(currentUser.getId(), targetUser.getId());
        verify(relationshipRepository).save(any(Relationship.class));
        verify(entityMapper).map(updatedRelationship, RelationshipResponse.class);
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
        when(relationshipFactory.createBlockedRelationship(currentUser, targetUser)).thenReturn(newRelationship);
        when(relationshipRepository.save(any(Relationship.class))).thenReturn(newRelationship);
        when(entityMapper.map(newRelationship, RelationshipResponse.class)).thenReturn(expectedResponse);

        RelationshipResponse result = relationshipService.blockUser(request, currentUser);

        assertNotNull(result);

        verify(relationshipRepository).findRelationshipBetweenUsers(currentUser.getId(), targetUser.getId());
        verify(userRepository).findById(targetUser.getId());
        verify(relationshipFactory).createBlockedRelationship(currentUser, targetUser);
        verify(relationshipRepository).save(any(Relationship.class));
        verify(entityMapper).map(newRelationship, RelationshipResponse.class);
    }

    @Test
    void getFriendList() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        List<Relationship> relationships = List.of(
                createTestRelationship(currentUser, createTestUser(2L, "friend1", "friend1@example.com"), FriendshipStatus.ACCEPTED),
                createTestRelationship(currentUser, createTestUser(3L, "friend2", "friend2@example.com"), FriendshipStatus.ACCEPTED)
        );
        List<RelationshipResponse> expectedResponses = relationships.stream()
                .map(TestDataFactory::createTestRelationshipResponse)
                .toList();

        when(relationshipRepository.findByUserAndStatus(currentUser.getId(), FriendshipStatus.ACCEPTED))
                .thenReturn(relationships);
        when(entityMapper.mapList(relationships, RelationshipResponse.class))
                .thenReturn(expectedResponses);

        List<RelationshipResponse> result = relationshipService.getFriendList(currentUser);

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(relationshipRepository).findByUserAndStatus(currentUser.getId(), FriendshipStatus.ACCEPTED);
        verify(entityMapper).mapList(relationships, RelationshipResponse.class);
    }
}
