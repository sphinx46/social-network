package ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;

import java.util.List;

@Transactional
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> getAllPostsByUser(User user);
}
