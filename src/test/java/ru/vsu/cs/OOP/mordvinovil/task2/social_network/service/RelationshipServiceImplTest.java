package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.RelationshipRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.RelationshipResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Relationship;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.FriendshipStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.EventPublisherService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.custom.AccessDeniedException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.relationship.DuplicateRelationshipException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.relationship.RelationshipNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.relationship.SelfRelationshipException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.user.UserNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.RelationshipRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.RelationshipServiceImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.RelationshipFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.RelationshipValidator;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory.*;

@ExtendWith(MockitoExtension.class)
public class RelationshipServiceImplTest {

    @Mock
    private RelationshipRepository relationshipRepository;

    @Mock
    private EntityUtils entityUtils;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private RelationshipFactory relationshipFactory;

    @Mock
    private RelationshipValidator relationshipValidator;

    @Mock
    private EventPublisherService eventPublisherService;

    @InjectMocks
    private RelationshipServiceImpl relationshipServiceImpl;

    @Test
    void sendFriendRequest_whenRequestIsValid() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        User receiverUser = createTestUser(2L, "receiver", "123@example.com");
        RelationshipRequest request = createTestRelationshipRequest(receiverUser.getId());
        Relationship relationship = createTestRelationship(currentUser, receiverUser, FriendshipStatus.PENDING);
        RelationshipResponse expectedResponse = createTestRelationshipResponse(relationship);

        doNothing().when(relationshipValidator).validate(request, currentUser);
        when(entityUtils.getUser(receiverUser.getId())).thenReturn(receiverUser);
        when(relationshipFactory.createPendingRelationship(currentUser, receiverUser)).thenReturn(relationship);
        when(relationshipRepository.save(any(Relationship.class))).thenReturn(relationship);
        when(entityMapper.map(relationship, RelationshipResponse.class)).thenReturn(expectedResponse);

        RelationshipResponse result = relationshipServiceImpl.sendFriendRequest(request, currentUser);

        assertNotNull(result);

