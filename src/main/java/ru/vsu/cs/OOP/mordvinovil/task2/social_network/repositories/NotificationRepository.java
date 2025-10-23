package ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Notification;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.NotificationStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserActionOrderByCreatedAtDesc(User user);

    List<Notification> findByUserActionAndStatusOrderByCreatedAtDesc(User user, NotificationStatus status);

    Optional<Notification> findByIdAndUserActionId(Long id, Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.status = :status, n.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE n.id = :notificationId AND n.userAction.id = :userId")
    void updateStatus(@Param("notificationId") Long notificationId,
                      @Param("userId") Long userId,
                      @Param("status") NotificationStatus status);

    @Modifying
    @Query("UPDATE Notification n SET n.status = :status, n.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE n.userAction.id = :userId AND n.status = NotificationStatus.UNREAD")
    void markAllAsRead(@Param("userId") Long userId, @Param("status") NotificationStatus status);

    Long countByUserActionAndStatus(User user, NotificationStatus status);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.userAction = :user AND n.status = NotificationStatus.DELETED")
    void deleteAllDeletedByUser(@Param("user") User user);
}