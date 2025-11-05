package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.relationship;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.common.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.relationship.RelationshipRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.common.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.relationship.RelationshipResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Relationship;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.FriendshipStatus;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.notification.NotificationEventPublisherService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.relationship.RelationshipNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.CentralLogger;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.RelationshipRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.relationship.RelationshipService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.RelationshipFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.RelationshipValidator;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RelationshipServiceImpl  implements RelationshipService {
    private final RelationshipRepository relationshipRepository;
    private final EntityUtils entityUtils;
    private final EntityMapper entityMapper;
    private final RelationshipFactory relationshipFactory;
    private final RelationshipValidator relationshipValidator;
    private final NotificationEventPublisherService notificationEventPublisherService;
    private final CentralLogger centralLogger;

    /**
     * Отправляет запрос на дружбу другому пользователю
     *
     * @param request запрос на установление отношений
     * @param currentUser текущий пользователь, отправляющий запрос
     * @return ответ с информацией о созданных отношениях
     */
    @Override
    public RelationshipResponse sendFriendRequest(RelationshipRequest request, User currentUser) {
        Map<String, Object> context = new HashMap<>();
        context.put("senderId", currentUser.getId());
        context.put("targetUserId", request.getTargetUserId());

        centralLogger.logInfo("ЗАПРОС_ДРУЖБЫ_ОТПРАВКА",
                "Отправка запроса на дружбу", context);

        try {
            relationshipValidator.validate(request, currentUser);

            User receiver = entityUtils.getUser(request.getTargetUserId());

            Relationship relationship = relationshipFactory.createPendingRelationship(currentUser, receiver);
            Relationship savedRelationship = relationshipRepository.save(relationship);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("relationshipId", savedRelationship.getId());
            successContext.put("status", savedRelationship.getStatus());

            centralLogger.logInfo("ЗАПРОС_ДРУЖБЫ_ОТПРАВЛЕН",
                    "Запрос на дружбу успешно отправлен", successContext);

            notificationEventPublisherService.publishFriendRequest(this, request.getTargetUserId(), currentUser.getId());

            return entityMapper.map(savedRelationship, RelationshipResponse.class);
        } catch (Exception e) {
            centralLogger.logError("ЗАПРОС_ДРУЖБЫ_ОШИБКА_ОТПРАВКИ",
                    "Ошибка при отправке запроса на дружбу", context, e);
            throw e;
        }
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
        Map<String, Object> context = new HashMap<>();
        context.put("userId", currentUser.getId());
        context.put("page", pageRequest.getPageNumber());
        context.put("size", pageRequest.getSize());
        context.put("status", FriendshipStatus.ACCEPTED);

        centralLogger.logInfo("СПИСОК_ДРУЗЕЙ_ПОЛУЧЕНИЕ",
                "Получение списка друзей", context);

        try {
            PageResponse<RelationshipResponse> response = getRelationshipsByStatus(currentUser, FriendshipStatus.ACCEPTED, pageRequest);

            centralLogger.logInfo("СПИСОК_ДРУЗЕЙ_ПОЛУЧЕН",
                    "Список друзей успешно получен", context);

            return response;
        } catch (Exception e) {
            centralLogger.logError("СПИСОК_ДРУЗЕЙ_ОШИБКА_ПОЛУЧЕНИЯ",
                    "Ошибка при получении списка друзей", context, e);
            throw e;
        }
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
        Map<String, Object> context = new HashMap<>();
        context.put("userId", currentUser.getId());
        context.put("page", pageRequest.getPageNumber());
        context.put("size", pageRequest.getSize());
        context.put("status", FriendshipStatus.BLOCKED);

        centralLogger.logInfo("ЧЕРНЫЙ_СПИСОК_ПОЛУЧЕНИЕ",
                "Получение черного списка", context);

        try {
            PageResponse<RelationshipResponse> response = getRelationshipsByStatus(currentUser, FriendshipStatus.BLOCKED, pageRequest);

            centralLogger.logInfo("ЧЕРНЫЙ_СПИСОК_ПОЛУЧЕН",
                    "Черный список успешно получен", context);

            return response;
        } catch (Exception e) {
            centralLogger.logError("ЧЕРНЫЙ_СПИСОК_ОШИБКА_ПОЛУЧЕНИЯ",
                    "Ошибка при получении черного списка", context, e);
            throw e;
        }
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
        Map<String, Object> context = new HashMap<>();
        context.put("userId", currentUser.getId());
        context.put("page", pageRequest.getPageNumber());
        context.put("size", pageRequest.getSize());
        context.put("status", FriendshipStatus.DECLINED);

        centralLogger.logInfo("ОТКЛОНЕННЫЕ_ЗАПРОСЫ_ПОЛУЧЕНИЕ",
                "Получение списка отклоненных запросов", context);

        try {
            PageResponse<RelationshipResponse> response = getRelationshipsByStatus(currentUser, FriendshipStatus.DECLINED, pageRequest);

            centralLogger.logInfo("ОТКЛОНЕННЫЕ_ЗАПРОСЫ_ПОЛУЧЕНЫ",
                    "Список отклоненных запросов успешно получен", context);

            return response;
        } catch (Exception e) {
            centralLogger.logError("ОТКЛОНЕННЫЕ_ЗАПРОСЫ_ОШИБКА_ПОЛУЧЕНИЯ",
                    "Ошибка при получении списка отклоненных запросов", context, e);
            throw e;
        }
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
        Map<String, Object> context = new HashMap<>();
        context.put("userId", currentUser.getId());
        context.put("targetUserId", request.getTargetUserId());

        centralLogger.logInfo("ПОЛЬЗОВАТЕЛЬ_БЛОКИРОВКА",
                "Блокировка пользователя", context);

        try {
            relationshipValidator.validateBlockUser(request, currentUser);

            User targetUser = entityUtils.getUser(request.getTargetUserId());

            Relationship relationship = relationshipRepository
                    .findRelationshipBetweenUsers(currentUser.getId(), targetUser.getId())
                    .map(existing -> updateRelationshipStatus(existing, FriendshipStatus.BLOCKED))
                    .orElseGet(() -> relationshipFactory.createBlockedRelationship(currentUser, targetUser));

            Relationship savedRelationship = relationshipRepository.save(relationship);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("relationshipId", savedRelationship.getId());

            centralLogger.logInfo("ПОЛЬЗОВАТЕЛЬ_ЗАБЛОКИРОВАН",
                    "Пользователь успешно заблокирован", successContext);

            return entityMapper.map(savedRelationship, RelationshipResponse.class);
        } catch (Exception e) {
            centralLogger.logError("ПОЛЬЗОВАТЕЛЬ_ОШИБКА_БЛОКИРОВКИ",
                    "Ошибка при блокировке пользователя", context, e);
            throw e;
        }
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
        Map<String, Object> context = new HashMap<>();
        context.put("userId", currentUser.getId());
        context.put("targetUserId", request.getTargetUserId());

        centralLogger.logInfo("ЗАПРОС_ДРУЖБЫ_ПРИНЯТИЕ",
                "Принятие запроса на дружбу", context);

        try {
            relationshipValidator.validateStatusChange(request, currentUser);

            notificationEventPublisherService.publishFriendRequestAccepted(this, request.getTargetUserId(), currentUser.getId());

            RelationshipResponse response = changeRelationshipStatus(request, FriendshipStatus.ACCEPTED, currentUser);

            centralLogger.logInfo("ЗАПРОС_ДРУЖБЫ_ПРИНЯТ",
                    "Запрос на дружбу успешно принят", context);

            return response;
        } catch (Exception e) {
            centralLogger.logError("ЗАПРОС_ДРУЖБЫ_ОШИБКА_ПРИНЯТИЯ",
                    "Ошибка при принятии запроса на дружбу", context, e);
            throw e;
        }
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
        Map<String, Object> context = new HashMap<>();
        context.put("userId", currentUser.getId());
        context.put("targetUserId", request.getTargetUserId());

        centralLogger.logInfo("ЗАПРОС_ДРУЖБЫ_ОТКЛОНЕНИЕ",
                "Отклонение запроса на дружбу", context);

        try {
            relationshipValidator.validateStatusChange(request, currentUser);

            RelationshipResponse response = changeRelationshipStatus(request, FriendshipStatus.DECLINED, currentUser);

            centralLogger.logInfo("ЗАПРОС_ДРУЖБЫ_ОТКЛОНЕН",
                    "Запрос на дружбу успешно отклонен", context);

            return response;
        } catch (Exception e) {
            centralLogger.logError("ЗАПРОС_ДРУЖБЫ_ОШИБКА_ОТКЛОНЕНИЯ",
                    "Ошибка при отклонении запроса на дружбу", context, e);
            throw e;
        }
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