        verify(relationshipValidator).validate(request, currentUser);
        verify(entityUtils).getUser(receiverUser.getId());
        verify(relationshipFactory).createPendingRelationship(currentUser, receiverUser);
        verify(relationshipRepository).save(any(Relationship.class));
        verify(entityMapper).map(relationship, RelationshipResponse.class);
        verify(eventPublisherService).publishFriendRequest(any(), eq(receiverUser.getId()), eq(currentUser.getId()));
    }

    @Test
    void sendFriendRequest_whenReceiverUserNotFound() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        User receiverUser = createTestUser(2L, "receiver", "123@example.com");
        RelationshipRequest request = createTestRelationshipRequest(receiverUser.getId());

        doNothing().when(relationshipValidator).validate(request, currentUser);
        when(entityUtils.getUser(receiverUser.getId()))
                .thenThrow(new UserNotFoundException(ResponseMessageConstants.FAILURE_USER_NOT_FOUND));

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> relationshipServiceImpl.sendFriendRequest(request, currentUser));

        assertEquals(ResponseMessageConstants.FAILURE_USER_NOT_FOUND, exception.getMessage());

        verify(relationshipValidator).validate(request, currentUser);
        verify(entityUtils).getUser(receiverUser.getId());
        verify(relationshipFactory, never()).createPendingRelationship(any(), any());
        verify(relationshipRepository, never()).save(any());
    }

    @Test
    void sendFriendRequest_whenSendingToSelf() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        RelationshipRequest request = createTestRelationshipRequest(currentUser.getId());

        doThrow(new SelfRelationshipException(ResponseMessageConstants.FAILURE_RELATIONSHIP_TO_SELF_OPERATION))
                .when(relationshipValidator).validate(request, currentUser);

        SelfRelationshipException exception = assertThrows(SelfRelationshipException.class,
                () -> relationshipServiceImpl.sendFriendRequest(request, currentUser));

        assertEquals(ResponseMessageConstants.FAILURE_RELATIONSHIP_TO_SELF_OPERATION, exception.getMessage());

        verify(relationshipValidator).validate(request, currentUser);
        verify(entityUtils, never()).getUser(any());
        verify(relationshipFactory, never()).createPendingRelationship(any(), any());
        verify(relationshipRepository, never()).save(any());
    }

    @Test
    void sendFriendRequest_whenRelationshipAlreadyExists() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        User receiverUser = createTestUser(2L, "receiver", "123@example.com");
        RelationshipRequest request = createTestRelationshipRequest(receiverUser.getId());

        doThrow(new DuplicateRelationshipException(ResponseMessageConstants.FAILURE_RELATIONSHIP_ALREADY_EXISTS))
                .when(relationshipValidator).validate(request, currentUser);

        DuplicateRelationshipException exception = assertThrows(DuplicateRelationshipException.class,
                () -> relationshipServiceImpl.sendFriendRequest(request, currentUser));

        assertEquals(ResponseMessageConstants.FAILURE_RELATIONSHIP_ALREADY_EXISTS, exception.getMessage());

        verify(relationshipValidator).validate(request, currentUser);
        verify(entityUtils, never()).getUser(any());
        verify(relationshipFactory, never()).createPendingRelationship(any(), any());
        verify(relationshipRepository, never()).save(any());
    }

    @Test
    void acceptFriendRequest_whenRequestIsValid() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        User senderUser = createTestUser(2L, "sender", "sender@example.com");
        RelationshipRequest request = createTestRelationshipRequest(senderUser.getId());
        Relationship relationship = createTestRelationship(senderUser, currentUser, FriendshipStatus.PENDING);
        Relationship updatedRelationship = createTestRelationship(senderUser, currentUser, FriendshipStatus.ACCEPTED);
        RelationshipResponse expectedResponse = createTestRelationshipResponse(updatedRelationship);

        doNothing().when(relationshipValidator).validateStatusChange(request, currentUser);
        when(relationshipRepository.findBySenderIdAndReceiverIdAndStatus(senderUser.getId(), currentUser.getId(), FriendshipStatus.PENDING))
                .thenReturn(Optional.of(relationship));
        when(relationshipRepository.save(any(Relationship.class))).thenReturn(updatedRelationship);
        when(entityMapper.map(updatedRelationship, RelationshipResponse.class)).thenReturn(expectedResponse);

        RelationshipResponse result = relationshipServiceImpl.acceptFriendRequest(request, currentUser);

        assertNotNull(result);
        assertEquals(FriendshipStatus.ACCEPTED, result.getStatus());

        verify(relationshipValidator).validateStatusChange(request, currentUser);
        verify(relationshipRepository).findBySenderIdAndReceiverIdAndStatus(senderUser.getId(), currentUser.getId(), FriendshipStatus.PENDING);
        verify(relationshipRepository).save(any(Relationship.class));
        verify(entityMapper).map(updatedRelationship, RelationshipResponse.class);
        verify(eventPublisherService).publishFriendRequestAccepted(any(), eq(senderUser.getId()), eq(currentUser.getId()));
    }

    @Test
    void acceptFriendRequest_whenRelationshipNotFound() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        RelationshipRequest request = createTestRelationshipRequest(2L);

        doThrow(new RelationshipNotFoundException(ResponseMessageConstants.FAILURE_RELATIONSHIP_NOT_FOUND))
                .when(relationshipValidator).validateStatusChange(request, currentUser);

        RelationshipNotFoundException exception = assertThrows(RelationshipNotFoundException.class,
                () -> relationshipServiceImpl.acceptFriendRequest(request, currentUser));

        assertEquals(ResponseMessageConstants.FAILURE_RELATIONSHIP_NOT_FOUND, exception.getMessage());

        verify(relationshipValidator).validateStatusChange(request, currentUser);
        verify(relationshipRepository, never()).findBySenderIdAndReceiverIdAndStatus(any(), any(), any());
        verify(relationshipRepository, never()).save(any());
    }

    @Test
    void acceptFriendRequest_whenUserIsNotReceiver() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        User senderUser = createTestUser(2L, "sender", "sender@example.com");
        RelationshipRequest request = createTestRelationshipRequest(senderUser.getId());

        doThrow(new AccessDeniedException(ResponseMessageConstants.FAILURE_RELATIONSHIP_CANNOT_CHANGE_QUERY))
                .when(relationshipValidator).validateStatusChange(request, currentUser);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> relationshipServiceImpl.acceptFriendRequest(request, currentUser));

        assertEquals(ResponseMessageConstants.FAILURE_RELATIONSHIP_CANNOT_CHANGE_QUERY, exception.getMessage());

        verify(relationshipValidator).validateStatusChange(request, currentUser);
        verify(relationshipRepository, never()).findBySenderIdAndReceiverIdAndStatus(any(), any(), any());
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

        doNothing().when(relationshipValidator).validateBlockUser(request, currentUser);
        when(entityUtils.getUser(targetUser.getId())).thenReturn(targetUser);
        when(relationshipRepository.findRelationshipBetweenUsers(currentUser.getId(), targetUser.getId()))
                .thenReturn(Optional.of(existingRelationship));
        when(relationshipRepository.save(any(Relationship.class))).thenReturn(updatedRelationship);
        when(entityMapper.map(updatedRelationship, RelationshipResponse.class)).thenReturn(expectedResponse);

        RelationshipResponse result = relationshipServiceImpl.blockUser(request, currentUser);

        assertNotNull(result);
        assertEquals(FriendshipStatus.BLOCKED, result.getStatus());

        verify(relationshipValidator).validateBlockUser(request, currentUser);
        verify(entityUtils).getUser(targetUser.getId());
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

        doNothing().when(relationshipValidator).validateBlockUser(request, currentUser);
        when(entityUtils.getUser(targetUser.getId())).thenReturn(targetUser);
        when(relationshipRepository.findRelationshipBetweenUsers(currentUser.getId(), targetUser.getId()))
                .thenReturn(Optional.empty());
        when(relationshipFactory.createBlockedRelationship(currentUser, targetUser)).thenReturn(newRelationship);
        when(relationshipRepository.save(any(Relationship.class))).thenReturn(newRelationship);
        when(entityMapper.map(newRelationship, RelationshipResponse.class)).thenReturn(expectedResponse);

        RelationshipResponse result = relationshipServiceImpl.blockUser(request, currentUser);

        assertNotNull(result);

        verify(relationshipValidator).validateBlockUser(request, currentUser);
        verify(entityUtils).getUser(targetUser.getId());
        verify(relationshipRepository).findRelationshipBetweenUsers(currentUser.getId(), targetUser.getId());
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
        Page<Relationship> relationshipPage = new PageImpl<>(relationships);
        PageRequest pageRequest = PageRequest.builder()
                .pageNumber(0)
                .size(10)
                .direction(Sort.Direction.DESC)
                .sortBy("createdAt")
                .build();

        when(relationshipRepository.findByUserAndStatus(eq(currentUser.getId()), eq(FriendshipStatus.ACCEPTED), any()))
                .thenReturn(relationshipPage);

        PageResponse<RelationshipResponse> result = relationshipServiceImpl.getFriendList(currentUser, pageRequest);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());

        verify(relationshipRepository).findByUserAndStatus(eq(currentUser.getId()), eq(FriendshipStatus.ACCEPTED), any());
    }

    @Test
    void getBlackList() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        List<Relationship> relationships = List.of(
                createTestRelationship(currentUser, createTestUser(2L, "blocked1", "blocked1@example.com"), FriendshipStatus.BLOCKED)
        );
        Page<Relationship> relationshipPage = new PageImpl<>(relationships);
        PageRequest pageRequest = PageRequest.builder()
                .pageNumber(0)
                .size(10)
                .direction(Sort.Direction.DESC)
                .sortBy("createdAt")
                .build();

        when(relationshipRepository.findByUserAndStatus(eq(currentUser.getId()), eq(FriendshipStatus.BLOCKED), any()))
                .thenReturn(relationshipPage);

        PageResponse<RelationshipResponse> result = relationshipServiceImpl.getBlackList(currentUser, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(relationshipRepository).findByUserAndStatus(eq(currentUser.getId()), eq(FriendshipStatus.BLOCKED), any());
    }

    @Test
    void getDeclinedList() {
        User currentUser = createTestUser(1L, "user", "example@example.com");
        List<Relationship> relationships = List.of(
                createTestRelationship(currentUser, createTestUser(2L, "declined1", "declined1@example.com"), FriendshipStatus.DECLINED)
        );
        Page<Relationship> relationshipPage = new PageImpl<>(relationships);
        PageRequest pageRequest = PageRequest.builder()
                .pageNumber(0)
                .size(10)
                .direction(Sort.Direction.DESC)
                .sortBy("createdAt")
                .build();

        when(relationshipRepository.findByUserAndStatus(eq(currentUser.getId()), eq(FriendshipStatus.DECLINED), any()))
                .thenReturn(relationshipPage);

        PageResponse<RelationshipResponse> result = relationshipServiceImpl.getDeclinedList(currentUser, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(relationshipRepository).findByUserAndStatus(eq(currentUser.getId()), eq(FriendshipStatus.DECLINED), any());
    }
}