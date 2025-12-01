package ru.cs.vsu.social_network.contents_service.service.serviceImpl.content;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.CommentDetailsResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.contents_service.entity.Comment;
import ru.cs.vsu.social_network.contents_service.provider.CommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.CommentRepository;
import ru.cs.vsu.social_network.contents_service.service.aggregator.CommentDetailsAggregator;
import ru.cs.vsu.social_network.contents_service.service.serviceImpl.content.CommentDetailsServiceImpl;
import ru.cs.vsu.social_network.contents_service.utils.TestDataFactory;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для {@link CommentDetailsServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class CommentDetailsServiceImplTest {

    private static final UUID USER_ID = UUID.fromString("3fc4530f-5c0c-429d-8a64-810ce3bd1415");
    private static final UUID POST_ID = UUID.fromString("8dce7d87-60d9-4b66-8851-e7dba4684538");
    private static final UUID COMMENT_ID = UUID.fromString("40d4dde4-663d-4113-bd08-25f5e1d42efe");

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommentEntityProvider commentEntityProvider;
    @Mock
    private CommentDetailsAggregator commentDetailsAggregator;

    @InjectMocks
    private CommentDetailsServiceImpl commentDetailsService;

    @Test
    @DisplayName("Получение деталей комментария - успешно с лайками")
    void getCommentDetails_whenIncludeLikes_shouldReturnResponseWithLikes() {
        Comment comment = TestDataFactory.createCommentEntity(COMMENT_ID);
        CommentDetailsResponse expectedResponse = TestDataFactory.createCommentDetailsResponse(COMMENT_ID);

        when(commentEntityProvider.getById(COMMENT_ID)).thenReturn(comment);
        when(commentDetailsAggregator.aggregateCommentDetails(comment, true, 10))
                .thenReturn(expectedResponse);

        CommentDetailsResponse actual =
                commentDetailsService.getCommentDetails(COMMENT_ID, true, 10);

        assertEquals(expectedResponse, actual);
        verify(commentEntityProvider).getById(COMMENT_ID);
        verify(commentDetailsAggregator).aggregateCommentDetails(comment, true, 10);
    }

    @Test
    @DisplayName("Получение деталей комментария - успешно без лайков")
    void getCommentDetails_whenExcludeLikes_shouldReturnResponseWithoutLikes() {
        Comment comment = TestDataFactory.createCommentEntity(COMMENT_ID);
        CommentDetailsResponse expectedResponse = TestDataFactory.createCommentDetailsResponse(COMMENT_ID);

        when(commentEntityProvider.getById(COMMENT_ID)).thenReturn(comment);
        when(commentDetailsAggregator.aggregateCommentDetails(comment, false, 0))
                .thenReturn(expectedResponse);

        CommentDetailsResponse actual =
                commentDetailsService.getCommentDetails(COMMENT_ID, false, 0);

        assertEquals(expectedResponse, actual);
        verify(commentEntityProvider).getById(COMMENT_ID);
        verify(commentDetailsAggregator).aggregateCommentDetails(comment, false, 0);
    }

    @Test
    @DisplayName("Получение деталей комментариев поста - успешно")
    void getPostCommentsDetails_whenPostExists_shouldReturnPageResponse() {
        PageRequest pageRequest = TestDataFactory.createPageRequest(0, 10);
        Comment comment = TestDataFactory.createCommentEntity(COMMENT_ID);
        List<Comment> comments = List.of(comment);
        Page<Comment> commentsPage = TestDataFactory.createPage(comments);
        CommentDetailsResponse detailsResponse = TestDataFactory.createCommentDetailsResponse(COMMENT_ID);
        Page<CommentDetailsResponse> detailsPage = TestDataFactory.createPage(List.of(detailsResponse));
        PageResponse<CommentDetailsResponse> expectedResponse =
                TestDataFactory.createPageResponse(List.of(detailsResponse));

        when(commentRepository.findByPostIdOrderByCreatedAtDesc(POST_ID, pageRequest.toPageable()))
                .thenReturn(commentsPage);
        when(commentDetailsAggregator.aggregateCommentsPage(commentsPage, true, 10))
                .thenReturn(detailsPage);

        PageResponse<CommentDetailsResponse> actual =
                commentDetailsService.getPostCommentsDetails(POST_ID, pageRequest, true, 10);

        assertNotNull(actual);
        assertEquals(1, actual.getContent().size());
        verify(commentRepository).findByPostIdOrderByCreatedAtDesc(POST_ID, pageRequest.toPageable());
        verify(commentDetailsAggregator).aggregateCommentsPage(commentsPage, true, 10);
    }

    @Test
    @DisplayName("Получение деталей комментариев пользователя - успешно")
    void getUserCommentsDetails_whenUserExists_shouldReturnPageResponse() {
        PageRequest pageRequest = TestDataFactory.createPageRequest(0, 10);
        Comment comment = TestDataFactory.createCommentEntity(COMMENT_ID);
        List<Comment> comments = List.of(comment);
        Page<Comment> commentsPage = TestDataFactory.createPage(comments);
        CommentDetailsResponse detailsResponse = TestDataFactory.createCommentDetailsResponse(COMMENT_ID);
        Page<CommentDetailsResponse> detailsPage = TestDataFactory.createPage(List.of(detailsResponse));
        PageResponse<CommentDetailsResponse> expectedResponse =
                TestDataFactory.createPageResponse(List.of(detailsResponse));

        when(commentRepository.findByOwnerIdOrderByCreatedAtDesc(USER_ID, pageRequest.toPageable()))
                .thenReturn(commentsPage);
        when(commentDetailsAggregator.aggregateCommentsPage(commentsPage, false, 5))
                .thenReturn(detailsPage);

        PageResponse<CommentDetailsResponse> actual =
                commentDetailsService.getUserCommentsDetails(USER_ID, pageRequest, false, 5);

        assertNotNull(actual);
        assertEquals(1, actual.getContent().size());
        verify(commentRepository).findByOwnerIdOrderByCreatedAtDesc(USER_ID, pageRequest.toPageable());
        verify(commentDetailsAggregator).aggregateCommentsPage(commentsPage, false, 5);
    }

    @Test
    @DisplayName("Получение деталей комментариев поста - пустой результат")
    void getPostCommentsDetails_whenNoComments_shouldReturnEmptyPageResponse() {
        PageRequest pageRequest = TestDataFactory.createPageRequest(0, 10);
        Page<Comment> emptyPage = Page.empty();
        Page<CommentDetailsResponse> emptyDetailsPage = Page.empty();
        PageResponse<CommentDetailsResponse> expectedResponse =
                TestDataFactory.createPageResponse(List.of());

        when(commentRepository.findByPostIdOrderByCreatedAtDesc(POST_ID, pageRequest.toPageable()))
                .thenReturn(emptyPage);
        when(commentDetailsAggregator.aggregateCommentsPage(emptyPage, true, 10))
                .thenReturn(emptyDetailsPage);

        PageResponse<CommentDetailsResponse> actual =
                commentDetailsService.getPostCommentsDetails(POST_ID, pageRequest, true, 10);

        assertNotNull(actual);
        assertTrue(actual.getContent().isEmpty());
        verify(commentRepository).findByPostIdOrderByCreatedAtDesc(POST_ID, pageRequest.toPageable());
        verify(commentDetailsAggregator).aggregateCommentsPage(emptyPage, true, 10);
    }
}