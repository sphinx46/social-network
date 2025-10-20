package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.*;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.*;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.*;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.FriendshipStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.MessageStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public static User createTestUser(Long id, String username, String email) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("password");
        user.setCity("Москва");
        return user;
    }

    // Post creation
    public static Post createTestPost(User user, String content, String imageUrl) {
        return Post.builder()
                .user(user)
                .imageUrl(imageUrl)
                .content(content)
                .time(LocalDateTime.now())
                .build();
    }

    // Comment creation
    public static Comment createTestComment(Long id, User creator, Post post, String content, String imageUrl) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setImageUrl(imageUrl);
        comment.setPost(post);
        comment.setContent(content);
        comment.setCreator(creator);
        comment.setTime(LocalDateTime.now());
        return comment;
    }

    public static Comment createTestComment(Long id, String content) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setContent(content);
        comment.setCreator(createTestUser(1L, "commentCreator", "creator@example.com"));
        comment.setTime(LocalDateTime.now());
        return comment;
    }

    // Like creation
    public static Like createTestLike(User user, Post post, Comment comment) {
        return Like.builder()
                .user(user)
                .post(post)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // Message creation
    public static Message createTestMessage(User sender, User receiver, String content, String imageUrl,
                                            MessageStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .imageUrl(imageUrl)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }

    // Relationship creation
    public static Relationship createTestRelationship(User sender, User receiver, FriendshipStatus status) {
        return Relationship.builder()
                .sender(sender)
                .receiver(receiver)
                .status(status)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
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

    public static CommentRequest createTestCommentRequest(Long postId, String content, String imageUrl) {
        CommentRequest request = new CommentRequest();
        request.setPostId(postId);
        request.setContent(content);
        request.setImageUrl(imageUrl);
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

    public static CommentResponse createTestCommentResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setImageUrl(comment.getImageUrl());
        response.setTime(comment.getTime());
        response.setUsername(comment.getCreator().getUsername());
        return response;
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

    public static LikeCommentRequest createTestLikeCommentRequest(Long commentId) {
        LikeCommentRequest request = new LikeCommentRequest();
        request.setCommentId(commentId);
        return request;
    }

    public static LikePostRequest createTestLikePostRequest(Long postId) {
        LikePostRequest request = new LikePostRequest();
        request.setPostId(postId);
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

    public static MessageRequest createTestRequest(Long receiverUserId, String content, String imageUrl) {
        return MessageRequest.builder()
                .receiverUserId(receiverUserId)
                .content(content)
                .imageUrl(imageUrl)
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

    public static MessageResponse createTestResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .senderUsername(message.getSender().getUsername())
                .receiverUsername(message.getReceiver().getUsername())
                .content(message.getContent())
                .imageUrl(message.getImageUrl())
                .status(message.getStatus())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .build();
    }

    // Post DTOs
    public static PostRequest createPostRequest() {
        return PostRequest.builder()
                .content("test")
                .imageUrl("http://example.com/image.jpg")
                .build();
    }

    public static PostRequest createTestPostRequest(String content, String imageUrl) {
        return PostRequest.builder()
                .content(content)
                .imageUrl(imageUrl)
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

    public static List<PostResponse> createPostResponseList() {
        List<PostResponse> result = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            var response = PostResponse.builder()
                    .id(Long.valueOf(i))
                    .username("username" + i)
                    .content("test")
                    .imageUrl("http://example.com/image.jpg")
                    .time(LocalDateTime.now())
                    .build();
            result.add(response);
        }
        return result;
    }

    public static PostResponse createTestPostResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .username(post.getUser().getUsername())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .time(post.getTime())
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

    public static RelationshipRequest createTestRelationshipRequest(Long targetUserId) {
        RelationshipRequest request = new RelationshipRequest();
        request.setTargetUserId(targetUserId);
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

    public static RelationshipResponse createTestRelationshipResponse(Relationship relationship) {
        RelationshipResponse response = new RelationshipResponse();
        response.setId(relationship.getId());
        response.setSenderId(relationship.getSender().getId());
        response.setReceiverId(relationship.getReceiver().getId());
        response.setStatus(relationship.getStatus());
        response.setCreatedAt(relationship.getCreatedAt());
        response.setUpdatedAt(relationship.getUpdatedAt());
        return response;
    }
    // Добавить в класс TestDataFactory

    // User creation with specific parameters
    public static User createTestUser(Long id, String username, String email, String city) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("password");
        user.setCity(city);
        return user;
    }

    // Like creation with specific parameters
    public static Like createTestLike(Long id, User user, Post post, Comment comment) {
        Like like = Like.builder()
                .user(user)
                .post(post)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .build();
        like.setId(id); // Устанавливаем ID через сеттер
        return like;
    }

    public static Like createTestLike(User user, Post post) {
        return Like.builder()
                .user(user)
                .post(post)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static Like createTestLike(User user, Comment comment) {
        return Like.builder()
                .user(user)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // Like creation with ID only
    public static Like createTestLike(Long id) {
        Like like = Like.builder()
                .user(createTestUser())
                .createdAt(LocalDateTime.now())
                .build();
        like.setId(id); // Устанавливаем ID через сеттер
        return like;
    }

    // Post creation with ID
    public static Post createTestPost(Long id, User user, String content, String imageUrl) {
        Post post = Post.builder()
                .user(user)
                .imageUrl(imageUrl)
                .content(content)
                .time(LocalDateTime.now())
                .build();
        post.setId(id); // Устанавливаем ID через сеттер
        return post;
    }

    // Comment creation with ID
    public static Comment createTestComment(Long id, User creator, Post post, String content) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setPost(post);
        comment.setContent(content);
        comment.setCreator(creator);
        comment.setTime(LocalDateTime.now());
        return comment;
    }


    // Like DTOs with specific parameters
    public static LikePostResponse createTestLikePostResponse(Long id, Long userId, String username, Long postId) {
        LikePostResponse response = new LikePostResponse();
        response.setId(id);
        response.setUserId(userId);
        response.setUsername(username);
        response.setPostId(postId);
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }

    public static LikeCommentResponse createTestLikeCommentResponse(Long id, Long userId, String username, Long commentId) {
        LikeCommentResponse response = new LikeCommentResponse();
        response.setId(id);
        response.setUserId(userId);
        response.setUsername(username);
        response.setCommentId(commentId);
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }

    // List creation helpers
    public static List<Like> createTestLikesForPost(Long postId, int count) {
        List<Like> likes = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            User user = createTestUser((long) i, "user" + i, "user" + i + "@example.com");
            Post post = createTestPost(postId, user, "Post " + postId, null);
            likes.add(createTestLike((long) i, user, post, null));
        }
        return likes;
    }

    public static List<Like> createTestLikesForComment(Long commentId, int count) {
        List<Like> likes = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            User user = createTestUser((long) i, "user" + i, "user" + i + "@example.com");
            Comment comment = createTestComment(commentId, user, null, "Comment " + commentId);
            likes.add(createTestLike((long) i, user, null, comment));
        }
        return likes;
    }

    // Empty like creation (for null cases in tests)
    public static Like createEmptyTestLike(User user) {
        return Like.builder()
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // Исправленные методы в TestDataFactory

    public static LikeCommentResponse createTestLikeCommentResponse(Like like) {
        LikeCommentResponse response = new LikeCommentResponse();
        response.setId(like.getId());
        response.setUserId(like.getUser().getId());
        response.setUsername(like.getUser().getUsername());
        // Добавляем проверку на null для комментария
        if (like.getComment() != null) {
            response.setCommentId(like.getComment().getId());
        } else {
            response.setCommentId(1L); // или любое другое значение по умолчанию
        }
        response.setCreatedAt(like.getCreatedAt());
        return response;
    }

    public static LikePostResponse createTestLikePostResponse(Like like) {
        LikePostResponse response = new LikePostResponse();
        response.setId(like.getId());
        response.setUserId(like.getUser().getId());
        response.setUsername(like.getUser().getUsername());
        if (like.getPost() != null) {
            response.setPostId(like.getPost().getId());
        } else {
            response.setPostId(1L);
        }
        response.setCreatedAt(like.getCreatedAt());
        return response;
    }

    public static List<NewsFeedResponse> createTestNewsFeedResponseList() {
        List<PostResponse> postResponseList = createPostResponseList();
        List<NewsFeedResponse> newsFeedResponseList = new ArrayList<>();
        for (PostResponse response : postResponseList) {
            var newsFeedResponse = NewsFeedResponse.builder()
                    .postResponse(response)
                    .author(response.getUsername())
                    .id(response.getId())
                    .build();
            newsFeedResponseList.add(newsFeedResponse);
        }
        return newsFeedResponseList;
    }
}
