package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.*;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.*;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.FriendshipStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.MessageStatus;

import java.time.LocalDateTime;

public class TestDataFactory {

    // User creation
    public static User createTestUser() {
        return createTestUser(1L, "testUser");
    }

    public static User createTestUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("password");
        user.setCity("Москва");
        return user;
    }

    // Auth DTOs
    public static SignUpRequest createSignUpRequest() {
        SignUpRequest request = new SignUpRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setCity("Moscow");
        request.setPassword("password123");
        return request;
    }

    public static SignInRequest createSignInRequest() {
        SignInRequest request = new SignInRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        return request;
    }

    public static JwtAuthenticationResponse createJwtResponse() {
        return new JwtAuthenticationResponse("test-jwt-token");
    }

    // Comment DTOs
    public static CommentRequest createCommentRequest() {
        return createCommentRequest(1L);
    }

    public static CommentRequest createCommentRequest(Long postId) {
        CommentRequest request = new CommentRequest();
        request.setPostId(postId);
        request.setContent("Тестовый комментарий");
        request.setImageUrl("http://example.com/image.jpg");
        return request;
    }

    public static CommentResponse createCommentResponse() {
        return createCommentResponse(1L, "testUser");
    }

    public static CommentResponse createCommentResponse(Long id, String username) {
        CommentResponse response = new CommentResponse();
        response.setId(id);
        response.setContent("Тестовый комментарий");
        response.setImageUrl("http://example.com/image.jpg");
        response.setTime(LocalDateTime.now());
        response.setUsername(username);
        return response;
    }

    // Like DTOs
    public static LikePostRequest createLikePostRequest() {
        LikePostRequest request = new LikePostRequest();
        request.setPostId(1L);
        return request;
    }

    public static LikeCommentRequest createLikeCommentRequest() {
        LikeCommentRequest request = new LikeCommentRequest();
        request.setCommentId(1L);
        return request;
    }

    public static LikePostResponse createLikePostResponse() {
        LikePostResponse response = new LikePostResponse();
        response.setId(1L);
        response.setUserId(1L);
        response.setUsername("testUser");
        response.setPostId(1L);
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }

    public static LikeCommentResponse createLikeCommentResponse() {
        LikeCommentResponse response = new LikeCommentResponse();
        response.setId(1L);
        response.setUserId(1L);
        response.setUsername("testUser");
        response.setCommentId(1L);
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }

    // Message DTOs
    public static MessageRequest createMessageRequest() {
        return MessageRequest.builder()
                .receiverUserId(2L)
                .content("Тестовое сообщение")
                .imageUrl("http://example.com/image.jpg")
                .build();
    }

    public static MessageResponse createMessageResponse() {
        return MessageResponse.builder()
                .id(1L)
                .senderUsername("testUser")
                .receiverUsername("receiverUser")
                .content("Тестовое сообщение")
                .imageUrl("http://example.com/image.jpg")
                .status(MessageStatus.SENT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // Post DTOs
    public static PostRequest createPostRequest() {
        return PostRequest.builder()
                .content("test")
                .imageUrl("http://example.com/image.jpg")
                .build();
    }

    public static PostResponse createPostResponse() {
        return PostResponse.builder()
                .id(1L)
                .username("testUser")
                .content("test")
                .imageUrl("http://example.com/image.jpg")
                .time(LocalDateTime.now())
                .build();
    }

    // Profile DTOs
    public static ProfileRequest createProfileRequest() {
        return ProfileRequest.builder()
                .bio("Тестовое описание")
                .city("Москва")
                .dateOfBirth(LocalDateTime.now().minusYears(25))
                .imageUrl("http://example.com/image.jpg")
                .build();
    }

    public static ProfileResponse createProfileResponse() {
        ProfileResponse response = new ProfileResponse();
        response.setUsername("testUser");
        response.setCity("Москва");
        response.setBio("Тестовое описание");
        response.setImageUrl("http://example.com/image.jpg");
        response.setDateOfBirth(LocalDateTime.now().minusYears(25));
        response.setAge(25);
        response.setIsOnline(true);
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }

    // Relationship DTOs
    public static RelationshipRequest createRelationshipRequest() {
        RelationshipRequest request = new RelationshipRequest();
        request.setTargetUserId(2L);
        return request;
    }

    public static RelationshipResponse createRelationshipResponse() {
        RelationshipResponse response = new RelationshipResponse();
        response.setId(1L);
        response.setSenderId(1L);
        response.setReceiverId(2L);
        response.setStatus(FriendshipStatus.PENDING);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        return response;
    }
}