package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.user;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.user.JwtService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.CentralLogger;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Service
public class JwtServiceImpl implements JwtService {
    @Value("${token.signing.key}")
    private String jwtSigningKey;

    private final CentralLogger centralLogger;

    public JwtServiceImpl(CentralLogger centralLogger) {
        this.centralLogger = centralLogger;
    }

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

        centralLogger.logInfo("JWT_ИЗВЛЕЧЕНИЕ_ИМЕНИ",
                "Извлечение имени пользователя из токена", context);

        try {
            String username = extractClaim(token, Claims::getSubject);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("username", username);

            centralLogger.logInfo("JWT_ИМЯ_ИЗВЛЕЧЕНО",
                    "Имя пользователя успешно извлечено из токена", successContext);

            return username;
        } catch (Exception e) {
            centralLogger.logError("JWT_ОШИБКА_ИЗВЛЕЧЕНИЯ_ИМЕНИ",
                    "Ошибка при извлечении имени пользователя из токена", context, e);
            throw e;
        }
    }

    /**
     * Генерация токена
     *
     * @param userDetails данные пользователя
     * @return токен
     */
    @Override
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> context = new HashMap<>();
        context.put("username", userDetails.getUsername());

        centralLogger.logInfo("JWT_ГЕНЕРАЦИЯ_ТОКЕНА",
                "Генерация JWT токена", context);

        try {
            Map<String, Object> claims = new HashMap<>();
            if (userDetails instanceof User customUserDetails) {
                claims.put("id", customUserDetails.getId());
                claims.put("email", customUserDetails.getEmail());
                claims.put("role", customUserDetails.getRole());
            }
            String token = generateToken(claims, userDetails);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("tokenLength", token.length());

            centralLogger.logInfo("JWT_ТОКЕН_СГЕНЕРИРОВАН",
                    "JWT токен успешно сгенерирован", successContext);

            return token;
        } catch (Exception e) {
            centralLogger.logError("JWT_ОШИБКА_ГЕНЕРАЦИИ_ТОКЕНА",
                    "Ошибка при генерации JWT токена", context, e);
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

        centralLogger.logInfo("JWT_ПРОВЕРКА_ВАЛИДНОСТИ",
                "Проверка валидности JWT токена", context);

        try {
            final String userName = extractUserName(token);
            boolean isValid = (userName.equals(userDetails.getUsername())) && !isTokenExpired(token);

            Map<String, Object> resultContext = new HashMap<>(context);
            resultContext.put("isValid", isValid);
            resultContext.put("extractedUsername", userName);

            centralLogger.logInfo("JWT_ПРОВЕРКА_ЗАВЕРШЕНА",
                    "Проверка валидности JWT токена завершена", resultContext);

            return isValid;
        } catch (Exception e) {
            centralLogger.logError("JWT_ОШИБКА_ПРОВЕРКИ_ВАЛИДНОСТИ",
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