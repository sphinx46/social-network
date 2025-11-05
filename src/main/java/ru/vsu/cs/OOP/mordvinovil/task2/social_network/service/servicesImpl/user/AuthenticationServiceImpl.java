package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.servicesImpl.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.auth.SignInRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.auth.SignUpRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.auth.JwtAuthenticationResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Role;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.security.filters.UserDetailsImpl;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.user.AuthenticationService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.CentralLogger;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserServiceImpl userServiceImpl;
    private final JwtServiceImpl jwtServiceImpl;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ProfileServiceImpl profileServiceImpl;
    private final CentralLogger centralLogger;

    /**
     * Регистрация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    @Transactional
    @Override
    public JwtAuthenticationResponse signUp(SignUpRequest request) {
        Map<String, Object> context = new HashMap<>();
        context.put("username", request.getUsername());
        context.put("email", request.getEmail());
        context.put("city", request.getCity());

        centralLogger.logInfo("РЕГИСТРАЦИЯ_ПОЛЬЗОВАТЕЛЯ",
                "Регистрация нового пользователя", context);

        try {
            var user = User.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .city(request.getCity())
                    .isOnline(false)
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(Role.ROLE_USER)
                    .build();

            User savedUser = userServiceImpl.create(user);

            profileServiceImpl.createDefaultProfile(savedUser);

            UserDetails userDetails = UserDetailsImpl.build(savedUser);
            var jwt = jwtServiceImpl.generateToken(userDetails);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("userId", savedUser.getId());
            successContext.put("tokenLength", jwt.length());

            centralLogger.logInfo("РЕГИСТРАЦИЯ_УСПЕШНА",
                    "Регистрация пользователя успешно завершена", successContext);

            return new JwtAuthenticationResponse(jwt);
        } catch (Exception e) {
            centralLogger.logError("РЕГИСТРАЦИЯ_ОШИБКА",
                    "Ошибка при регистрации пользователя", context, e);
            throw e;
        }
    }

    /**
     * Аутентификация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    @Override
    public JwtAuthenticationResponse signIn(SignInRequest request) {
        Map<String, Object> context = new HashMap<>();
        context.put("username", request.getUsername());

        centralLogger.logInfo("АУТЕНТИФИКАЦИЯ_ПОЛЬЗОВАТЕЛЯ",
                "Аутентификация пользователя", context);

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
            ));

            var user = userServiceImpl
                    .userDetailsService()
                    .loadUserByUsername(request.getUsername());

            var jwt = jwtServiceImpl.generateToken(user);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("tokenLength", jwt.length());

            centralLogger.logInfo("АУТЕНТИФИКАЦИЯ_УСПЕШНА",
                    "Аутентификация пользователя успешно завершена", successContext);

            return new JwtAuthenticationResponse(jwt);
        } catch (Exception e) {
            centralLogger.logError("АУТЕНТИФИКАЦИЯ_ОШИБКА",
                    "Ошибка при аутентификации пользователя", context, e);
            throw e;
        }
    }
}