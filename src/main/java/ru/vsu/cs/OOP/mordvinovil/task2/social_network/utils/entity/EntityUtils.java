package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.*;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.providersImpl.*;

@Component
@RequiredArgsConstructor
public class EntityUtils {
    private final UserEntityProviderImpl userProvider;
    private final ProfileEntityProviderImpl profileProvider;
    private final PostEntityProviderImpl postProvider;
    private final CommentEntityProviderImpl commentProvider;
    private final MessageEntityProviderImpl messageProvider;
    private final RelationshipEntityProviderImpl relationshipProvider;
    private final LikeEntityProviderImpl likeProvider;
    private final NotificationEntityProviderImpl notificationProvider;

    /**
     * Получает пользователя по идентификатору
     *
     * @param id идентификатор пользователя
     * @return пользователь
     */
    public User getUser(Long id) {
        return userProvider.getById(id);
    }

    /**
     * Получает уведомление по идентификатору
     *
     * @param id идентификатор уведомления
     * @return уведомление
     */
    public Notification getNotification(Long id) {
        return notificationProvider.getById(id);
    }

    /**
     * Получает профиль по идентификатору
     *
     * @param id идентификатор профиля
     * @return профиль
     */
    public Profile getProfile(Long id) {
        return profileProvider.getById(id);
    }

    /**
     * Получает профиль по пользователю
     *
     * @param user пользователь
     * @return профиль
     */
    public Profile getProfileByUser(User user) {
        return profileProvider.getByUser(user);
    }

    /**
     * Получает профиль по идентификатору пользователя
     *
     * @param userId идентификатор пользователя
     * @return профиль
     */
    public Profile getProfileByUserId(Long userId) {
        return profileProvider.getByUserId(userId);
    }

    /**
     * Получает пост по идентификатору
     *
     * @param id идентификатор поста
     * @return пост
     */
    public Post getPost(Long id) {
        return postProvider.getById(id);
    }

    /**
     * Получает комментарий по идентификатору
     *
     * @param id идентификатор комментария
     * @return комментарий
     */
    public Comment getComment(Long id) {
        return commentProvider.getById(id);
    }

    /**
     * Получает сообщение по идентификатору
     *
     * @param id идентификатор сообщения
     * @return сообщение
     */
    public Message getMessage(Long id) {
        return messageProvider.getById(id);
    }

    /**
     * Получает отношение по идентификатору
     *
     * @param id идентификатор отношения
     * @return отношение
     */
    public Relationship getRelationship(Long id) {
        return relationshipProvider.getById(id);
    }

    /**
     * Получает лайк по идентификатору
     *
     * @param id идентификатор лайка
     * @return лайк
     */
    public Like getLike(Long id) {
        return likeProvider.getById(id);
    }
}