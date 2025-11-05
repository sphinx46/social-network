package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.common.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.post.PostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.common.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.post.PostResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.cache.CacheEventPublisherService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.custom.AccessDeniedException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.CentralLogger;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.PostRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.storage.FileStorageServiceImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.content.PostServiceImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.ContentFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.PostValidator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.TestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {
    @Mock
    private CentralLogger centralLogger;

    @Mock
    private FileStorageServiceImpl fileStorageServiceImpl;

    @Mock
    private PostRepository postRepository;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private ContentFactory contentFactory;

    @Mock
    private PostValidator postValidator;

    @Mock
    private EntityUtils entityUtils;

    @Mock
    private CacheEventPublisherService cacheEventPublisherService;

    @InjectMocks
    private PostServiceImpl postServiceImpl;

    private User owner;
    private User notOwner;
    private Post post;
    private Post postWithImage;

    @BeforeEach
    void setUp() {
        owner = createTestUser(1L, "owner", "owner@example.com");
        notOwner = createTestUser(2L, "notOwner", "notowner@example.com");
        post = createTestPost(owner, "Initial content", null);
        post.setId(1L);
        postWithImage = createTestPost(owner, "Content with image", "existing.jpg");
        postWithImage.setId(1L);
    }

    @Test
    void createPost_whenRequestIsValid() {
        PostRequest request = createTestPostRequest("New post", "image.jpg");
        Post postToSave = createTestPost(owner, request.getContent(), request.getImageUrl());
        Post savedPost = createTestPost(owner, request.getContent(), request.getImageUrl());
        savedPost.setId(1L);
        PostResponse expectedResponse = createTestPostResponse(savedPost);

        when(contentFactory.createPost(eq(owner), eq(request.getContent()), eq(request.getImageUrl())))
                .thenReturn(postToSave);
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);
        when(entityMapper.map(savedPost, PostResponse.class)).thenReturn(expectedResponse);

        PostResponse result = postServiceImpl.create(request, owner);

        assertNotNull(result);
        assertEquals(expectedResponse.getContent(), result.getContent());
        assertEquals(expectedResponse.getImageUrl(), result.getImageUrl());

        verify(postValidator).validate(request, owner);
        verify(contentFactory).createPost(owner, request.getContent(), request.getImageUrl());
        verify(postRepository).save(any(Post.class));
        verify(entityMapper).map(savedPost, PostResponse.class);
        verify(cacheEventPublisherService).publishPostCreate(eq(postServiceImpl), eq(savedPost), eq(1L));
    }

    @Test
    void editPost_whenRequestIsValid() {
        PostRequest request = createTestPostRequest("Updated content", "new-image.jpg");
        Post updatedPost = createTestPost(owner, request.getContent(), request.getImageUrl());
        updatedPost.setId(1L);
        PostResponse expectedResponse = createTestPostResponse(updatedPost);

        when(entityUtils.getPost(1L)).thenReturn(post);
        doNothing().when(postValidator).validatePostUpdate(request, 1L, owner);
        when(postRepository.save(any(Post.class))).thenReturn(updatedPost);
        when(entityMapper.map(updatedPost, PostResponse.class)).thenReturn(expectedResponse);

        PostResponse result = postServiceImpl.editPost(request, 1L, owner);

        assertNotNull(result);
        assertEquals("Updated content", result.getContent());
        assertEquals("new-image.jpg", result.getImageUrl());

        verify(entityUtils).getPost(1L);
        verify(postValidator).validatePostUpdate(request, 1L, owner);
        verify(postRepository).save(any(Post.class));
        verify(entityMapper).map(updatedPost, PostResponse.class);
        verify(cacheEventPublisherService).publishPostEdit(eq(postServiceImpl), eq(updatedPost), eq(1L));
    }

    @Test
    void uploadImage_whenUserIsOwner() {
        MultipartFile imageFile = mock(MultipartFile.class);
        Post postAfterUpdate = createTestPost(owner, post.getContent(), "new-image.jpg");
        postAfterUpdate.setId(1L);
        PostResponse expectedResponse = createTestPostResponse(postAfterUpdate);

        when(entityUtils.getPost(1L)).thenReturn(post);
        doNothing().when(postValidator).validatePostOwnership(1L, owner);
        when(fileStorageServiceImpl.savePostImage(imageFile, 1L)).thenReturn("new-image.jpg");
        when(postRepository.save(any(Post.class))).thenReturn(postAfterUpdate);
        when(entityMapper.map(postAfterUpdate, PostResponse.class)).thenReturn(expectedResponse);

        PostResponse result = postServiceImpl.uploadImage(1L, imageFile, owner);

        assertNotNull(result);
        assertEquals("new-image.jpg", result.getImageUrl());

        verify(fileStorageServiceImpl).validateImageFile(imageFile);
        verify(entityUtils).getPost(1L);
        verify(postValidator).validatePostOwnership(1L, owner);
        verify(fileStorageServiceImpl).savePostImage(imageFile, 1L);
        verify(postRepository).save(any(Post.class));
        verify(entityMapper).map(postAfterUpdate, PostResponse.class);
        verify(cacheEventPublisherService).publishPostEdit(eq(postServiceImpl), eq(postAfterUpdate), eq(1L));
    }

    @Test
    void removeImage_whenUserIsOwner() {
        Post postAfterRemoval = createTestPost(owner, postWithImage.getContent(), null);
        postAfterRemoval.setId(1L);
        PostResponse expectedResponse = createTestPostResponse(postAfterRemoval);

        when(entityUtils.getPost(1L)).thenReturn(postWithImage);
        doNothing().when(postValidator).validatePostOwnership(1L, owner);
        when(postRepository.save(any(Post.class))).thenReturn(postAfterRemoval);
        when(entityMapper.map(postAfterRemoval, PostResponse.class)).thenReturn(expectedResponse);

        PostResponse result = postServiceImpl.removeImage(1L, owner);

        assertNotNull(result);
        assertNull(result.getImageUrl());

        verify(entityUtils).getPost(1L);
        verify(postValidator).validatePostOwnership(1L, owner);
        verify(fileStorageServiceImpl).deleteFile("existing.jpg");
        verify(postRepository).save(argThat(p -> p.getImageUrl() == null));
        verify(entityMapper).map(postAfterRemoval, PostResponse.class);
        verify(cacheEventPublisherService).publishPostEdit(eq(postServiceImpl), eq(postAfterRemoval), eq(1L));
    }

    @Test
    void getPostById_whenPostExists() {
        PostResponse expectedResponse = createTestPostResponse(post);

        when(entityUtils.getPost(1L)).thenReturn(post);
        when(entityMapper.map(post, PostResponse.class)).thenReturn(expectedResponse);

        PostResponse result = postServiceImpl.getPostById(1L);

        assertNotNull(result);
        assertEquals(expectedResponse.getId(), result.getId());

        verify(entityUtils).getPost(1L);
        verify(entityMapper).map(post, PostResponse.class);
    }

    @Test
    void getAllPostsByUser() {
        PageRequest pageRequest = PageRequest.builder()
                .pageNumber(0)
                .size(10)
                .sortBy("createdAt")
                .direction(Sort.Direction.DESC)
                .build();

        Post post1 = createTestPost(owner, "Post 1", null);
        Post post2 = createTestPost(owner, "Post 2", "img.jpg");
        List<Post> posts = List.of(post1, post2);
        Page<Post> postPage = new PageImpl<>(posts, pageRequest.toPageable(), posts.size());

        List<PostResponse> responses = posts.stream().map(TestDataFactory::createTestPostResponse).toList();
        PageResponse<PostResponse> expectedResponse = PageResponse.<PostResponse>builder()
                .content(responses)
                .totalPages(1)
                .totalElements(2L)
                .pageSize(10)
                .currentPage(0)
                .build();

        when(postRepository.getAllPostsByUserWithCommentsAndLikes(eq(owner), any()))
                .thenReturn(postPage);
        when(entityMapper.mapWithName(any(Post.class), eq(PostResponse.class), eq("withDetails")))
                .thenReturn(responses.get(0), responses.get(1));

        PageResponse<PostResponse> result = postServiceImpl.getAllPostsByUser(owner, pageRequest);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(1, result.getTotalPages());
        assertEquals(2L, result.getTotalElements());
        assertEquals(10, result.getPageSize());
        assertEquals(0, result.getCurrentPage());

        verify(postRepository).getAllPostsByUserWithCommentsAndLikes(eq(owner), any());
        verify(entityMapper, times(2)).mapWithName(any(Post.class), eq(PostResponse.class), eq("withDetails"));
    }

    @Test
    void editPost_whenUserIsNotOwner() {
        PostRequest request = createTestPostRequest("Updated", "image.jpg");

        doThrow(new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED))
                .when(postValidator).validatePostUpdate(request, 1L, notOwner);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> postServiceImpl.editPost(request, 1L, notOwner));

        assertEquals(ResponseMessageConstants.ACCESS_DENIED, exception.getMessage());

        verify(postValidator).validatePostUpdate(request, 1L, notOwner);
        verify(postRepository, never()).save(any());
        verify(cacheEventPublisherService, never()).publishPostEdit(any(), any(), anyLong());
    }

    @Test
    void uploadImage_whenUserIsNotOwner() {
        MultipartFile imageFile = mock(MultipartFile.class);

        doThrow(new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED))
                .when(postValidator).validatePostOwnership(1L, notOwner);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> postServiceImpl.uploadImage(1L, imageFile, notOwner));

        assertEquals(ResponseMessageConstants.ACCESS_DENIED, exception.getMessage());

        verify(postValidator).validatePostOwnership(1L, notOwner);
        verify(fileStorageServiceImpl, never()).savePostImage(any(), anyLong());
        verify(postRepository, never()).save(any());
        verify(cacheEventPublisherService, never()).publishPostEdit(any(), any(), anyLong());
    }

    @Test
    void removeImage_whenUserIsNotOwner() {
        doThrow(new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED))
                .when(postValidator).validatePostOwnership(1L, notOwner);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> postServiceImpl.removeImage(1L, notOwner));

        assertEquals(ResponseMessageConstants.ACCESS_DENIED, exception.getMessage());

        verify(postValidator).validatePostOwnership(1L, notOwner);
        verify(fileStorageServiceImpl, never()).deleteFile(anyString());
        verify(postRepository, never()).save(any());
        verify(cacheEventPublisherService, never()).publishPostEdit(any(), any(), anyLong());
    }
}