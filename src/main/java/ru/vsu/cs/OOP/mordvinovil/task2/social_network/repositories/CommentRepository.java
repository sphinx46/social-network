package ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
}
