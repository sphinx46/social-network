package ru.cs.vsu.social_network.user_profile_service.config.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Slf4j
@Component
public class HeaderSignatureFilter extends OncePerRequestFilter {
    @Value("${app.gateway.signature-secret}")
    private String signatureSecret;

    private static final long MAX_TIMESTAMP_DIFF_MS = 30000;
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        if (shouldNotFilter(request)) {
            log.debug("СИГНАТУРА_ПРОПУСК: Публичный эндпоинт: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        String userId = request.getHeader("X-User-Id");
        String timestamp = request.getHeader("X-Timestamp");
        String signature = request.getHeader("X-Signature");

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
        filterChain.doFilter(request, response);
    }

    /**
     * Пропускает публичные эндпоинты из проверки подписи
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/actuator/") ||
                path.equals("/api/user-profile/profile/me") && "GET".equalsIgnoreCase(request.getMethod()) ||
                path.startsWith("/api/user-profile/profile/") && "GET".equalsIgnoreCase(request.getMethod());
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
}




