package ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.UserRepository;


@Component
@RequiredArgsConstructor
@Slf4j
public class UserInfoService {
    private final UserRepository userRepository;

    /**
     * Безопасное получение имени пользователя
     */
    public String getUsernameSafe(Long userId) {
        try {
            return userRepository.findById(userId)
                    .map(User::getUsername)
                    .orElse("Unknown User");
        } catch (Exception e) {
            log.warn("Failed to get username for user ID: {}", userId, e);
            return "Unknown User";
        }
    }

    /**
     * Безопасное получение ID пользователя с проверкой существования
     */
    public boolean userExists(Long userId) {
        try {
            return userRepository.existsById(userId);
        } catch (Exception e) {
            log.warn("Failed to check user existence for ID: {}", userId, e);
            return false;
        }
    }
}