package ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories;

import jakarta.transaction.Transactional;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
