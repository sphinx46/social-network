package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PostResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Role;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.custom.AccessDeniedException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.PostNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.PostRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private PostService postService;

    private User owner;
    private User notOwner;
    private Post post;
    private Post postWithImage;

    @BeforeEach
    void setUp() {
        owner = createTestUser(1L, "owner", "owner@example.com");
        notOwner = createTestUser(2L, "not owner", "123@gmail.com");
        post = createTestPost(owner, "content", null);
        postWithImage = createTestPost(owner, "Content", "new-image.jpg");
        post.setId(1L);
    }


    @Test
    void uploadImage_ShouldUploadImageWhenUserIsOwner() {
        User currentUser = createTestUser(1L, owner.getUsername(), "owner@example.com");
        MultipartFile imageFile = mock(MultipartFile.class);;
        PostResponse expectedResponse = createTestPostResponse(postWithImage);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(fileStorageService.savePostImage(imageFile, 1L)).thenReturn("new-image.jpg");
        when(postRepository.save(any(Post.class))).thenReturn(postWithImage);
        when(modelMapper.map(postWithImage, PostResponse.class)).thenReturn(expectedResponse);

        PostResponse result = postService.uploadImage(1L, imageFile, currentUser);

        assertNotNull(result);
        assertEquals("new-image.jpg", result.getImageUrl());
        verify(fileStorageService).validateImageFile(imageFile);
        verify(postRepository).findById(1L);
        verify(fileStorageService).savePostImage(imageFile, 1L);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void editPost_whenRequestIsValid() {
        post.setId(1L);

        PostRequest request = createTestPostRequest("example updated", "new-image.jpg");
        Post updatedPost = createTestPost(owner, request.getContent(), request.getImageUrl());
        updatedPost.setId(1L);
        
        PostResponse expectedResponse = createTestPostResponse(updatedPost);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(updatedPost);
        when(modelMapper.map(updatedPost, PostResponse.class)).thenReturn(expectedResponse);

        PostResponse response = postService.editPost(request, 1L, owner);

        assertNotNull(response);
        assertEquals(expectedResponse.getImageUrl(), response.getImageUrl());
        assertEquals(expectedResponse.getContent(), response.getContent());

        verify(postRepository).findById(1L);
        verify(postRepository).save(any(Post.class));
        verify(modelMapper).map(updatedPost, PostResponse.class);
    }

    @Test
    void editPost_whenPostIsNotExists() {
        User currentUser = createTestUser(1L, "owner", "owner@example.com");

        PostRequest request = createTestPostRequest("example updated", "new-image.jpg");

        PostNotFoundException postNotFoundException = assertThrows(PostNotFoundException.class,
                () -> postService.editPost(request, 1L, currentUser));

        assertEquals(ResponseMessageConstants.NOT_FOUND, postNotFoundException.getMessage());
    }

    @Test
    void editPost_whenUserIsNotOwner() {
        Post post = createTestPost(owner, "example", null);
        post.setId(1L);

        PostRequest request = createTestPostRequest("example updated", "new-image.jpg");

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        AccessDeniedException accessDeniedException = assertThrows(AccessDeniedException.class,
                () -> postService.editPost(request, 1L, notOwner));

        assertEquals(ResponseMessageConstants.ACCESS_DENIED, accessDeniedException.getMessage());
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void uploadImage_ShouldThrowExceptionWhenUserNotOwner() {
        MultipartFile imageFile = mock(MultipartFile.class);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> postService.uploadImage(1L, imageFile, notOwner));

        assertEquals(ResponseMessageConstants.ACCESS_DENIED, exception.getMessage());
        verify(fileStorageService, never()).savePostImage(any(), anyLong());
    }

    @Test
    void uploadImage_ShouldThrowExceptionWhenPostNotFound() {
        MultipartFile imageFile = mock(MultipartFile.class);

        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        PostNotFoundException exception = assertThrows(PostNotFoundException.class,
                () -> postService.uploadImage(1L, imageFile, owner));

        assertEquals(ResponseMessageConstants.NOT_FOUND, exception.getMessage());
    }

    @Test
    void removeImage_ShouldRemoveImageSuccessfully() {
        postWithImage.setId(1L);

        PostResponse responseForPostWithoutImage = createTestPostResponse(post);

        when(postRepository.findById(1L)).thenReturn(Optional.of(postWithImage));
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(modelMapper.map(any(Post.class), eq(PostResponse.class)))
                .thenReturn(responseForPostWithoutImage);

        PostResponse result = postService.removeImage(1L, owner);

        assertNotNull(result);
        verify(fileStorageService).deleteFile("new-image.jpg");
        verify(postRepository).save(argThat(p -> p.getImageUrl() == null));
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

    private Post createTestPost(User user, String content, String imageUrl) {
        return Post.builder()
                .user(user)
                .imageUrl(imageUrl)
                .content(content)
                .build();
    }

    private PostRequest createTestPostRequest(String content, String imageUrl) {
        return PostRequest.builder()
                .content(content)
                .imageUrl(imageUrl)
                .build();
    }

    private PostResponse createTestPostResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .username(post.getUser().getUsername())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .time(post.getTime())
                .build();
    }
}
