package ru.cs.vsu.social_network.contents_service.service.serviceImpl.content;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.post.PostCreateRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.post.PostEditRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.post.PostRemoveImageRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.post.PostUploadImageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.PostResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.contents_service.entity.Post;
import ru.cs.vsu.social_network.contents_service.exception.post.PostUploadImageException;
import ru.cs.vsu.social_network.contents_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.contents_service.provider.PostEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.PostRepository;
import ru.cs.vsu.social_network.contents_service.service.cache.CacheEventPublisherService;
import ru.cs.vsu.social_network.contents_service.service.serviceImpl.content.PostServiceImpl;
import ru.cs.vsu.social_network.contents_service.utils.MessageConstants;
import ru.cs.vsu.social_network.contents_service.utils.TestDataFactory;
import ru.cs.vsu.social_network.contents_service.utils.factory.content.PostFactory;
import ru.cs.vsu.social_network.contents_service.validation.PostValidator;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для {@link PostServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    private static final UUID USER_ID = UUID.fromString("3fc4530f-5c0c-429d-8a64-810ce3bd1415");
    private static final UUID POST_ID = UUID.fromString("8dce7d87-60d9-4b66-8851-e7dba4684538");
    private static final UUID ANOTHER_USER_ID = UUID.fromString("e0d8a734-6f6c-4ab4-b4fe-e93cc63d8406");

    @Mock
    private EntityMapper mapper;
    @Mock
    private PostRepository postRepository;
    @Mock
    private PostFactory postFactory;
    @Mock
    private PostEntityProvider postEntityProvider;
    @Mock
    private PostValidator postValidator;
    @Mock
    private CacheEventPublisherService cacheEventPublisherService;

    @InjectMocks
    private PostServiceImpl postService;

    @Test
    @DisplayName("Создание поста - успешно")
    void create_whenRequestIsValid_shouldReturnResponse() {
        PostCreateRequest request = TestDataFactory.createPostCreateRequest("Test messaging");
        Post post = TestDataFactory.createPostEntity(POST_ID, USER_ID, "Test messaging", null);
        Post savedPost = TestDataFactory.createPostEntity(POST_ID);
        PostResponse expectedResponse = TestDataFactory.createPostResponse(POST_ID, USER_ID);

        when(postFactory.create(USER_ID, request)).thenReturn(post);
        when(postRepository.save(post)).thenReturn(savedPost);
        when(mapper.map(savedPost, PostResponse.class)).thenReturn(expectedResponse);
        doNothing().when(cacheEventPublisherService).publishPostCreated(any(), any(), any(), any());

        PostResponse actual = postService.create(USER_ID, request);

        assertNotNull(actual);
        assertEquals(expectedResponse, actual);
        verify(postRepository).save(post);
        verify(postRepository).flush();
    }

    @Test
    @DisplayName("Редактирование поста - успешно")
    void editPost_whenRequestIsValid_shouldReturnResponse() {
        PostEditRequest request = TestDataFactory.createPostEditRequest(POST_ID, "Updated messaging");
        Post post = TestDataFactory.createPostEntity(POST_ID, USER_ID, "Old messaging", null);
        Post updatedPost = TestDataFactory.createPostEntity(POST_ID, USER_ID, "Updated messaging", null);
        PostResponse expectedResponse = TestDataFactory.createPostResponse(POST_ID, USER_ID);

        doNothing().when(postValidator).validateOwnership(USER_ID, POST_ID);
        when(postEntityProvider.getById(POST_ID)).thenReturn(post);
        when(postRepository.save(post)).thenReturn(updatedPost);
        when(mapper.map(updatedPost, PostResponse.class)).thenReturn(expectedResponse);
        doNothing().when(cacheEventPublisherService).publishPostUpdated(any(), any(), any(), any());

        PostResponse actual = postService.editPost(USER_ID, request);

        assertEquals("Updated messaging", post.getContent());
        assertEquals(expectedResponse, actual);
        verify(postValidator).validateOwnership(USER_ID, POST_ID);
    }

    @Test
    @DisplayName("Редактирование поста - доступ запрещен")
    void editPost_whenUserNotOwner_shouldThrowException() {
        PostEditRequest request = TestDataFactory.createPostEditRequest(POST_ID, "Updated messaging");

        doThrow(new AccessDeniedException(MessageConstants.ACCESS_DENIED))
                .when(postValidator).validateOwnership(ANOTHER_USER_ID, POST_ID);

        assertThrows(AccessDeniedException.class, () -> postService.editPost(ANOTHER_USER_ID, request));

        verify(postValidator).validateOwnership(ANOTHER_USER_ID, POST_ID);
        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("Загрузка изображения поста - успешно")
    void uploadPostImage_whenRequestIsValid_shouldReturnResponse() {
        String imageUrl = "http://example.com/image.jpg";
        PostUploadImageRequest request = TestDataFactory.createPostUploadImageRequest(POST_ID, imageUrl);
        Post post = TestDataFactory.createPostEntity(POST_ID, USER_ID, "Content", null);
        Post updatedPost = TestDataFactory.createPostEntity(POST_ID, USER_ID, "Content", imageUrl);
        PostResponse expectedResponse = TestDataFactory.createPostResponse(POST_ID, USER_ID);

        doNothing().when(postValidator).validateOwnership(USER_ID, POST_ID);
        when(postEntityProvider.getById(POST_ID)).thenReturn(post);
        when(postRepository.save(post)).thenReturn(updatedPost);
        when(mapper.map(updatedPost, PostResponse.class)).thenReturn(expectedResponse);
        doNothing().when(cacheEventPublisherService).publishPostUpdated(any(), any(), any(), any());

        PostResponse actual = postService.uploadPostImage(USER_ID, request);

        assertEquals(imageUrl, post.getImageUrl());
        assertEquals(expectedResponse, actual);
        verify(postValidator).validateOwnership(USER_ID, POST_ID);
    }

    @Test
    @DisplayName("Загрузка изображения поста - пустой URL")
    void uploadPostImage_whenImageUrlEmpty_shouldThrowException() {
        PostUploadImageRequest request = TestDataFactory.createPostUploadImageRequest(POST_ID, " ");
        Post post = TestDataFactory.createPostEntity(POST_ID, USER_ID, "Content", null);

        doNothing().when(postValidator).validateOwnership(USER_ID, POST_ID);
        when(postEntityProvider.getById(POST_ID)).thenReturn(post);

        assertThrows(PostUploadImageException.class, () -> postService.uploadPostImage(USER_ID, request));

        verify(postValidator).validateOwnership(USER_ID, POST_ID);
        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("Удаление изображения поста - успешно")
    void removeImage_whenRequestIsValid_shouldReturnResponse() {
        PostRemoveImageRequest request = TestDataFactory.createPostRemoveImageRequest(POST_ID);
        Post post = TestDataFactory.createPostEntity(POST_ID, USER_ID, "Content",
                "http://example.com/image.jpg");
        Post updatedPost = TestDataFactory.createPostEntity(POST_ID, USER_ID, "Content", null);
        PostResponse expectedResponse = TestDataFactory.createPostResponse(POST_ID, USER_ID);

        doNothing().when(postValidator).validateOwnership(USER_ID, POST_ID);
        when(postEntityProvider.getById(POST_ID)).thenReturn(post);
        when(postRepository.save(post)).thenReturn(updatedPost);
        when(mapper.map(updatedPost, PostResponse.class)).thenReturn(expectedResponse);
        doNothing().when(cacheEventPublisherService).publishPostUpdated(any(), any(), any(), any());

        PostResponse actual = postService.removeImage(USER_ID, request);

        assertNull(post.getImageUrl());
        assertEquals(expectedResponse, actual);
        verify(postValidator).validateOwnership(USER_ID, POST_ID);
    }

    @Test
    @DisplayName("Получение поста по ID - успешно")
    void getPostById_whenPostExists_shouldReturnResponse() {
        Post post = TestDataFactory.createPostEntity(POST_ID);
        PostResponse expectedResponse = TestDataFactory.createPostResponse(POST_ID, USER_ID);

        when(postEntityProvider.getById(POST_ID)).thenReturn(post);
        when(mapper.map(post, PostResponse.class)).thenReturn(expectedResponse);

        PostResponse actual = postService.getPostById(POST_ID);

        assertEquals(expectedResponse, actual);
        verify(postEntityProvider).getById(POST_ID);
    }

    @Test
    @DisplayName("Получение постов пользователя - успешно")
    void getAllPostsByUser_whenUserExists_shouldReturnPageResponse() {
        PageRequest pageRequest = TestDataFactory.createPageRequest(0, 10);
        Post post = TestDataFactory.createPostEntity(POST_ID);
        List<Post> posts = List.of(post);
        org.springframework.data.domain.Page<Post> page = TestDataFactory.createPage(posts);
        PostResponse postResponse = TestDataFactory.createPostResponse(POST_ID, USER_ID);
        PageResponse<PostResponse> expectedResponse = TestDataFactory.createPageResponse(List.of(postResponse));

        when(postRepository.findAllByOwnerId(USER_ID, pageRequest.toPageable())).thenReturn(page);
        when(mapper.map(post, PostResponse.class)).thenReturn(postResponse);

        PageResponse<PostResponse> actual = postService.getAllPostsByUser(USER_ID, pageRequest);

        assertNotNull(actual);
        assertEquals(1, actual.getContent().size());
        verify(postRepository).findAllByOwnerId(USER_ID, pageRequest.toPageable());
    }
}