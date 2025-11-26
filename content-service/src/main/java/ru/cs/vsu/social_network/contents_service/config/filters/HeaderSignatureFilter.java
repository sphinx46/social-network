package ru.cs.vsu.social_network.contents_service.config.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.cs.vsu.social_network.contents_service.config.security.GatewayUserPrincipal;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Component
public final class HeaderSignatureFilter extends OncePerRequestFilter {
    private static final long MAX_TIMESTAMP_DIFF_MS = 30_000;
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USERNAME = "X-Username";
    private static final String HEADER_EMAIL = "X-Email";
    private static final String HEADER_FIRST_NAME = "X-First-Name";
    private static final String HEADER_LAST_NAME = "X-Last-Name";
    private static final String HEADER_TIMESTAMP = "X-Timestamp";
    private static final String HEADER_SIGNATURE = "X-Signature";

    @Value("${app.gateway.signature-secret}")
    private String signatureSecret;

    /**
     * Проверяет подпись заголовков и формирует аутентификационный контекст.
     */
    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain)
            throws ServletException, IOException {

        if (shouldNotFilter(request)) {
            log.debug("СИГНАТУРА_ПРОПУСК: публичный эндпоинт {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        String userId = request.getHeader(HEADER_USER_ID);
        String timestamp = request.getHeader(HEADER_TIMESTAMP);
        String signature = request.getHeader(HEADER_SIGNATURE);

        if (userId == null || timestamp == null || signature == null) {
            log.warn("СИГНАТУРА_ОШИБКА: отсутствуют обязательные заголовки");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        if (isTimestampExpired(timestamp) || !isValidSignature(userId, timestamp, signature)) {
            log.warn("СИГНАТУРА_ОШИБКА: подпись невалидна для userId {}", userId);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        setAuthenticationContext(request, userId);
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        String path = normalize(request);
        boolean isSwagger = path.startsWith("/swagger") || path.contains("swagger")
                || path.startsWith("/api-docs") || path.contains("api-docs");
        boolean isActuator = path.startsWith("/actuator");
        return isSwagger || isActuator;
    }

    private String normalize(final HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            return uri.substring(contextPath.length());
        }
        return uri;
    }

    private boolean isTimestampExpired(final String timestampStr) {
        try {
            long timestamp = Long.parseLong(timestampStr);
            long current = System.currentTimeMillis();
            return Math.abs(current - timestamp) > MAX_TIMESTAMP_DIFF_MS;
        } catch (NumberFormatException ex) {
            log.warn("СИГНАТУРА_ОШИБКА: неверный формат timestamp {}", timestampStr);
            return true;
        }
    }

    private boolean isValidSignature(final String userId,
                                     final String timestamp,
                                     final String receivedSignature) {
        try {
            String data = userId + "|" + timestamp;
            String expectedSignature = calculateSignature(data);
            return expectedSignature.equals(receivedSignature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("СИГНАТУРА_ОШИБКА: не удалось проверить подпись", e);
            return false;
        }
    }

    private String calculateSignature(final String data)
            throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(
                signatureSecret.getBytes(StandardCharsets.UTF_8),
                HMAC_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(secretKeySpec);
        byte[] signatureBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    private void setAuthenticationContext(final HttpServletRequest request,
                                          final String userId) {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }
        try {
            UUID parsed = UUID.fromString(userId);
            GatewayUserPrincipal principal = GatewayUserPrincipal.builder()
                    .userId(parsed)
                    .username(defaultIfNull(request.getHeader(HEADER_USERNAME)))
                    .email(defaultIfNull(request.getHeader(HEADER_EMAIL)))
                    .firstName(defaultIfNull(request.getHeader(HEADER_FIRST_NAME)))
                    .lastName(defaultIfNull(request.getHeader(HEADER_LAST_NAME)))
                    .build();

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                    );
            authenticationToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            log.debug("СИГНАТУРА_УСПЕХ: аутентификация выполнена для userId {}", userId);
        } catch (IllegalArgumentException ex) {
            log.warn("СИГНАТУРА_ОШИБКА: неверный формат userId {}", userId);
        }
    }

    private String defaultIfNull(final String value) {
        return value == null ? "" : value;
    }
}

