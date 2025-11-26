package ru.cs.vsu.social_network.contents_service.service.serviceImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.post.PostCreateRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.post.PostEditRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.post.PostRemoveImageRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.post.PostUploadImageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.post.PostResponse;
import ru.cs.vsu.social_network.contents_service.entity.Post;
import ru.cs.vsu.social_network.contents_service.exception.PostUploadImageException;
import ru.cs.vsu.social_network.contents_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.contents_service.provider.PostEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.PostRepository;
import ru.cs.vsu.social_network.contents_service.service.PostService;
import ru.cs.vsu.social_network.contents_service.utils.MessageConstants;
import ru.cs.vsu.social_network.contents_service.utils.factory.PostFactory;

import java.util.UUID;

/**
 * Реализация сервиса для работы с постами.
 * Обеспечивает бизнес-логику создания, редактирования и управления постами.
 */
@Slf4j
@Service
public class PostServiceImpl implements PostService {
    private final EntityMapper mapper;
    private final PostRepository postRepository;
    private final PostFactory postFactory;
    private final PostEntityProvider postEntityProvider;

    public PostServiceImpl(EntityMapper mapper, PostRepository postRepository, PostFactory postFactory, PostEntityProvider postEntityProvider) {
        this.mapper = mapper;
        this.postRepository = postRepository;
        this.postFactory = postFactory;
        this.postEntityProvider = postEntityProvider;
    }

    /** {@inheritDoc} */
    @Override
    public PostResponse create(final UUID keycloakUserId, final PostCreateRequest request) {
        log.info("ПОСТ_СЕРВИС_СОЗДАНИЕ_НАЧАЛО: создание поста для пользователя: {}", keycloakUserId);

        Post post = postFactory.create(keycloakUserId, request);
        postRepository.save(post);

        log.info("ПОСТ_СЕРВИС_СОЗДАНИЕ_УСПЕХ: пост успешно создан с ID: {}", post.getId());
        return mapper.map(post, PostResponse.class);
    }

    /** {@inheritDoc} */
    @Override
    public PostResponse editPost(final PostEditRequest request) {
        log.info("ПОСТ_СЕРВИС_РЕДАКТИРОВАНИЕ_НАЧАЛО: редактирование поста с ID: {}", request.getId());

        Post post = postEntityProvider.getById(request.getId());
        post.setContent(request.getContent());
        Post updatedPost = postRepository.save(post);

        log.info("ПОСТ_СЕРВИС_РЕДАКТИРОВАНИЕ_УСПЕХ: пост с ID: {} успешно обновлен", request.getId());
        return mapper.map(updatedPost, PostResponse.class);
    }

    /** {@inheritDoc} */
    @Override
    public PostResponse uploadImage(final PostUploadImageRequest request) {
        log.info("ПОСТ_СЕРВИС_ЗАГРУЗКА_ИЗОБРАЖЕНИЯ_НАЧАЛО: загрузка изображения для поста с ID: {}", request.getPostId());

        Post post = postEntityProvider.getById(request.getPostId());
        String imageUrl = request.getImageUrl();

        if (!StringUtils.hasText(imageUrl)) {
            log.error("ПОСТ_СЕРВИС_ЗАГРУЗКА_ИЗОБРАЖЕНИЯ_ОШИБКА: URL изображения пустой для поста с ID: {}", request.getPostId());
            throw new PostUploadImageException(MessageConstants.POST_UPLOAD_IMAGE_FAILURE);
        }

        imageUrl = imageUrl.trim();
        post.setImageUrl(imageUrl);
        Post updatedPost = postRepository.save(post);

        log.info("ПОСТ_СЕРВИС_ЗАГРУЗКА_ИЗОБРАЖЕНИЯ_УСПЕХ: изображение загружено для поста с ID: {}", request.getPostId());
        return mapper.map(updatedPost, PostResponse.class);
    }

    /** {@inheritDoc} */
    @Override
    public PostResponse removeImage(final PostRemoveImageRequest request) {
        log.info("ПОСТ_СЕРВИС_УДАЛЕНИЕ_ИЗОБРАЖЕНИЯ_НАЧАЛО: удаление изображения у поста с ID: {}", request.getPostId());

        Post post = postEntityProvider.getById(request.getPostId());
        post.setImageUrl(null);
        Post updatedPost = postRepository.save(post);

        log.info("ПОСТ_СЕРВИС_УДАЛЕНИЕ_ИЗОБРАЖЕНИЯ_УСПЕХ: изображение удалено у поста с ID: {}", request.getPostId());
        return mapper.map(updatedPost, PostResponse.class);
    }

    /** {@inheritDoc} */
    @Override
    public PostResponse getPostById(UUID postId) {
        log.info("ПОСТ_СЕРВИС_ПОЛУЧЕНИЕ_ПО_ID_НАЧАЛО: запрос поста с ID: {}", postId);

        Post post = postEntityProvider.getById(postId);

        log.info("ПОСТ_СЕРВИС_ПОЛУЧЕНИЕ_ПО_ID_УСПЕХ: пост с ID: {} найден", postId);
        return mapper.map(post, PostResponse.class);
    }

    /** {@inheritDoc} */
    @Override
    public PageResponse<PostResponse> getAllPostsByUser(final UUID keycloakUserId,
                                                        final PageRequest pageRequest) {
        log.info("ПОСТ_СЕРВИС_ПОЛУЧЕНИЕ_ПОЛЬЗОВАТЕЛЯ_НАЧАЛО: запрос постов пользователя: {}, страница: {}, размер: {}",
                keycloakUserId, pageRequest.getPageNumber(), pageRequest.getSize());

        Pageable pageable = pageRequest.toPageable();
        Page<Post> postsPage = postRepository.findAllByOwnerId(keycloakUserId, pageable);

        log.info("ПОСТ_СЕРВИС_ПОЛУЧЕНИЕ_ПОЛЬЗОВАТЕЛЯ_УСПЕХ: найдено {} постов для пользователя: {}",
                postsPage.getTotalElements(), keycloakUserId);
        return PageResponse.of(postsPage.map(
                post -> mapper.map(post, PostResponse.class)));
    }
}