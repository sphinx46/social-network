package ru.vsu.cs.social_network.api_gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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
            boolean isProtectedEndpoint = path.contains("/profile/me");

            return exchange.getPrincipal()
                    .onErrorResume(e -> Mono.empty())
                    .flatMap(principal -> {
                        if (principal instanceof OAuth2AuthenticationToken) {
                            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) principal;
                            if (oauth2Token.getPrincipal() instanceof OidcUser) {
                                return Mono.just((OidcUser) oauth2Token.getPrincipal());
                            }
                            return Mono.empty();
                        } else if (principal instanceof OidcUser) {
                            return Mono.just((OidcUser) principal);
                        }
                        return Mono.empty();
                    })
                    .flatMap(oidcUser -> {
                        try {
                            String userId = oidcUser.getSubject();
                            if (userId == null || userId.isEmpty()) {
                                log.warn("ШЛЮЗ_КОНТЕКСТ_ОШИБКА: отсутствует subject (user ID) в OIDC user");
                                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                return exchange.getResponse().setComplete()
                                        .then(Mono.empty());
                            }

                            UUID.fromString(userId);

                            String username = sanitizeHeader(oidcUser.getPreferredUsername());
                            String email = sanitizeEmail(oidcUser.getEmail());
                            String firstName = sanitizeHeader(oidcUser.getGivenName());
                            String lastName = sanitizeHeader(oidcUser.getFamilyName());

                            if (username != null && !USERNAME_PATTERN.matcher(username).matches()) {
                                username = "";
                            }

                            String currentTimestamp = String.valueOf(System.currentTimeMillis());
                            String signature;
                            try {
                                signature = generateSignature(userId, currentTimestamp);
                            } catch (IllegalStateException e) {
                                log.error("ШЛЮЗ_КОНТЕКСТ_ОШИБКА: ошибка генерации подписи для userId: {}", userId, e);
                                exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                                return exchange.getResponse().setComplete()
                                        .then(Mono.empty());
                            }

                            ServerHttpRequest request = exchange.getRequest().mutate()
                                    .header("X-User-Id", userId)
                                    .header("X-Username", username != null ? username : "")
                                    .header("X-Email", email != null ? email : "")
                                    .header("X-First-Name", firstName != null ? firstName : "")
                                    .header("X-Last-Name", lastName != null ? lastName : "")
                                    .header("X-Timestamp", currentTimestamp)
                                    .header("X-Signature", signature)
                                    .build();

                            return Mono.just(exchange.mutate().request(request).build());
                        } catch (IllegalArgumentException e) {
                            log.error("ШЛЮЗ_КОНТЕКСТ_ОШИБКА: неверный формат user ID в OIDC user: {}", e.getMessage(), e);
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete()
                                    .then(Mono.empty());
                        } catch (Exception e) {
                            log.error("ШЛЮЗ_КОНТЕКСТ_ОШИБКА: ошибка извлечения контекста пользователя: {}", e.getMessage(), e);
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete()
                                    .then(Mono.empty());
                        }
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        if (isProtectedEndpoint) {
                            log.warn("ШЛЮЗ_КОНТЕКСТ_ОШИБКА: OIDC user не найден для защищенного эндпоинта: {}. " +
                                    "Запрос требует аутентификации. Блокируем запрос.", path);
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete()
                                    .then(Mono.empty());
                        }
                        return Mono.just(exchange);
                    }))
                    .flatMap(chain::filter)
                    .onErrorResume(Exception.class, e -> {
                        log.error("ШЛЮЗ_КОНТЕКСТ_ОШИБКА_НЕОЖИДАННАЯ: Неожиданная ошибка в UserContextFilter для пути {}: {}", path, e.getMessage(), e);
                        if (!exchange.getResponse().isCommitted()) {
                            if (isProtectedEndpoint) {
                                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            } else {
                                exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                            }
                            return exchange.getResponse().setComplete();
                        }
                        return Mono.error(e);
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

    /**
     * Конфигурация фильтра извлечения контекста пользователя.
     */
    public static class Config {
        private boolean enabled = true;

        /**
         * Проверяет, включен ли фильтр.
         *
         * @return true, если фильтр включен
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Устанавливает состояние фильтра.
         *
         * @param enabled true для включения фильтра, false для отключения
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
