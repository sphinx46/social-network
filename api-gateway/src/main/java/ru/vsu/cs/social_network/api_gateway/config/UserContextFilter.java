package ru.vsu.cs.social_network.api_gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Component
public class UserContextFilter extends AbstractGatewayFilterFactory<UserContextFilter.Config> {
    @Value("${app.gateway.signature-secret}")
    private String signatureSecret;


    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{1,100}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final int MAX_HEADER_LENGTH = 200;

    public UserContextFilter() {
        super(Config.class);
    }

    /**
     * Применяет фильтр для извлечения информации о пользователе из OIDC и добавления её в заголовки запроса.
     *
     * @param config конфигурация фильтра
     * @return Gateway фильтр
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!config.isEnabled()) {
                return chain.filter(exchange);
            }

            String path = exchange.getRequest().getPath().value();
            log.debug("ШЛЮЗ_КОНТЕКСТ_НАЧАЛО: извлечение контекста пользователя для пути: {}", path);

            return ReactiveSecurityContextHolder.getContext()
                    .cast(SecurityContext.class)
                    .map(SecurityContext::getAuthentication)
                    .filter(authentication -> authentication != null && authentication.getPrincipal() instanceof OidcUser)
                    .map(authentication -> (OidcUser) authentication.getPrincipal())
                    .map(oidcUser -> {
                        try {
                            String userId = oidcUser.getSubject();
                            if (userId == null || userId.isEmpty()) {
                                log.warn("ШЛЮЗ_КОНТЕКСТ_ОШИБКА: отсутствует subject (user ID) в OIDC user");
                                throw new IllegalStateException("OIDC subject (user ID) is missing");
                            }

                            UUID.fromString(userId);

                            String username = sanitizeHeader(oidcUser.getPreferredUsername());
                            String email = sanitizeEmail(oidcUser.getEmail());
                            String firstName = sanitizeHeader(oidcUser.getGivenName());
                            String lastName = sanitizeHeader(oidcUser.getFamilyName());

                            if (username != null && !USERNAME_PATTERN.matcher(username).matches()) {
                                log.warn("ШЛЮЗ_КОНТЕКСТ_ОШИБКА: неверный формат username: {}", username);
                                username = "";
                            }

                            log.debug("ШЛЮЗ_КОНТЕКСТ_ДАННЫЕ: userId={}, username={}", userId, username);

                            String currentTimestamp = String.valueOf(System.currentTimeMillis());
                            String signature = generateSignature(userId, currentTimestamp);

                            ServerHttpRequest request = exchange.getRequest().mutate()
                                    .header("X-User-Id", userId)
                                    .header("X-Username", username != null ? username : "")
                                    .header("X-Email", email != null ? email : "")
                                    .header("X-First-Name", firstName != null ? firstName : "")
                                    .header("X-Last-Name", lastName != null ? lastName : "")
                                    .header("X-Timestamp", currentTimestamp)
                                    .header("X-Signature", signature)
                                    .build();

                            log.debug("ШЛЮЗ_ПОДПИСЬ_СГЕНЕРИРОВАНА: Подпись заголовков для userId: {}, timestamp: {}", userId, currentTimestamp);

                            return exchange.mutate().request(request).build();
                        } catch (IllegalArgumentException e) {
                            log.error("ШЛЮЗ_КОНТЕКСТ_ОШИБКА: неверный формат user ID в OIDC user: {}", e.getMessage(), e);
                            throw new IllegalStateException("Invalid user ID format in OIDC user", e);
                        } catch (Exception e) {
                            log.error("ШЛЮЗ_КОНТЕКСТ_ОШИБКА: ошибка извлечения контекста пользователя: {}", e.getMessage(), e);
                            throw new IllegalStateException("Error extracting user context from OIDC user", e);
                        }
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        log.warn("ШЛЮЗ_КОНТЕКСТ_ОШИБКА: OIDC user не найден в SecurityContext для запроса: {}", path);
                        log.debug("ШЛЮЗ_КОНТЕКСТ_ОШИБКА: пропускаем фильтр, передаем запрос дальше");
                        return Mono.just(exchange);
                    }))
                    .flatMap(chain::filter)
                    .onErrorResume(IllegalStateException.class, e -> {
                        log.error("ШЛЮЗ_КОНТЕКСТ_ОШИБКА_АУТЕНТИФИКАЦИЯ: {}", e.getMessage(), e);
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        };
    }

    /**
     * Санитизирует значение заголовка, удаляя опасные символы и ограничивая длину.
     *
     * @param value исходное значение
     * @return санитизированное значение или null
     */
    private String sanitizeHeader(String value) {
        if (value == null) {
            return null;
        }
        String sanitized = value.replaceAll("[\\r\\n]", "").trim();
        if (sanitized.length() > MAX_HEADER_LENGTH) {
            sanitized = sanitized.substring(0, MAX_HEADER_LENGTH);
        }
        return sanitized;
    }

    /**
     * Санитизирует и валидирует email адрес.
     *
     * @param email исходный email
     * @return валидный email или null
     */
    private String sanitizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String sanitized = email.replaceAll("[\\r\\n]", "").trim();
        if (sanitized.length() > MAX_HEADER_LENGTH) {
            return null;
        }
        if (!EMAIL_PATTERN.matcher(sanitized).matches()) {
            log.warn("ШЛЮЗ_КОНТЕКСТ_ОШИБКА: неверный формат email: {}", email);
            return null;
        }
        return sanitized;
    }


    /**
     * Генерирует HMAC подпись для данных пользователя.
     *
     * @param userId    идентификатор пользователя
     * @param timestamp временная метка
     * @return Base64-encoded подпись
     */
    private String generateSignature(String userId, String timestamp) {
        try {
            String dataToSign = userId + "|" + timestamp;
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    signatureSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
            byte[] signatureBytes = mac.doFinal(dataToSign.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            log.error("ШЛЮЗ_ПОДПИСЬ_ОШИБКА: Ошибка генерации подписи для userId: {}", userId, e);
            throw new IllegalStateException("Не удалось сгенерировать подпись заголовков");
        }
    }

    public static class Config {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
