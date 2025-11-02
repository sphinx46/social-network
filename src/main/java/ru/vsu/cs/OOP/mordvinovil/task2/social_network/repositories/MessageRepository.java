package ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Message;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.MessageStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Modifying
    @Query("UPDATE Message m SET m.status = :status, m.updatedAt = :updatedAt WHERE m.id = :messageId")
    void updateMessageStatus(@Param("messageId") Long messageId,
                             @Param("status") MessageStatus status,
                             @Param("updatedAt") LocalDate updatedAt);

    @Query("SELECT m FROM Message m WHERE ((m.sender.id = :senderId AND m.receiver.id = :receiverId) OR (m.sender.id = :receiverId AND m.receiver.id = :senderId)) AND m.status = :status")
    Optional<List<Message>> findMessagesBetweenUsersByStatus(@Param("senderId") Long senderId,
                                                             @Param("receiverId") Long receiverId,
                                                             @Param("status") MessageStatus status);

    @Query("SELECT COUNT(m) FROM Message m WHERE (m.sender.id = :userId OR m.receiver.id = :userId) " +
            "AND m.status = :status")
    Long countMessagesByStatus(@Param("userId") Long userId,
                               @Param("status") MessageStatus status);


    @Query("SELECT m FROM Message m WHERE (m.sender.id = :senderId AND m.receiver.id = :receiverId) " +
            "OR (m.sender.id = :receiverId AND m.receiver.id = :senderId)")
    Optional<Page<Message>> findMessagesBetweenUsers(@Param("senderId") Long senderId,
                                                     @Param("receiverId") Long receiverId,
                                                     Pageable pageable);

    Optional<Page<Message>> findBySenderId(Long senderId, Pageable pageable);
    Optional<Page<Message>> findByReceiverIdAndStatus(Long receiverId, MessageStatus status, Pageable pageable);
}