package ru.cs.vsu.social_network.api_gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Фильтр шлюза для извлечения контекста пользователя из OAuth2/OIDC аутентификации
 * и добавления информации о пользователе в заголовки запроса для последующих микросервисов.
 */
@Slf4j
@Component
public class UserContextFilter
        extends AbstractGatewayFilterFactory<UserContextFilter.Config> {

    /**
     * Секретный ключ для генерации подписи заголовков.
     */
    @Value("${app.gateway.signature-secret}")
    private String signatureSecret;

    /**
     * Паттерн для валидации имени пользователя.
     */
    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._-]{1,100}$");

    /**
     * Паттерн для валидации email адреса.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    /**
     * Максимальная длина значения заголовка.
     */
    private static final int MAX_HEADER_LENGTH = 200;

    /**
     * Конструктор по умолчанию.
     */
    public UserContextFilter() {
        super(Config.class);
    }

    /**
     * Применяет фильтр для извлечения информации о пользователе из OIDC
     * и добавления её в заголовки запроса.
     *
     * @param config конфигурация фильтра
     * @return Gateway фильтр
     */
    @Override
    public GatewayFilter apply(final Config config) {
        return (exchange, chain) -> {
            if (!config.isEnabled()) {
                return chain.filter(exchange);
            }

            String path = exchange.getRequest().getPath().value();
            boolean isProtectedEndpoint = isProtectedEndpoint(path);

            return exchange.getPrincipal()
                    .onErrorResume(e -> Mono.empty())
                    .flatMap(this::resolveUserIdentity)
                    .flatMap(identity -> {
                        try {
                            String userId = identity.userId();
                            if (userId == null || userId.isEmpty()) {
                                log.warn("ШЛЮЗ_КОНТЕКСТ_ОШИБКА: "
                                        + "отсутствует subject (user ID) "
                                        + "в OIDC user");
                                exchange.getResponse()
                                        .setStatusCode(HttpStatus.UNAUTHORIZED);
                                return exchange.getResponse().setComplete()
                                        .then(Mono.empty());
                            }

                            UUID.fromString(userId);

                            String username = sanitizeHeader(identity.username());
                            String email = sanitizeEmail(identity.email());
                            String firstName = sanitizeHeader(identity.firstName());
                            String lastName = sanitizeHeader(identity.lastName());

                            if (username != null
                                    && !USERNAME_PATTERN.matcher(username)
                                    .matches()) {
                                username = "";
                            }

                            String currentTimestamp = String.valueOf(
                                    System.currentTimeMillis());
                            String signature;
                            try {
                                signature = generateSignature(
                                        userId, currentTimestamp);
                            } catch (IllegalStateException e) {
                                log.error("ШЛЮЗ_КОНТЕКСТ_ОШИБКА: "
                                                + "ошибка генерации подписи для "
                                                + "userId: {}",
                                        userId, e);
                                exchange.getResponse()
                                        .setStatusCode(HttpStatus
                                                .INTERNAL_SERVER_ERROR);
                                return exchange.getResponse().setComplete()
                                        .then(Mono.empty());
                            }

                            ServerHttpRequest request = exchange.getRequest()
                                    .mutate()
                                    .header("X-User-Id", userId)
                                    .header("X-Username",
                                            username != null ? username : "")
                                    .header("X-Email",
                                            email != null ? email : "")
                                    .header("X-First-Name",
                                            firstName != null ? firstName : "")
                                    .header("X-Last-Name",
                                            lastName != null ? lastName : "")
                                    .header("X-Timestamp", currentTimestamp)
                                    .header("X-Signature", signature)
                                    .build();

                            return Mono.just(exchange.mutate()
                                    .request(request).build());
                        } catch (IllegalArgumentException e) {
                            log.error("ШЛЮЗ_КОНТЕКСТ_ОШИБКА: "
                                            + "неверный формат user ID в OIDC user: {}",
                                    e.getMessage(), e);
                            exchange.getResponse()
                                    .setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete()
                                    .then(Mono.empty());
                        } catch (Exception e) {
                            log.error("ШЛЮЗ_КОНТЕКСТ_ОШИБКА: "
                                            + "ошибка извлечения контекста "
                                            + "пользователя: {}",
                                    e.getMessage(), e);
                            exchange.getResponse()
                                    .setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete()
                                    .then(Mono.empty());
                        }
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        if (isProtectedEndpoint) {
                            log.warn("ШЛЮЗ_КОНТЕКСТ_ОШИБКА: "
                                    + "OIDC user не найден для защищенного "
                                    + "эндпоинта: {}. "
                                    + "Запрос требует аутентификации. "
                                    + "Блокируем запрос.", path);
                            exchange.getResponse()
                                    .setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete()
                                    .then(Mono.empty());
                        }
                        return Mono.just(exchange);
                    }))
                    .flatMap(chain::filter)
                    .onErrorResume(Exception.class, e -> {
                        log.error("ШЛЮЗ_КОНТЕКСТ_ОШИБКА_НЕОЖИДАННАЯ: "
                                + "Неожиданная ошибка в UserContextFilter "
                                + "для пути {}: {}", path, e.getMessage(), e);
                        if (!exchange.getResponse().isCommitted()) {
                            if (isProtectedEndpoint) {
                                exchange.getResponse()
                                        .setStatusCode(
                                                HttpStatus.UNAUTHORIZED);
                            } else {
                                exchange.getResponse()
                                        .setStatusCode(HttpStatus
                                                .INTERNAL_SERVER_ERROR);
                            }
                            return exchange.getResponse().setComplete();
                        }
                        return Mono.error(e);
                    });
        };
    }

    /**
     * Разрешает идентичность пользователя из различных типов principal объектов.
     *
     * @param principal объект principal из контекста безопасности
     * @return Mono с UserIdentity или empty если не удалось разрешить
     */
    private Mono<UserIdentity> resolveUserIdentity(final Object principal) {
        if (principal instanceof OAuth2AuthenticationToken oauth2Token) {
            if (oauth2Token.getPrincipal() instanceof OidcUser oidcUser) {
                return Mono.just(fromOidcUser(oidcUser));
            }
            return Mono.empty();
        } else if (principal instanceof OidcUser oidcUser) {
            return Mono.just(fromOidcUser(oidcUser));
        } else if (principal instanceof JwtAuthenticationToken jwtToken) {
            return Mono.just(fromJwt(jwtToken.getToken()));
        }
        return Mono.empty();
    }

    /**
     * Создает UserIdentity из OidcUser объекта.
     *
     * @param oidcUser OIDC пользователь
     * @return UserIdentity с данными пользователя
     */
    private UserIdentity fromOidcUser(final OidcUser oidcUser) {
        return new UserIdentity(
                oidcUser.getSubject(),
                oidcUser.getPreferredUsername(),
                oidcUser.getEmail(),
                oidcUser.getGivenName(),
                oidcUser.getFamilyName()
        );
    }

    /**
     * Создает UserIdentity из JWT токена.
     *
     * @param jwt JWT токен
     * @return UserIdentity с данными пользователя
     */
    private UserIdentity fromJwt(final Jwt jwt) {
        return new UserIdentity(
                jwt.getSubject(),
                jwt.getClaimAsString("preferred_username"),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("given_name"),
                jwt.getClaimAsString("family_name")
        );
    }

    /**
     * Проверяет, является ли endpoint защищенным и требует аутентификации.
     *
     * @param path путь запроса
     * @return true если endpoint защищенный, false в противном случае
     */
    private boolean isProtectedEndpoint(final String path) {
        if (path == null) {
            return false;
        }
        return path.startsWith("/api/profile/me")
                || path.startsWith("/api/upload");
    }

    /**
     * Санитизирует значение заголовка, удаляя опасные символы
     * и ограничивая длину.
     *
     * @param value исходное значение
     * @return санитизированное значение или null
     */
    private String sanitizeHeader(final String value) {
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
    private String sanitizeEmail(final String email) {
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
    private String generateSignature(
            final String userId, final String timestamp) {
        try {
            String dataToSign = userId + "|" + timestamp;
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    signatureSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
            byte[] signatureBytes = mac.doFinal(
                    dataToSign.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder()
                    .encodeToString(signatureBytes);
        } catch (Exception e) {
            log.error("ШЛЮЗ_ПОДПИСЬ_ОШИБКА: "
                    + "Ошибка генерации подписи для userId: {}", userId, e);
            throw new IllegalStateException(
                    "Не удалось сгенерировать подпись заголовков");
        }
    }

    /**
     * Конфигурация фильтра извлечения контекста пользователя.
     */
    public static class Config {
        /**
         * Включен ли фильтр.
         */
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
         * @param enabledParam true для включения фильтра,
         *                     false для отключения
         */
        public void setEnabled(final boolean enabledParam) {
            this.enabled = enabledParam;
        }
    }

    /**
     * Запись, представляющая идентичность пользователя с основными атрибутами.
     *
     * @param userId    уникальный идентификатор пользователя
     * @param username  имя пользователя
     * @param email     email адрес
     * @param firstName имя
     * @param lastName  фамилия
     */
    private record UserIdentity(
            String userId,
            String username,
            String email,
            String firstName,
            String lastName) {
    }
}