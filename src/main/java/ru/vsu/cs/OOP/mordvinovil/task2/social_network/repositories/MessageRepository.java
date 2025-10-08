package ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Message;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.MessageStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE (m.sender.id = :senderId AND m.receiver.id = :receiverId) OR (m.sender.id = :receiverId AND m.receiver.id = :senderId)")
    Optional<List<Message>> findMessagesBetweenUsers(@Param("senderId") Long senderId,
                                           @Param("receiverId") Long receiverId);

    @Modifying
    @Query("UPDATE Message m SET m.status = :status WHERE m.sender.id = :senderId AND m.receiver.id = :receiverId")
    void updateStatusMessages(@Param("senderId") Long senderId,
                              @Param("receiverId") Long receiverId,
                              @Param("status") MessageStatus status);

    @Query("SELECT m FROM Message m WHERE ((m.sender.id = :senderId AND m.receiver.id = :receiverId) OR (m.sender.id = :receiverId AND m.receiver.id = :senderId)) AND m.status = :status")
    Optional<List<Message>> findMessagesBetweenUsersByStatus(@Param("senderId") Long senderId,
                                                             @Param("receiverId") Long receiverId,
                                                             @Param("status") MessageStatus status);

    @Query("SELECT COUNT(m) FROM Message m WHERE (m.sender.id = :userId OR m.receiver.id = :userId) AND m.status = :status")
    Long countMessagesByStatus(@Param("userId") Long userId,
                               @Param("status") MessageStatus status);

    List<Message> findBySenderIdAndReceiverId(Long senderId, Long receiverId);

    List<Message> findBySenderIdOrReceiverId(Long senderId, Long receiverId);
}