package ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Relationship;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.enums.FriendshipStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface RelationshipRepository extends JpaRepository<Relationship, Long> {
    List<Relationship> findBySenderIdAndStatus(Long senderId, FriendshipStatus status);
    List<Relationship> findByReceiverIdAndStatus(Long receiverId, FriendshipStatus status);

    Optional<Relationship> findBySenderIdAndReceiverIdAndStatus(Long senderId,
                                                                Long receiverId,
                                                                FriendshipStatus status);

    @Query("SELECT r FROM Relationship r WHERE (r.sender.id = :userId OR r.receiver.id = :userId) " +
            "AND r.status = :status")
    List<Relationship> findByUserAndStatus(@Param("userId") Long userId,
                                           @Param("status") FriendshipStatus status);

    @Query("SELECT r FROM Relationship r WHERE (r.sender.id = :userId OR r.receiver.id = :userId) " +
            "AND r.status = :status")
    Page<Relationship> findByUserAndStatus(@Param("userId") Long userId,
                                           @Param("status") FriendshipStatus status,
                                           Pageable pageable);

    @Query("SELECT r FROM Relationship r WHERE r.sender.id = :userId AND r.status = :status")
    List<Relationship> findSentRequestsByUserAndStatus(@Param("userId") Long userId,
                                                       @Param("status") FriendshipStatus status);

    @Query("SELECT r FROM Relationship r WHERE r.receiver.id = :userId AND r.status = :status")
    List<Relationship> findReceivedRequestsByUserAndStatus(@Param("userId") Long userId,
                                                           @Param("status") FriendshipStatus status);

    @Query("SELECT r.status FROM Relationship r WHERE r.sender.id = :userSenderId " +
            "AND r.receiver.id = :userReceiverId")
    Optional<FriendshipStatus> findStatusBySenderAndReceiver(@Param("userSenderId") Long userSenderId,
                                                             @Param("userReceiverId") Long userReceiverId);

    @Query("SELECT r FROM Relationship r WHERE (r.sender.id = :user1Id AND r.receiver.id = :user2Id) " +
            "OR (r.sender.id = :user2Id AND r.receiver.id = :user1Id)")
    Optional<Relationship> findRelationshipBetweenUsers(@Param("user1Id") Long user1Id,
                                                        @Param("user2Id") Long user2Id);

    @Query("SELECT COUNT(r) > 0 FROM Relationship r WHERE ((r.sender.id = :user1Id " +
            "AND r.receiver.id = :user2Id) OR (r.sender.id = :user2Id AND r.receiver.id = :user1Id)) " +
            "AND r.status = :status")
    boolean existsRelationshipWithStatus(@Param("user1Id") Long user1Id,
                                         @Param("user2Id") Long user2Id,
                                         @Param("status") FriendshipStatus status);

    @Query("SELECT r FROM Relationship r WHERE r.receiver.id = :userId AND r.status = :status")
    List<Relationship> findPendingRequestsForUser(@Param("userId") Long userId,
                                                  @Param("status") FriendshipStatus status);
}