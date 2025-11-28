package ru.cs.vsu.social_network.contents_service.service.serviceImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cs.vsu.social_network.contents_service.dto.request.like.LikePostRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.LikePostResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.contents_service.entity.LikePost;
import ru.cs.vsu.social_network.contents_service.exception.like.LikeNotFoundException;
import ru.cs.vsu.social_network.contents_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.contents_service.provider.LikePostEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.LikePostRepository;
import ru.cs.vsu.social_network.contents_service.service.LikePostService;
import ru.cs.vsu.social_network.contents_service.utils.MessageConstants;
import ru.cs.vsu.social_network.contents_service.utils.factory.LikePostFactory;
import ru.cs.vsu.social_network.contents_service.validation.LikePostValidator;

import java.util.UUID;

@Slf4j
@Service
public class LikePostServiceImpl implements LikePostService {
    private final EntityMapper mapper;
    private final LikePostRepository likePostRepository;
    private final LikePostFactory likePostFactory;
    private final LikePostValidator likePostValidator;
    private final LikePostEntityProvider likePostEntityProvider;

    public LikePostServiceImpl(EntityMapper mapper,
                               LikePostRepository likePostRepository,
                               LikePostFactory likePostFactory,
                               LikePostValidator likePostValidator,
                               LikePostEntityProvider likePostEntityProvider) {
        this.mapper = mapper;
        this.likePostRepository = likePostRepository;
        this.likePostFactory = likePostFactory;
        this.likePostValidator = likePostValidator;
        this.likePostEntityProvider = likePostEntityProvider;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public LikePostResponse create(UUID keycloakUserId,
                                   LikePostRequest likePostRequest) {
        log.info("ЛАЙК_ПОСТ_СЕРВИС_СОЗДАНИЕ_НАЧАЛО: "
                        + "создание лайка для поста с ID: {} пользователем: {}",
                likePostRequest.getPostId(), keycloakUserId);

        if (likePostEntityProvider.existsByOwnerIdAndPostId(keycloakUserId,
                likePostRequest.getPostId())) {
            log.warn("ЛАЙК_ПОСТ_СЕРВИС_СОЗДАНИЕ_ОШИБКА: "
                            + "лайк уже существует для поста с ID: {} пользователем: {}",
                    likePostRequest.getPostId(), keycloakUserId);
            throw new RuntimeException("Лайк уже существует");
        }

        LikePost likePost = likePostFactory.create(keycloakUserId, likePostRequest);
        LikePost savedLike = likePostRepository.save(likePost);

        log.info("ЛАЙК_ПОСТ_СЕРВИС_СОЗДАНИЕ_УСПЕХ: "
                        + "лайк создан с ID: {} для поста с ID: {} пользователем: {}",
                savedLike.getId(), likePostRequest.getPostId(), keycloakUserId);

        return mapper.map(savedLike, LikePostResponse.class);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public LikePostResponse delete(UUID keycloakUserId,
                                   LikePostRequest likePostRequest) {
        log.info("ЛАЙК_ПОСТ_СЕРВИС_УДАЛЕНИЕ_НАЧАЛО: "
                        + "удаление лайка с поста с ID: {} пользователем: {}",
                likePostRequest.getPostId(), keycloakUserId);

        LikePost likePost =
                likePostEntityProvider.findByOwnerIdAndPostId(keycloakUserId,
                                likePostRequest.getPostId())
                .orElseThrow(() -> {
                    log.error("ЛАЙК_ПОСТ_СЕРВИС_УДАЛЕНИЕ_ОШИБКА: "
                                    + "лайк не найден для поста с ID: {} пользователем: {}",
                            likePostRequest.getPostId(), keycloakUserId);
                    return new LikeNotFoundException(MessageConstants.LIKE_NOT_FOUND_FAILURE);
                });

        likePostValidator.validateOwnership(keycloakUserId, likePost.getId());
        likePostRepository.delete(likePost);

        log.info("ЛАЙК_ПОСТ_СЕРВИС_УДАЛЕНИЕ_УСПЕХ: "
                        + "лайк удален с ID: {} с поста с ID: {} пользователем: {}",
                likePost.getId(), likePostRequest.getPostId(), keycloakUserId);

        return mapper.map(likePost, LikePostResponse.class);
    }

    /** {@inheritDoc} */
    @Override
    public PageResponse<LikePostResponse> getAllLikesByPost(UUID postId,
                                                            PageRequest pageRequest) {
        log.info("ЛАЙК_ПОСТ_СЕРВИС_ПОЛУЧЕНИЕ_ПОСТ_НАЧАЛО: "
                        + "запрос лайков для поста с ID: {}, страница: {}, размер: {}",
                postId, pageRequest.getPageNumber(), pageRequest.getSize());

        final Page<LikePost> likesPage =
                likePostRepository.findAllByPostId(postId, pageRequest.toPageable());

        log.info("ЛАЙК_ПОСТ_СЕРВИС_ПОЛУЧЕНИЕ_ПОСТ_УСПЕХ: "
                        + "найдено {} лайков для поста с ID: {}, всего страниц: {}",
                likesPage.getTotalElements(), postId, likesPage.getTotalPages());

        return PageResponse.of(likesPage.map(
                like -> mapper.map(like, LikePostResponse.class)));
    }

    /** {@inheritDoc} */
    @Override
    public Long getLikesCountByPost(UUID postId) {
        log.info("ЛАЙК_ПОСТ_СЕРВИС_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_НАЧАЛО: "
                + "запрос количества лайков для поста с ID: {}", postId);

        final Long count = likePostEntityProvider.getLikesCountByPost(postId);

        log.info("ЛАЙК_ПОСТ_СЕРВИС_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_УСПЕХ: "
                + "найдено {} лайков для поста с ID: {}", count, postId);

        return count;
    }
}