package ru.cs.vsu.social_network.user_profile_service.config.filters;

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
import ru.cs.vsu.social_network.user_profile_service.config.security.GatewayUserPrincipal;

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
public class HeaderSignatureFilter extends OncePerRequestFilter {
    @Value("${app.gateway.signature-secret}")
    private String signatureSecret;

    private static final long MAX_TIMESTAMP_DIFF_MS = 30000;
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USERNAME = "X-Username";
    private static final String HEADER_EMAIL = "X-Email";
    private static final String HEADER_FIRST_NAME = "X-First-Name";
    private static final String HEADER_LAST_NAME = "X-Last-Name";
    private static final String HEADER_TIMESTAMP = "X-Timestamp";
    private static final String HEADER_SIGNATURE = "X-Signature";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (shouldNotFilter(request)) {
            log.debug("СИГНАТУРА_ПРОПУСК: Публичный эндпоинт: {}", request.getServletPath());
            filterChain.doFilter(request, response);
            return;
        }

        String userId = request.getHeader(HEADER_USER_ID);
        String timestamp = request.getHeader(HEADER_TIMESTAMP);
        String signature = request.getHeader(HEADER_SIGNATURE);

        if (userId == null || timestamp == null || signature == null) {
            log.warn("СИГНАТУРА_ЗАГОЛОВКОВ_ОШИБКА: Отсутствуют обязательные заголовки подписи");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        if (!isValidSignature(userId, timestamp, signature)) {
            log.warn("СИГНАТУРА_ЗАГОЛОВКОВ_ОШИБКА: Невалидная подпись для userId: {}", userId);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        if (isTimestampExpired(timestamp)) {
            log.warn("СИГНАТУРА_ЗАГОЛОВКОВ_ОШИБКА: Просроченный timestamp: {}", timestamp);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        log.debug("СИГНАТУРА_ЗАГОЛОВКОВ_УСПЕХ: Подпись валидна для userId: {}", userId);
        setAuthenticationContext(request, userId);
        filterChain.doFilter(request, response);
    }

    /**
     * Пропускает публичные эндпоинты из проверки подписи
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String normalizedPath = getNormalizedPath(request);
        boolean isGet = "GET".equalsIgnoreCase(request.getMethod());
        boolean isPublicProfileLookup = normalizedPath.startsWith("/profile/") && !normalizedPath.equals("/profile/me");

        return normalizedPath.startsWith("/actuator/") ||
                (isPublicProfileLookup && isGet);
    }

    private String getNormalizedPath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && requestUri.startsWith(contextPath)) {
            return requestUri.substring(contextPath.length());
        }
        return requestUri;
    }

    private boolean isValidSignature(String userId, String timestamp, String receivedSignature) {
        try {
            String dataToSign = userId + "|" + timestamp;
            String expectedSignature = calculateSignature(dataToSign);
            return expectedSignature.equals(receivedSignature);
        } catch (Exception e) {
            log.error("СИГНАТУРА_ЗАГОЛОВКОВ_ОШИБКА: Ошибка проверки подписи", e);
            return false;
        }
    }

    private String calculateSignature(String data) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(signatureSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(secretKeySpec);
        byte[] signatureBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    private boolean isTimestampExpired(String timestampStr) {
        try {
            long timestamp = Long.parseLong(timestampStr);
            long currentTime = System.currentTimeMillis();
            return Math.abs(currentTime - timestamp) > MAX_TIMESTAMP_DIFF_MS;
        } catch (NumberFormatException e) {
            log.warn("СИГНАТУРА_ЗАГОЛОВКОВ_ОШИБКА_TIMESTAMP: Невалидный формат timestamp: {}", timestampStr);
            return true;
        }
    }

    private void setAuthenticationContext(HttpServletRequest request, String userId) {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }

        try {
            UUID validatedUserId = UUID.fromString(userId);
            GatewayUserPrincipal principal = GatewayUserPrincipal.builder()
                    .userId(validatedUserId)
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
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            log.debug("СИГНАТУРА_БЕЗОПАСНОСТЬ: Установлен аутентифицированный контекст для userId {}", userId);
        } catch (IllegalArgumentException ex) {
            log.warn("СИГНАТУРА_БЕЗОПАСНОСТЬ_ОШИБКА: userId из заголовка имеет неверный формат: {}", userId);
        }
    }

    private String defaultIfNull(String value) {
        return value != null ? value : "";
    }
}




