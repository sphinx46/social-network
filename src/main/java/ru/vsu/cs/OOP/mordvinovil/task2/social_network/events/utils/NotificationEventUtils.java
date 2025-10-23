package ru.vsu.cs.OOP.mordvinovil.task2.social_network.events.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.UserRepository;

@RequiredArgsConstructor
@Slf4j
@Component
public class NotificationEventUtils {
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
     * Обрезка сообщения до максимальной длины
     */
    public String truncateMessage(String message, int maxLength) {
        if (message == null) return "";
        return message.length() > maxLength ? message.substring(0, maxLength - 3) + "..." : message;
    }
}
