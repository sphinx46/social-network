package ru.cs.vsu.social_network.contents_service.service.serviceImpl.content;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import ru.cs.vsu.social_network.contents_service.dto.request.like.LikeCommentRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.LikeCommentResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.contents_service.entity.LikeComment;
import ru.cs.vsu.social_network.contents_service.exception.like.LikeNotFoundException;
import ru.cs.vsu.social_network.contents_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.contents_service.provider.LikeCommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.LikeCommentRepository;
import ru.cs.vsu.social_network.contents_service.utils.MessageConstants;
import ru.cs.vsu.social_network.contents_service.utils.TestDataFactory;
import ru.cs.vsu.social_network.contents_service.utils.factory.content.LikeCommentFactory;
import ru.cs.vsu.social_network.contents_service.validation.LikeCommentValidator;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для {@link LikeCommentServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class LikeCommentServiceImplTest {

    private static final UUID USER_ID = UUID.fromString("3fc4530f-5c0c-429d-8a64-810ce3bd1415");
    private static final UUID COMMENT_ID = UUID.fromString("40d4dde4-663d-4113-bd08-25f5e1d42efe");
    private static final UUID LIKE_ID = UUID.fromString("5ab3c6d7-ec6f-49ad-95ac-6c752ad8172e");

    @Mock
    private EntityMapper mapper;
    @Mock
    private LikeCommentRepository likeCommentRepository;
    @Mock
    private LikeCommentFactory likeCommentFactory;
    @Mock
    private LikeCommentValidator likeCommentValidator;
    @Mock
    private LikeCommentEntityProvider likeCommentEntityProvider;

    @InjectMocks
    private LikeCommentServiceImpl likeCommentService;

    @Test
    @DisplayName("Создание лайка комментария - успешно")
    void create_whenRequestIsValid_shouldReturnResponse() {
        LikeCommentRequest request = TestDataFactory.createLikeCommentRequest(COMMENT_ID);
        LikeComment likeComment = TestDataFactory.createLikeCommentEntity(LIKE_ID, USER_ID, COMMENT_ID);
        LikeComment savedLike = TestDataFactory.createLikeCommentEntity(LIKE_ID, USER_ID, COMMENT_ID);
        LikeCommentResponse expectedResponse = TestDataFactory.createLikeCommentResponse(LIKE_ID, USER_ID, COMMENT_ID);

        when(likeCommentEntityProvider.existsByOwnerIdAndCommentId(USER_ID, COMMENT_ID)).thenReturn(false);
        when(likeCommentFactory.create(USER_ID, request)).thenReturn(likeComment);
        when(likeCommentRepository.save(likeComment)).thenReturn(savedLike);
        when(mapper.map(savedLike, LikeCommentResponse.class)).thenReturn(expectedResponse);

        LikeCommentResponse actual = likeCommentService.create(USER_ID, request);

        assertNotNull(actual);
        assertEquals(expectedResponse, actual);
        verify(likeCommentEntityProvider).existsByOwnerIdAndCommentId(USER_ID, COMMENT_ID);
        verify(likeCommentRepository).save(likeComment);
    }

    @Test
    @DisplayName("Создание лайка комментария - лайк уже существует")
    void create_whenLikeAlreadyExists_shouldThrowException() {
        LikeCommentRequest request = TestDataFactory.createLikeCommentRequest(COMMENT_ID);

        when(likeCommentEntityProvider.existsByOwnerIdAndCommentId(USER_ID, COMMENT_ID)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> likeCommentService.create(USER_ID, request));

        assertEquals("Ошибка! Лайк уже существует!", exception.getMessage());
        verify(likeCommentEntityProvider).existsByOwnerIdAndCommentId(USER_ID, COMMENT_ID);
        verify(likeCommentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Удаление лайка комментария - успешно")
    void delete_whenLikeExists_shouldReturnResponse() {
        LikeCommentRequest request = TestDataFactory.createLikeCommentRequest(COMMENT_ID);
        LikeComment likeComment = TestDataFactory.createLikeCommentEntity(LIKE_ID, USER_ID, COMMENT_ID);
        LikeCommentResponse expectedResponse = TestDataFactory.createLikeCommentResponse(LIKE_ID, USER_ID, COMMENT_ID);

        when(likeCommentEntityProvider.findByOwnerIdAndCommentId(USER_ID, COMMENT_ID))
                .thenReturn(Optional.of(likeComment));
        doNothing().when(likeCommentValidator).validateOwnership(USER_ID, LIKE_ID);
        when(mapper.map(likeComment, LikeCommentResponse.class)).thenReturn(expectedResponse);

        LikeCommentResponse actual = likeCommentService.delete(USER_ID, request);

        assertEquals(expectedResponse, actual);
        verify(likeCommentEntityProvider).findByOwnerIdAndCommentId(USER_ID, COMMENT_ID);
        verify(likeCommentValidator).validateOwnership(USER_ID, LIKE_ID);
        verify(likeCommentRepository).delete(likeComment);
    }

    @Test
    @DisplayName("Удаление лайка комментария - лайк не найден")
    void delete_whenLikeNotFound_shouldThrowException() {
        LikeCommentRequest request = TestDataFactory.createLikeCommentRequest(COMMENT_ID);

        when(likeCommentEntityProvider.findByOwnerIdAndCommentId(USER_ID, COMMENT_ID))
                .thenReturn(Optional.empty());

        LikeNotFoundException exception = assertThrows(LikeNotFoundException.class,
                () -> likeCommentService.delete(USER_ID, request));

        assertEquals(MessageConstants.LIKE_NOT_FOUND_FAILURE, exception.getMessage());
        verify(likeCommentEntityProvider).findByOwnerIdAndCommentId(USER_ID, COMMENT_ID);
        verify(likeCommentRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Получение всех лайков комментария - успешно")
    void getAllLikesByComment_whenCommentExists_shouldReturnPageResponse() {
        PageRequest pageRequest = TestDataFactory.createPageRequest(0, 10);
        LikeComment likeComment = TestDataFactory.createLikeCommentEntity(LIKE_ID, USER_ID, COMMENT_ID);
        List<LikeComment> likes = List.of(likeComment);
        Page<LikeComment> likesPage = TestDataFactory.createPage(likes);
        LikeCommentResponse likeResponse = TestDataFactory.createLikeCommentResponse(LIKE_ID, USER_ID, COMMENT_ID);
        PageResponse<LikeCommentResponse> expectedResponse =
                TestDataFactory.createPageResponse(List.of(likeResponse));

        when(likeCommentRepository.findAllByCommentId(COMMENT_ID, pageRequest.toPageable()))
                .thenReturn(likesPage);
        when(mapper.map(likeComment, LikeCommentResponse.class)).thenReturn(likeResponse);

        PageResponse<LikeCommentResponse> actual = likeCommentService.getAllLikesByComment(COMMENT_ID, pageRequest);

        assertNotNull(actual);
        assertEquals(1, actual.getContent().size());
        verify(likeCommentRepository).findAllByCommentId(COMMENT_ID, pageRequest.toPageable());
    }

    @Test
    @DisplayName("Получение количества лайков комментария - успешно")
    void getLikesCountByComment_whenCommentExists_shouldReturnCount() {
        Long expectedCount = 5L;

        when(likeCommentEntityProvider.getLikesCountByComment(COMMENT_ID)).thenReturn(expectedCount);

        Long actual = likeCommentService.getLikesCountByComment(COMMENT_ID);

        assertEquals(expectedCount, actual);
        verify(likeCommentEntityProvider).getLikesCountByComment(COMMENT_ID);
    }

    @Test
    @DisplayName("Получение всех лайков комментария - пустой результат")
    void getAllLikesByComment_whenNoLikes_shouldReturnEmptyPageResponse() {
        PageRequest pageRequest = TestDataFactory.createPageRequest(0, 10);
        Page<LikeComment> emptyPage = Page.empty();
        PageResponse<LikeCommentResponse> expectedResponse =
                TestDataFactory.createPageResponse(List.of());

        when(likeCommentRepository.findAllByCommentId(COMMENT_ID, pageRequest.toPageable()))
                .thenReturn(emptyPage);

        PageResponse<LikeCommentResponse> actual = likeCommentService.getAllLikesByComment(COMMENT_ID, pageRequest);

        assertNotNull(actual);
        assertTrue(actual.getContent().isEmpty());
        verify(likeCommentRepository).findAllByCommentId(COMMENT_ID, pageRequest.toPageable());
    }
}