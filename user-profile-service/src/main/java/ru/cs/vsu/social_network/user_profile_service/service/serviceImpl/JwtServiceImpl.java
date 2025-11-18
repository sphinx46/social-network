package ru.cs.vsu.social_network.user_profile_service.service.serviceImpl;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.cs.vsu.social_network.user_profile_service.service.JwtService;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class JwtServiceImpl implements JwtService {
    @Value("${token.signing.key}")
    private String jwtSigningKey;
    

    /**
     * Извлечение имени пользователя из токена
     *
     * @param token токен
     * @return имя пользователя
     */
    @Override
    public String extractUserName(String token) {
        Map<String, Object> context = new HashMap<>();
        context.put("tokenLength", token != null ? token.length() : 0);

        log.info("JWT_ИЗВЛЕЧЕНИЕ_ИМЕНИ",
                "Извлечение имени пользователя из токена", context);

        try {
            String username = extractClaim(token, Claims::getSubject);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("username", username);

            log.info("JWT_ИМЯ_ИЗВЛЕЧЕНО",
                    "Имя пользователя успешно извлечено из токена", successContext);

            return username;
        } catch (Exception e) {
            log.error("JWT_ОШИБКА_ИЗВЛЕЧЕНИЯ_ИМЕНИ",
                    "Ошибка при извлечении имени пользователя из токена", context, e);
            throw e;
        }
    }


    /**
     * Проверка токена на валидность
     *
     * @param token       токен
     * @param userDetails данные пользователя
     * @return true, если токен валиден
     */
    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        Map<String, Object> context = new HashMap<>();
        context.put("username", userDetails.getUsername());
        context.put("tokenLength", token != null ? token.length() : 0);

        log.info("JWT_ПРОВЕРКА_ВАЛИДНОСТИ",
                "Проверка валидности JWT токена", context);

        try {
            final String userName = extractUserName(token);
            boolean isValid = (userName.equals(userDetails.getUsername())) && !isTokenExpired(token);

            Map<String, Object> resultContext = new HashMap<>(context);
            resultContext.put("isValid", isValid);
            resultContext.put("extractedUsername", userName);

            log.info("JWT_ПРОВЕРКА_ЗАВЕРШЕНА",
                    "Проверка валидности JWT токена завершена", resultContext);

            return isValid;
        } catch (Exception e) {
            log.error("JWT_ОШИБКА_ПРОВЕРКИ_ВАЛИДНОСТИ",
                    "Ошибка при проверке валидности JWT токена", context, e);
            throw e;
        }
    }

    /**
     * Извлечение данных из токена
     *
     * @param token           токен
     * @param claimsResolvers функция извлечения данных
     * @param <T>             тип данных
     * @return данные
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    /**
     * Генерация токена
     *
     * @param extraClaims дополнительные данные
     * @param userDetails данные пользователя
     * @return токен
     */
    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder().setClaims(extraClaims).setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 100000 * 60 * 24))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }

    /**
     * Проверка токена на просроченность
     *
     * @param token токен
     * @return true, если токен просрочен
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Извлечение даты истечения токена
     *
     * @param token токен
     * @return дата истечения
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Извлечение всех данных из токена
     *
     * @param token токен
     * @return данные
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(getSigningKey()).build().parseClaimsJws(token)
                .getBody();
    }

    /**
     * Получение ключа для подписи токена
     *
     * @return ключ
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}