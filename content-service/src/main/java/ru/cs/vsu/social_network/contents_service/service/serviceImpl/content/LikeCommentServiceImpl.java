package ru.cs.vsu.social_network.contents_service.service.serviceImpl.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cs.vsu.social_network.contents_service.dto.request.like.LikeCommentRequest;
import ru.cs.vsu.social_network.contents_service.dto.request.pageable.PageRequest;
import ru.cs.vsu.social_network.contents_service.dto.response.content.LikeCommentResponse;
import ru.cs.vsu.social_network.contents_service.dto.response.pageable.PageResponse;
import ru.cs.vsu.social_network.contents_service.entity.LikeComment;
import ru.cs.vsu.social_network.contents_service.exception.like.LikeAlreadyExistsException;
import ru.cs.vsu.social_network.contents_service.exception.like.LikeNotFoundException;
import ru.cs.vsu.social_network.contents_service.mapping.EntityMapper;
import ru.cs.vsu.social_network.contents_service.provider.LikeCommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.LikeCommentRepository;
import ru.cs.vsu.social_network.contents_service.service.content.LikeCommentService;
import ru.cs.vsu.social_network.contents_service.utils.MessageConstants;
import ru.cs.vsu.social_network.contents_service.utils.factory.content.LikeCommentFactory;
import ru.cs.vsu.social_network.contents_service.validation.LikeCommentValidator;

import java.util.UUID;

@Slf4j
@Service
public class LikeCommentServiceImpl implements LikeCommentService {
    private final EntityMapper mapper;
    private final LikeCommentRepository likeCommentRepository;
    private final LikeCommentFactory likeCommentFactory;
    private final LikeCommentValidator likeCommentValidator;
    private final LikeCommentEntityProvider likeCommentEntityProvider;

    public LikeCommentServiceImpl(EntityMapper mapper,
                                  LikeCommentRepository likeCommentRepository,
                                  LikeCommentFactory likeCommentFactory,
                                  LikeCommentValidator likeCommentValidator,
                                  LikeCommentEntityProvider likeCommentEntityProvider) {
        this.mapper = mapper;
        this.likeCommentRepository = likeCommentRepository;
        this.likeCommentFactory = likeCommentFactory;
        this.likeCommentValidator = likeCommentValidator;
        this.likeCommentEntityProvider = likeCommentEntityProvider;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public LikeCommentResponse create(UUID keycloakUserId,
                                      LikeCommentRequest likeCommentRequest) {
        log.info("ЛАЙК_КОММЕНТАРИЙ_СЕРВИС_СОЗДАНИЕ_НАЧАЛО: "
                        + "создание лайка для комментария с ID: {} пользователем: {}",
                likeCommentRequest.getCommentId(), keycloakUserId);

        if (likeCommentEntityProvider.existsByOwnerIdAndCommentId(keycloakUserId,
                likeCommentRequest.getCommentId())) {
            log.warn("ЛАЙК_КОММЕНТАРИЙ_СЕРВИС_СОЗДАНИЕ_ОШИБКА: "
                            + "лайк уже существует для комментария с ID: {} пользователем: {}",
                    likeCommentRequest.getCommentId(), keycloakUserId);
            throw new LikeAlreadyExistsException(MessageConstants.LIKE_ALREADY_EXISTS_FAILURE);
        }

        LikeComment likeComment =
                likeCommentFactory.create(keycloakUserId, likeCommentRequest);
        LikeComment savedLike = likeCommentRepository.save(likeComment);

        log.info("ЛАЙК_КОММЕНТАРИЙ_СЕРВИС_СОЗДАНИЕ_УСПЕХ: "
                        + "лайк создан с ID: {} для комментария с ID: {} пользователем: {}",
                savedLike.getId(), likeCommentRequest.getCommentId(), keycloakUserId);

        return mapper.map(savedLike, LikeCommentResponse.class);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public LikeCommentResponse delete(UUID keycloakUserId,
                                      LikeCommentRequest likeCommentRequest) {
        log.info("ЛАЙК_КОММЕНТАРИЙ_СЕРВИС_УДАЛЕНИЕ_НАЧАЛО: "
                        + "удаление лайка с комментария с ID: {} пользователем: {}",
                likeCommentRequest.getCommentId(), keycloakUserId);

        LikeComment likeComment =
                likeCommentEntityProvider.findByOwnerIdAndCommentId(keycloakUserId,
                                likeCommentRequest.getCommentId())
                .orElseThrow(() -> {
                    log.error("ЛАЙК_КОММЕНТАРИЙ_СЕРВИС_УДАЛЕНИЕ_ОШИБКА: "
                                    + "лайк не найден для комментария с ID: {} пользователем: {}",
                            likeCommentRequest.getCommentId(), keycloakUserId);
                    return new LikeNotFoundException(MessageConstants.LIKE_NOT_FOUND_FAILURE);
                });

        likeCommentValidator.validateOwnership(keycloakUserId, likeComment.getId());
        likeCommentRepository.delete(likeComment);

        log.info("ЛАЙК_КОММЕНТАРИЙ_СЕРВИС_УДАЛЕНИЕ_УСПЕХ: "
                        + "лайк удален с ID: {} с комментария с ID: {} пользователем: {}",
                likeComment.getId(), likeCommentRequest.getCommentId(), keycloakUserId);

        return mapper.map(likeComment, LikeCommentResponse.class);
    }

    /** {@inheritDoc} */
    @Override
    public PageResponse<LikeCommentResponse> getAllLikesByComment(UUID commentId,
                                                                  PageRequest pageRequest) {
        log.info("ЛАЙК_КОММЕНТАРИЙ_СЕРВИС_ПОЛУЧЕНИЕ_КОММЕНТАРИЙ_НАЧАЛО: "
                        + "запрос лайков для комментария с ID: {}, страница: {}, размер: {}",
                commentId, pageRequest.getPageNumber(), pageRequest.getSize());

        final Page<LikeComment> likesPage =
                likeCommentRepository.findAllByCommentId(commentId,
                        pageRequest.toPageable());

        log.info("ЛАЙК_КОММЕНТАРИЙ_СЕРВИС_ПОЛУЧЕНИЕ_КОММЕНТАРИЙ_УСПЕХ: "
                        + "найдено {} лайков для комментария с ID: {}, всего страниц: {}",
                likesPage.getTotalElements(), commentId, likesPage.getTotalPages());

        return PageResponse.of(likesPage.map(
                like -> mapper.map(like, LikeCommentResponse.class)));
    }

    /** {@inheritDoc} */
    @Override
    public Long getLikesCountByComment(UUID commentId) {
        log.info("ЛАЙК_КОММЕНТАРИЙ_СЕРВИС_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_НАЧАЛО: "
                + "запрос количества лайков для комментария с ID: {}", commentId);

        final Long count = likeCommentEntityProvider.getLikesCountByComment(commentId);

        log.info("ЛАЙК_КОММЕНТАРИЙ_СЕРВИС_ПОЛУЧЕНИЕ_КОЛИЧЕСТВА_УСПЕХ: "
                + "найдено {} лайков для комментария с ID: {}", count, commentId);

        return count;
    }
}