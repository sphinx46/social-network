package ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;

import java.util.List;

@Repository
public interface NewsFeedRepository extends JpaRepository<Post, Long> {

    @Query("SELECT DISTINCT p FROM Post p " +
            "JOIN FETCH p.user " +
            "WHERE p.user.id IN (" +
            "    SELECT r.receiver.id FROM Relationship r " +
            "    WHERE r.sender.id = :userId " +
            "    AND r.status = FriendshipStatus.ACCEPTED" +
            ") OR p.user.id IN (" +
            "    SELECT r.sender.id FROM Relationship r " +
            "    WHERE r.receiver.id = :userId " +
            "    AND r.status = FriendshipStatus.ACCEPTED" +
            ") " +
            "ORDER BY p.createdAt DESC")
    List<Post> findPostsByFriends(@Param("userId") Long userId);
}
