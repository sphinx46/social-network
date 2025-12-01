package ru.cs.vsu.social_network.contents_service.service.serviceImpl.content;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.PostDetailsResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.contents_service.entity.Post;
import ru.cs.vsu.social_network.contents_service.provider.PostEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.PostRepository;
import ru.cs.vsu.social_network.contents_service.service.aggregator.PostDetailsAggregator;
import ru.cs.vsu.social_network.contents_service.service.serviceImpl.content.PostDetailsServiceImpl;
import ru.cs.vsu.social_network.contents_service.utils.TestDataFactory;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для {@link PostDetailsServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class PostDetailsServiceImplTest {

    private static final UUID USER_ID = UUID.fromString("3fc4530f-5c0c-429d-8a64-810ce3bd1415");
    private static final UUID POST_ID = UUID.fromString("8dce7d87-60d9-4b66-8851-e7dba4684538");

    @Mock
    private PostRepository postRepository;
    @Mock
    private PostEntityProvider postEntityProvider;
    @Mock
    private PostDetailsAggregator postDetailsAggregator;

    @InjectMocks
    private PostDetailsServiceImpl postDetailsService;

    @Test
    @DisplayName("Получение деталей поста - успешно с комментариями и лайками")
    void getPostDetails_whenIncludeCommentsAndLikes_shouldReturnResponse() {
        Post post = TestDataFactory.createPostEntity(POST_ID);
        PostDetailsResponse expectedResponse = TestDataFactory.createPostDetailsResponse(POST_ID);

        when(postEntityProvider.getById(POST_ID)).thenReturn(post);
        when(postDetailsAggregator.aggregatePostDetails(post, true, true, 10, 5))
                .thenReturn(expectedResponse);

        PostDetailsResponse actual = postDetailsService.getPostDetails(POST_ID, true, true, 10, 5);

        assertEquals(expectedResponse, actual);
        verify(postEntityProvider).getById(POST_ID);
        verify(postDetailsAggregator).aggregatePostDetails(post, true, true, 10, 5);
    }

    @Test
    @DisplayName("Получение деталей поста - успешно без комментариев и лайков")
    void getPostDetails_whenExcludeCommentsAndLikes_shouldReturnResponse() {
        Post post = TestDataFactory.createPostEntity(POST_ID);
        PostDetailsResponse expectedResponse = TestDataFactory.createPostDetailsResponse(POST_ID);

        when(postEntityProvider.getById(POST_ID)).thenReturn(post);
        when(postDetailsAggregator.aggregatePostDetails(post, false, false, 0, 0))
                .thenReturn(expectedResponse);

        PostDetailsResponse actual = postDetailsService.getPostDetails(POST_ID, false, false, 0, 0);

        assertEquals(expectedResponse, actual);
        verify(postEntityProvider).getById(POST_ID);
        verify(postDetailsAggregator).aggregatePostDetails(post, false, false, 0, 0);
    }

    @Test
    @DisplayName("Получение деталей постов пользователя - успешно")
    void getUserPostsDetails_whenUserExists_shouldReturnPageResponse() {
        PageRequest pageRequest = TestDataFactory.createPageRequest(0, 10);
        Post post = TestDataFactory.createPostEntity(POST_ID);
        List<Post> posts = List.of(post);
        Page<Post> postsPage = TestDataFactory.createPage(posts);
        PostDetailsResponse detailsResponse = TestDataFactory.createPostDetailsResponse(POST_ID);
        Page<PostDetailsResponse> detailsPage = TestDataFactory.createPage(List.of(detailsResponse));

        when(postRepository.findAllByOwnerId(USER_ID, pageRequest.toPageable()))
                .thenReturn(postsPage);
        when(postDetailsAggregator.aggregatePostsPage(postsPage, true, true, 10, 5))
                .thenReturn(detailsPage);

        PageResponse<PostDetailsResponse> actual =
                postDetailsService.getUserPostsDetails(USER_ID, pageRequest, true, true, 10, 5);

        assertNotNull(actual);
        assertEquals(1, actual.getContent().size());
        verify(postRepository).findAllByOwnerId(USER_ID, pageRequest.toPageable());
        verify(postDetailsAggregator).aggregatePostsPage(postsPage, true, true, 10, 5);
    }

    @Test
    @DisplayName("Получение деталей всех постов - успешно")
    void getAllPostsDetails_whenPostsExist_shouldReturnPageResponse() {
        PageRequest pageRequest = TestDataFactory.createPageRequest(0, 10);
        Post post = TestDataFactory.createPostEntity(POST_ID);
        List<Post> posts = List.of(post);
        Page<Post> postsPage = TestDataFactory.createPage(posts);
        PostDetailsResponse detailsResponse = TestDataFactory.createPostDetailsResponse(POST_ID);
        Page<PostDetailsResponse> detailsPage = TestDataFactory.createPage(List.of(detailsResponse));

        when(postRepository.findAll(pageRequest.toPageable())).thenReturn(postsPage);
        when(postDetailsAggregator.aggregatePostsPage(postsPage, false, true, 5, 10))
                .thenReturn(detailsPage);

        PageResponse<PostDetailsResponse> actual =
                postDetailsService.getAllPostsDetails(pageRequest, false, true, 5, 10);

        assertNotNull(actual);
        assertEquals(1, actual.getContent().size());
        verify(postRepository).findAll(pageRequest.toPageable());
        verify(postDetailsAggregator).aggregatePostsPage(postsPage, false, true, 5, 10);
    }

    @Test
    @DisplayName("Получение деталей постов пользователя - пустой результат")
    void getUserPostsDetails_whenNoPosts_shouldReturnEmptyPageResponse() {
        PageRequest pageRequest = TestDataFactory.createPageRequest(0, 10);
        Page<Post> emptyPage = Page.empty();
        Page<PostDetailsResponse> emptyDetailsPage = Page.empty();

        when(postRepository.findAllByOwnerId(USER_ID, pageRequest.toPageable()))
                .thenReturn(emptyPage);
        when(postDetailsAggregator.aggregatePostsPage(emptyPage, true, false, 5, 0))
                .thenReturn(emptyDetailsPage);

        PageResponse<PostDetailsResponse> actual =
                postDetailsService.getUserPostsDetails(USER_ID, pageRequest, true, false, 5, 0);

        assertNotNull(actual);
        assertTrue(actual.getContent().isEmpty());
        verify(postRepository).findAllByOwnerId(USER_ID, pageRequest.toPageable());
        verify(postDetailsAggregator).aggregatePostsPage(emptyPage, true, false, 5, 0);
    }

    @Test
    @DisplayName("Получение деталей всех постов - пустой результат")
    void getAllPostsDetails_whenNoPosts_shouldReturnEmptyPageResponse() {
        PageRequest pageRequest = TestDataFactory.createPageRequest(0, 10);
        Page<Post> emptyPage = Page.empty();
        Page<PostDetailsResponse> emptyDetailsPage = Page.empty();

        when(postRepository.findAll(pageRequest.toPageable())).thenReturn(emptyPage);
        when(postDetailsAggregator.aggregatePostsPage(emptyPage, true, true, 10, 10))
                .thenReturn(emptyDetailsPage);

        PageResponse<PostDetailsResponse> actual =
                postDetailsService.getAllPostsDetails(pageRequest, true, true, 10, 10);

        assertNotNull(actual);
        assertTrue(actual.getContent().isEmpty());
        verify(postRepository).findAll(pageRequest.toPageable());
        verify(postDetailsAggregator).aggregatePostsPage(emptyPage, true, true, 10, 10);
    }
}