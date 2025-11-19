package ru.cs.vsu.social_network.user_profile_service.config.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HeaderSignatureFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private HeaderSignatureFilter filter;
    private String testSecret = "test-signature-secret-for-unit-tests-only";

    @BeforeEach
    void setUp() {
        filter = new HeaderSignatureFilter();
        ReflectionTestUtils.setField(filter, "signatureSecret", testSecret);
    }

    @Test
    @DisplayName("Пропуск публичного GET эндпоинта без проверки подписи")
    void shouldNotFilter_whenPublicGetEndpoint() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/user-profile/profile/" + UUID.randomUUID());
        when(request.getContextPath()).thenReturn("/api/user-profile");
        when(request.getMethod()).thenReturn("GET");

        boolean result = filter.shouldNotFilter(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Пропуск actuator эндпоинта без проверки подписи")
    void shouldNotFilter_whenActuatorEndpoint() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/actuator/health");
        when(request.getContextPath()).thenReturn("");
        when(request.getMethod()).thenReturn("GET");

        boolean result = filter.shouldNotFilter(request);

        assertTrue(result);
    }

    @Test
    @DisplayName("Требуется проверка подписи для защищенного эндпоинта")
    void shouldNotFilter_whenProtectedEndpoint() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/user-profile/profile/me");
        when(request.getContextPath()).thenReturn("/api/user-profile");
        when(request.getMethod()).thenReturn("GET");

        boolean result = filter.shouldNotFilter(request);

        assertFalse(result);
    }

    @Test
    @DisplayName("Ошибка 401 при отсутствии заголовков подписи")
    void doFilterInternal_whenMissingHeaders_shouldReturn401() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/user-profile/profile/me");
        when(request.getContextPath()).thenReturn("/api/user-profile");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("X-User-Id")).thenReturn(null);
        when(request.getHeader("X-Timestamp")).thenReturn(null);
        when(request.getHeader("X-Signature")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(401);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("Ошибка 401 при невалидной подписи")
    void doFilterInternal_whenInvalidSignature_shouldReturn401() throws ServletException, IOException {
        String userId = UUID.randomUUID().toString();
        String timestamp = String.valueOf(System.currentTimeMillis());
        String invalidSignature = "invalid-signature";

        when(request.getRequestURI()).thenReturn("/api/user-profile/profile/me");
        when(request.getContextPath()).thenReturn("/api/user-profile");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("X-User-Id")).thenReturn(userId);
        when(request.getHeader("X-Timestamp")).thenReturn(timestamp);
        when(request.getHeader("X-Signature")).thenReturn(invalidSignature);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(401);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("Ошибка 401 при просроченном timestamp")
    void doFilterInternal_whenExpiredTimestamp_shouldReturn401() throws ServletException, IOException {
        String userId = UUID.randomUUID().toString();
        String expiredTimestamp = String.valueOf(System.currentTimeMillis() - 60000);
        String signature = generateSignature(userId, expiredTimestamp);

        when(request.getRequestURI()).thenReturn("/api/user-profile/profile/me");
        when(request.getContextPath()).thenReturn("/api/user-profile");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("X-User-Id")).thenReturn(userId);
        when(request.getHeader("X-Timestamp")).thenReturn(expiredTimestamp);
        when(request.getHeader("X-Signature")).thenReturn(signature);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(401);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("Успешная проверка подписи и установка SecurityContext")
    void doFilterInternal_whenValidSignature_shouldSetSecurityContext() throws ServletException, IOException {
        String userId = UUID.randomUUID().toString();
        String timestamp = String.valueOf(System.currentTimeMillis());
        String signature = generateSignature(userId, timestamp);

        when(request.getRequestURI()).thenReturn("/api/user-profile/profile/me");
        when(request.getContextPath()).thenReturn("/api/user-profile");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("X-User-Id")).thenReturn(userId);
        when(request.getHeader("X-Timestamp")).thenReturn(timestamp);
        when(request.getHeader("X-Signature")).thenReturn(signature);
        when(request.getHeader("X-Username")).thenReturn("testuser");
        when(request.getHeader("X-Email")).thenReturn("test@example.com");
        when(request.getHeader("X-First-Name")).thenReturn("Test");
        when(request.getHeader("X-Last-Name")).thenReturn("User");

        filter.doFilterInternal(request, response, filterChain);

        verify(response, never()).setStatus(anyInt());
        verify(filterChain).doFilter(request, response);
    }

    private String generateSignature(String userId, String timestamp) {
        try {
            String dataToSign = userId + "|" + timestamp;
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    testSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
            byte[] signatureBytes = mac.doFinal(dataToSign.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка генерации подписи", e);
        }
    }
}

