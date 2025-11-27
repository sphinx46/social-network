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
import ru.cs.vsu.social_network.contents_service.dto.response.content.PostResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.contents_service.entity.Post;
import ru.cs.vsu.social_network.contents_service.exception.post.PostUploadImageException;
import ru.cs.vsu.social_network.contents_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.contents_service.provider.PostEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.PostRepository;
import ru.cs.vsu.social_network.contents_service.service.PostService;
import ru.cs.vsu.social_network.contents_service.utils.MessageConstants;
import ru.cs.vsu.social_network.contents_service.utils.factory.PostFactory;
import ru.cs.vsu.social_network.contents_service.validation.PostValidator;

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
    private final PostValidator postValidator;

    public PostServiceImpl(EntityMapper mapper, PostRepository postRepository,
                           PostFactory postFactory, PostEntityProvider postEntityProvider,
                           PostValidator postValidator) {
        this.mapper = mapper;
        this.postRepository = postRepository;
        this.postFactory = postFactory;
        this.postEntityProvider = postEntityProvider;
        this.postValidator = postValidator;
    }

    /** {@inheritDoc} */
    @Override
    public PostResponse create(final UUID keycloakUserId, final PostCreateRequest request) {
        log.info("ПОСТ_СЕРВИС_СОЗДАНИЕ_НАЧАЛО: " +
                        "создание поста для пользователя: {}, длина контента: {}",
                keycloakUserId, request.getContent().length());

        Post post = postFactory.create(keycloakUserId, request);
        Post savedPost = postRepository.save(post);

        log.info("ПОСТ_СЕРВИС_СОЗДАНИЕ_УСПЕХ: " +
                        "пост успешно создан с ID: {} для пользователя: {}",
                savedPost.getId(), keycloakUserId);

        return mapper.map(savedPost, PostResponse.class);
    }

    /** {@inheritDoc} */
    @Override
    public PostResponse editPost(final UUID keycloakUserId,
                                 final PostEditRequest request) {
        log.info("ПОСТ_СЕРВИС_РЕДАКТИРОВАНИЕ_НАЧАЛО: " +
                        "редактирование поста с ID: {} пользователем: {}, " +
                        "новая длина контента: {}",
                request.getPostId(), keycloakUserId, request.getContent().length());

        postValidator.validateOwnership(keycloakUserId, request.getPostId());

        Post post = postEntityProvider.getById(request.getPostId());
        post.setContent(request.getContent());
        Post updatedPost = postRepository.save(post);

        log.info("ПОСТ_СЕРВИС_РЕДАКТИРОВАНИЕ_УСПЕХ: " +
                        "пост с ID: {} успешно обновлен пользователем: {}",
                request.getPostId(), keycloakUserId);

        return mapper.map(updatedPost, PostResponse.class);
    }

    /** {@inheritDoc} */
    @Override
    public PostResponse uploadImage(final UUID keycloakUserId,
                                    final PostUploadImageRequest request) {
        log.info("ПОСТ_СЕРВИС_ЗАГРУЗКА_ИЗОБРАЖЕНИЯ_НАЧАЛО: " +
                        "загрузка изображения для поста с ID: {} пользователем: {}",
                request.getPostId(), keycloakUserId);

        postValidator.validateOwnership(keycloakUserId, request.getPostId());

        Post post = postEntityProvider.getById(request.getPostId());
        String imageUrl = request.getImageUrl();

        if (!StringUtils.hasText(imageUrl)) {
            log.error("ПОСТ_СЕРВИС_ЗАГРУЗКА_ИЗОБРАЖЕНИЯ_ОШИБКА: " +
                            "URL изображения пустой для поста с ID: {}, пользователь: {}",
                    request.getPostId(), keycloakUserId);
            throw new PostUploadImageException(MessageConstants.POST_UPLOAD_IMAGE_FAILURE);
        }

        imageUrl = imageUrl.trim();
        post.setImageUrl(imageUrl);
        Post updatedPost = postRepository.save(post);

        log.info("ПОСТ_СЕРВИС_ЗАГРУЗКА_ИЗОБРАЖЕНИЯ_УСПЕХ: " +
                        "изображение загружено для поста с ID: {}, URL: {}",
                request.getPostId(), imageUrl);

        return mapper.map(updatedPost, PostResponse.class);
    }

    /** {@inheritDoc} */
    @Override
    public PostResponse removeImage(final UUID keycloakUserId,
                                    final PostRemoveImageRequest request) {
        log.info("ПОСТ_СЕРВИС_УДАЛЕНИЕ_ИЗОБРАЖЕНИЯ_НАЧАЛО: " +
                        "удаление изображения у поста с ID: {} пользователем: {}",
                request.getPostId(), keycloakUserId);

        postValidator.validateOwnership(keycloakUserId, request.getPostId());

        Post post = postEntityProvider.getById(request.getPostId());
        post.setImageUrl(null);
        Post updatedPost = postRepository.save(post);

        log.info("ПОСТ_СЕРВИС_УДАЛЕНИЕ_ИЗОБРАЖЕНИЯ_УСПЕХ: " +
                        "изображение удалено у поста с ID: {} пользователем: {}",
                request.getPostId(), keycloakUserId);

        return mapper.map(updatedPost, PostResponse.class);
    }

    /** {@inheritDoc} */
    @Override
    public PostResponse getPostById(final UUID postId) {
        log.info("ПОСТ_СЕРВИС_ПОЛУЧЕНИЕ_ПО_ID_НАЧАЛО: запрос поста с ID: {}", postId);

        Post post = postEntityProvider.getById(postId);

        log.info("ПОСТ_СЕРВИС_ПОЛУЧЕНИЕ_ПО_ID_УСПЕХ: " +
                "пост с ID: {} найден, владелец: {}", postId, post.getOwnerId());

        return mapper.map(post, PostResponse.class);
    }

    /** {@inheritDoc} */
    @Override
    public PageResponse<PostResponse> getAllPostsByUser(final UUID keycloakUserId,
                                                        final PageRequest pageRequest) {
        log.info("ПОСТ_СЕРВИС_ПОЛУЧЕНИЕ_ПОЛЬЗОВАТЕЛЯ_НАЧАЛО: " +
                        "запрос постов пользователя: {}, " +
                        "страница: {}, размер: {}, сортировка: {}",
                keycloakUserId, pageRequest.getPageNumber(),
                pageRequest.getSize(), pageRequest.getSortBy());

        Pageable pageable = pageRequest.toPageable();
        Page<Post> postsPage = postRepository.findAllByOwnerId(keycloakUserId, pageable);

        log.info("ПОСТ_СЕРВИС_ПОЛУЧЕНИЕ_ПОЛЬЗОВАТЕЛЯ_УСПЕХ: " +
                        "найдено {} постов для пользователя: {}, всего страниц: {}",
                postsPage.getTotalElements(), keycloakUserId, postsPage.getTotalPages());

        return PageResponse.of(postsPage.map(
                post -> mapper.map(post, PostResponse.class)));
    }
}