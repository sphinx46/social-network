package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.relationship;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.common.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.relationship.RelationshipRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.common.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.profile.ProfileResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.relationship.RelationshipResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Profile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Relationship;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.FriendshipStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.notification.NotificationEventPublisherService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.relationship.RelationshipNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.RelationshipRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.relationship.RelationshipService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.RelationshipFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.RelationshipValidator;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RelationshipServiceImpl  implements RelationshipService {
    private final RelationshipRepository relationshipRepository;
    private final EntityUtils entityUtils;
    private final EntityMapper entityMapper;
    private final RelationshipFactory relationshipFactory;
    private final RelationshipValidator relationshipValidator;
    private final NotificationEventPublisherService notificationEventPublisherService;

    /**
     * Отправляет запрос на дружбу другому пользователю
     *
     * @param request запрос на установление отношений
     * @param currentUser текущий пользователь, отправляющий запрос
     * @return ответ с информацией о созданных отношениях
     */
    @Override
    public RelationshipResponse sendFriendRequest(RelationshipRequest request, User currentUser) {
        relationshipValidator.validate(request, currentUser);

        User receiver = entityUtils.getUser(request.getTargetUserId());

        Relationship relationship = relationshipFactory.createPendingRelationship(currentUser, receiver);
        Relationship savedRelationship = relationshipRepository.save(relationship);

        notificationEventPublisherService.publishFriendRequest(this, request.getTargetUserId(), currentUser.getId());

        return entityMapper.map(savedRelationship, RelationshipResponse.class);
    }

    /**
     * Получает список друзей пользователя
     *
     * @param currentUser текущий пользователь
     * @param pageRequest параметры пагинации
     * @return страница с списком друзей
     */
    @Override
    public PageResponse<RelationshipResponse> getFriendList(User currentUser, PageRequest pageRequest) {
        return getRelationshipsByStatus(currentUser, FriendshipStatus.ACCEPTED, pageRequest);
    }

    /**
     * Получает черный список пользователя (заблокированные пользователи)
     *
     * @param currentUser текущий пользователь
     * @param pageRequest параметры пагинации
     * @return страница с черным списком
     */
    @Override
    public PageResponse<RelationshipResponse> getBlackList(User currentUser, PageRequest pageRequest) {
        return getRelationshipsByStatus(currentUser, FriendshipStatus.BLOCKED, pageRequest);
    }

    /**
     * Получает список отклоненных запросов на дружбу
     *
     * @param currentUser текущий пользователь
     * @param pageRequest параметры пагинации
     * @return страница с отклоненными запросами
     */
    @Override
    public PageResponse<RelationshipResponse> getDeclinedList(User currentUser, PageRequest pageRequest) {
        return getRelationshipsByStatus(currentUser, FriendshipStatus.DECLINED, pageRequest);
    }

    /**
     * Блокирует пользователя
     *
     * @param request запрос на блокировку
     * @param currentUser текущий пользователь
     * @return ответ с информацией о заблокированных отношениях
     */
    @Override
    public RelationshipResponse blockUser(RelationshipRequest request, User currentUser) {
        relationshipValidator.validateBlockUser(request, currentUser);

        User targetUser = entityUtils.getUser(request.getTargetUserId());

        Relationship relationship = relationshipRepository
                .findRelationshipBetweenUsers(currentUser.getId(), targetUser.getId())
                .map(existing -> updateRelationshipStatus(existing, FriendshipStatus.BLOCKED))
                .orElseGet(() -> relationshipFactory.createBlockedRelationship(currentUser, targetUser));

        Relationship savedRelationship = relationshipRepository.save(relationship);
        return entityMapper.map(savedRelationship, RelationshipResponse.class);
    }

    /**
     * Принимает запрос на дружбу
     *
     * @param request запрос на принятие дружбы
     * @param currentUser текущий пользователь
     * @return ответ с информацией о принятых отношениях
     */
    @Override
    public RelationshipResponse acceptFriendRequest(RelationshipRequest request, User currentUser) {
        relationshipValidator.validateStatusChange(request, currentUser);

        notificationEventPublisherService.publishFriendRequestAccepted(this, request.getTargetUserId(), currentUser.getId());

        return changeRelationshipStatus(request, FriendshipStatus.ACCEPTED, currentUser);
    }

    /**
     * Отклоняет запрос на дружбу
     *
     * @param request запрос на отклонение дружбы
     * @param currentUser текущий пользователь
     * @return ответ с информацией об отклоненных отношениях
     */
    @Override
    public RelationshipResponse declineFriendRequest(RelationshipRequest request, User currentUser) {
        relationshipValidator.validateStatusChange(request, currentUser);

        return changeRelationshipStatus(request, FriendshipStatus.DECLINED, currentUser);
    }


    @Override
    public PageResponse<ProfileResponse> findFriendsCandidates(User currentUser, PageRequest pageRequest) {
        Page<Profile> friendsCandidates = relationshipRepository.findFriendsCandidates(currentUser.getId(),
                currentUser.getCity(), pageRequest.toPageable());
        return PageResponse.of(friendsCandidates.map(
                friendsCandidate -> entityMapper.map(friendsCandidate, ProfileResponse.class)
        ));

    }

    /**
     * Получает отношения по статусу для пользователя
     *
     * @param currentUser текущий пользователь
     * @param status статус отношений
     * @param pageRequest параметры пагинации
     * @return страница с отношениями указанного статуса
     */
    private PageResponse<RelationshipResponse> getRelationshipsByStatus(User currentUser, FriendshipStatus status, PageRequest pageRequest) {
        Page<Relationship> relationships = relationshipRepository.findByUserAndStatus(currentUser.getId(), status, pageRequest.toPageable());
        return PageResponse.of(relationships.map(
                relationship -> entityMapper.map(relationship, RelationshipResponse.class)
        ));
    }

    /**
     * Изменяет статус отношений
     *
     * @param request запрос на изменение статуса
     * @param status новый статус отношений
     * @param currentUser текущий пользователь
     * @return ответ с информацией об измененных отношениях
     */
    private RelationshipResponse changeRelationshipStatus(RelationshipRequest request, FriendshipStatus status, User currentUser) {
        Relationship relationship = relationshipRepository
                .findBySenderIdAndReceiverIdAndStatus(request.getTargetUserId(), currentUser.getId(), FriendshipStatus.PENDING)
                .orElseThrow(() -> new RelationshipNotFoundException(ResponseMessageConstants.FAILURE_RELATIONSHIP_NOT_FOUND));

        relationship.setStatus(status);
        relationship.setUpdatedAt(LocalDateTime.now());
        Relationship savedRelationship = relationshipRepository.save(relationship);
        return entityMapper.map(savedRelationship, RelationshipResponse.class);
    }

    /**
     * Обновляет статус отношений
     *
     * @param relationship сущность отношений
     * @param status новый статус
     * @return обновленная сущность отношений
     */
    private Relationship updateRelationshipStatus(Relationship relationship, FriendshipStatus status) {
        relationship.setStatus(status);
        relationship.setUpdatedAt(LocalDateTime.now());
        return relationship;
    }
}