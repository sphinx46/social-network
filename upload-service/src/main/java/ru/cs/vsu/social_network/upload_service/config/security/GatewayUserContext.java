package ru.cs.vsu.social_network.upload_service.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public final class GatewayUserContext {

    /**
     * Возвращает текущего пользователя из контекста безопасности.
     *
     * @return текущий аутентифицированный пользователь
     */
    public Optional<GatewayUserPrincipal> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof GatewayUserPrincipal principal)) {
            log.debug("ПОЛЬЗОВАТЕЛЬ_КОНТЕКСТ: аутентифицированный пользователь отсутствует");
            return Optional.empty();
        }
        return Optional.of(principal);
    }

    /**
     * Возвращает идентификатор текущего пользователя.
     *
     * @return UUID пользователя
     * @throws IllegalStateException если пользователь не найден в контексте
     */
    public UUID requireUserId() {
        return getCurrentUser()
                .map(GatewayUserPrincipal::getUserId)
                .orElseThrow(() -> {
                    log.error("ПОЛЬЗОВАТЕЛЬ_КОНТЕКСТ_ОШИБКА: не удалось получить userId из контекста");
                    return new IllegalStateException("Не удалось определить пользователя из контекста безопасности");
                });
    }
}

