package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

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
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.PostRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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


    @Test
    void uploadImage_ShouldUploadImageWhenUserIsOwner() {
        User currentUser = createTestUser(1L, "owner", "owner@example.com");
        MultipartFile imageFile = mock(MultipartFile.class);
        Post existingPost = createTestPost(currentUser, "Content", null);
        Post updatedPost = createTestPost(currentUser, "Content", "new-image.jpg");
        PostResponse expectedResponse = createTestPostResponse(1L, currentUser.getUsername(), "Content", "new-image.jpg");

        when(postRepository.findById(1L)).thenReturn(Optional.of(existingPost));
        when(fileStorageService.savePostImage(imageFile, 1L)).thenReturn("new-image.jpg");
        when(postRepository.save(any(Post.class))).thenReturn(updatedPost);
        when(modelMapper.map(updatedPost, PostResponse.class)).thenReturn(expectedResponse);

        PostResponse result = postService.uploadImage(1L, imageFile, currentUser);

        assertNotNull(result);
        assertEquals("new-image.jpg", result.getImageUrl());
        verify(fileStorageService).validateImageFile(imageFile);
        verify(postRepository).findById(1L);
        verify(fileStorageService).savePostImage(imageFile, 1L);
        verify(postRepository).save(any(Post.class));
    }

//    @Test
//    void editPost_whenRequestIsValid() {
//        User currentUser = createTestUser(1L, "owner", "owner@example.com");
//        Post post = createTestPost(currentUser, "example", null);
//        PostRequest request = createTestPostRequest("example updated", "new-image.jpg");
//
//        List<Post> posts = postRepository.getAllPostsByUser(currentUser);
//
//
//        PostResponse response = postService.editPost(request, post.getId(), currentUser);
//
//        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
//        when(modelMapper.map(post, PostResponse.class)).thenReturn(response);
//
//        assertNotNull(response);
//        assertEquals("new-image.jpg", response.getImageUrl());
//        assertEquals("example updated", response.getContent());
//    }


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

    private PostResponse createTestPostResponse(Long id, String username, String content, String imageUrl) {
        return PostResponse.builder()
                .id(id)
                .username(username)
                .content(content)
                .imageUrl(imageUrl)
                .time(LocalDateTime.now())
                .build();
    }
}
