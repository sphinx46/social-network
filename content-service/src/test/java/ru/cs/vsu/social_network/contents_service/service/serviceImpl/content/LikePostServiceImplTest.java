package ru.cs.vsu.social_network.contents_service.service.serviceImpl.content;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import ru.cs.vsu.social_network.contents_service.dto.request.like.LikePostRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.LikePostResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.contents_service.entity.LikePost;
import ru.cs.vsu.social_network.contents_service.exception.like.LikeNotFoundException;
import ru.cs.vsu.social_network.contents_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.contents_service.provider.LikePostEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.LikePostRepository;
import ru.cs.vsu.social_network.contents_service.service.cache.CacheEventPublisherService;
import ru.cs.vsu.social_network.contents_service.utils.MessageConstants;
import ru.cs.vsu.social_network.contents_service.utils.TestDataFactory;
import ru.cs.vsu.social_network.contents_service.utils.factory.content.LikePostFactory;
import ru.cs.vsu.social_network.contents_service.validation.LikePostValidator;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для {@link LikePostServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class LikePostServiceImplTest {

    private static final UUID USER_ID = UUID.fromString("3fc4530f-5c0c-429d-8a64-810ce3bd1415");
    private static final UUID POST_ID = UUID.fromString("8dce7d87-60d9-4b66-8851-e7dba4684538");
    private static final UUID LIKE_ID = UUID.fromString("5ab3c6d7-ec6f-49ad-95ac-6c752ad8172e");

    @Mock
    private EntityMapper mapper;
    @Mock
    private LikePostRepository likePostRepository;
    @Mock
    private LikePostFactory likePostFactory;
    @Mock
    private LikePostValidator likePostValidator;
    @Mock
    private LikePostEntityProvider likePostEntityProvider;
    @Mock
    private CacheEventPublisherService cacheEventPublisherService;

    @InjectMocks
    private LikePostServiceImpl likePostService;

    @Test
    @DisplayName("Создание лайка поста - успешно")
    void create_whenRequestIsValid_shouldReturnResponse() {
        LikePostRequest request = TestDataFactory.createLikePostRequest(POST_ID);
        LikePost likePost = TestDataFactory.createLikePostEntity(LIKE_ID, USER_ID, POST_ID);
        LikePost savedLike = TestDataFactory.createLikePostEntity(LIKE_ID, USER_ID, POST_ID);
        LikePostResponse expectedResponse = TestDataFactory.createLikePostResponse(LIKE_ID, USER_ID, POST_ID);

        when(likePostEntityProvider.existsByOwnerIdAndPostId(USER_ID, POST_ID)).thenReturn(false);
        when(likePostFactory.create(USER_ID, request)).thenReturn(likePost);
        when(likePostRepository.save(likePost)).thenReturn(savedLike);
        when(mapper.map(savedLike, LikePostResponse.class)).thenReturn(expectedResponse);
        doNothing().when(cacheEventPublisherService).publishPostLikeCreated(any(), any(), any(), any(), any());

        LikePostResponse actual = likePostService.create(USER_ID, request);

        assertNotNull(actual);
        assertEquals(expectedResponse, actual);
        verify(likePostEntityProvider).existsByOwnerIdAndPostId(USER_ID, POST_ID);
        verify(likePostRepository).save(likePost);
        verify(likePostRepository).flush();
    }

    @Test
    @DisplayName("Создание лайка поста - лайк уже существует")
    void create_whenLikeAlreadyExists_shouldThrowException() {
        LikePostRequest request = TestDataFactory.createLikePostRequest(POST_ID);

        when(likePostEntityProvider.existsByOwnerIdAndPostId(USER_ID, POST_ID)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> likePostService.create(USER_ID, request));

        assertEquals("Ошибка! Лайк уже существует!", exception.getMessage());
        verify(likePostEntityProvider).existsByOwnerIdAndPostId(USER_ID, POST_ID);
        verify(likePostRepository, never()).save(any());
    }

    @Test
    @DisplayName("Удаление лайка поста - успешно")
    void delete_whenLikeExists_shouldReturnResponse() {
        LikePostRequest request = TestDataFactory.createLikePostRequest(POST_ID);
        LikePost likePost = TestDataFactory.createLikePostEntity(LIKE_ID, USER_ID, POST_ID);
        LikePostResponse expectedResponse = TestDataFactory.createLikePostResponse(LIKE_ID, USER_ID, POST_ID);

        when(likePostEntityProvider.findByOwnerIdAndPostId(USER_ID, POST_ID))
                .thenReturn(Optional.of(likePost));
        doNothing().when(likePostValidator).validateOwnership(USER_ID, LIKE_ID);
        when(mapper.map(likePost, LikePostResponse.class)).thenReturn(expectedResponse);
        doNothing().when(cacheEventPublisherService).publishPostLikeDeleted(any(), any(), any(), any(), any());

        LikePostResponse actual = likePostService.delete(USER_ID, request);

        assertEquals(expectedResponse, actual);
        verify(likePostEntityProvider).findByOwnerIdAndPostId(USER_ID, POST_ID);
        verify(likePostValidator).validateOwnership(USER_ID, LIKE_ID);
        verify(likePostRepository).delete(likePost);
        verify(likePostRepository).flush();
    }

    @Test
    @DisplayName("Удаление лайка поста - лайк не найден")
    void delete_whenLikeNotFound_shouldThrowException() {
        LikePostRequest request = TestDataFactory.createLikePostRequest(POST_ID);

        when(likePostEntityProvider.findByOwnerIdAndPostId(USER_ID, POST_ID))
                .thenReturn(Optional.empty());

        LikeNotFoundException exception = assertThrows(LikeNotFoundException.class,
                () -> likePostService.delete(USER_ID, request));

        assertEquals(MessageConstants.LIKE_NOT_FOUND_FAILURE, exception.getMessage());
        verify(likePostEntityProvider).findByOwnerIdAndPostId(USER_ID, POST_ID);
        verify(likePostRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Получение всех лайков поста - успешно")
    void getAllLikesByPost_whenPostExists_shouldReturnPageResponse() {
        PageRequest pageRequest = TestDataFactory.createPageRequest(0, 10);
        LikePost likePost = TestDataFactory.createLikePostEntity(LIKE_ID, USER_ID, POST_ID);
        List<LikePost> likes = List.of(likePost);
        Page<LikePost> likesPage = TestDataFactory.createPage(likes);
        LikePostResponse likeResponse = TestDataFactory.createLikePostResponse(LIKE_ID, USER_ID, POST_ID);
        PageResponse<LikePostResponse> expectedResponse =
                TestDataFactory.createPageResponse(List.of(likeResponse));

        when(likePostRepository.findAllByPostId(POST_ID, pageRequest.toPageable()))
                .thenReturn(likesPage);
        when(mapper.map(likePost, LikePostResponse.class)).thenReturn(likeResponse);

        PageResponse<LikePostResponse> actual = likePostService.getAllLikesByPost(POST_ID, pageRequest);

        assertNotNull(actual);
        assertEquals(1, actual.getContent().size());
        verify(likePostRepository).findAllByPostId(POST_ID, pageRequest.toPageable());
    }

    @Test
    @DisplayName("Получение количества лайков поста - успешно")
    void getLikesCountByPost_whenPostExists_shouldReturnCount() {
        Long expectedCount = 10L;

        when(likePostEntityProvider.getLikesCountByPost(POST_ID)).thenReturn(expectedCount);

        Long actual = likePostService.getLikesCountByPost(POST_ID);

        assertEquals(expectedCount, actual);
        verify(likePostEntityProvider).getLikesCountByPost(POST_ID);
    }

    @Test
    @DisplayName("Получение всех лайков поста - пустой результат")
    void getAllLikesByPost_whenNoLikes_shouldReturnEmptyPageResponse() {
        PageRequest pageRequest = TestDataFactory.createPageRequest(0, 10);
        Page<LikePost> emptyPage = Page.empty();
        PageResponse<LikePostResponse> expectedResponse =
                TestDataFactory.createPageResponse(List.of());

        when(likePostRepository.findAllByPostId(POST_ID, pageRequest.toPageable()))
                .thenReturn(emptyPage);

        PageResponse<LikePostResponse> actual = likePostService.getAllLikesByPost(POST_ID, pageRequest);

        assertNotNull(actual);
        assertTrue(actual.getContent().isEmpty());
        verify(likePostRepository).findAllByPostId(POST_ID, pageRequest.toPageable());
    }
}