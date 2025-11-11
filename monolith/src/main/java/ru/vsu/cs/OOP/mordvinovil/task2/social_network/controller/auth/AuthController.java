package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.auth.SignInRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.auth.SignUpRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.auth.JwtAuthenticationResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.user.AuthenticationService;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.logging.CentralLogger;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация")
public class AuthController {
    private final AuthenticationService authenticationService;
    private final CentralLogger centralLogger;

    @Operation(summary = "Регистрация пользователя")
    @PostMapping("/sign-up")
    public JwtAuthenticationResponse signUp(@RequestBody @Valid SignUpRequest request) {
        Map<String, Object> context = new HashMap<>();
        context.put("email", request.getEmail());
        context.put("username", request.getUsername());
        context.put("city", request.getCity());

        centralLogger.logInfo("РЕГИСТРАЦИЯ_ЗАПРОС",
                "Запрос на регистрацию пользователя", context);

        try {
            JwtAuthenticationResponse response = authenticationService.signUp(request);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("tokenLength", response.getToken().length());

            centralLogger.logInfo("РЕГИСТРАЦИЯ_УСПЕХ",
                    "Пользователь успешно зарегистрирован", successContext);

            return response;
        } catch (Exception e) {
            centralLogger.logError("РЕГИСТРАЦИЯ_ОШИБКА",
                    "Ошибка при регистрации пользователя", context, e);
            throw e;
        }
    }

    @Operation(summary = "Авторизация пользователя")
    @PostMapping("/sign-in")
    public JwtAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request) {
        Map<String, Object> context = new HashMap<>();
        context.put("username", request.getUsername());

        centralLogger.logInfo("АВТОРИЗАЦИЯ_ЗАПРОС",
                "Запрос на авторизацию пользователя", context);

        try {
            JwtAuthenticationResponse response = authenticationService.signIn(request);

            Map<String, Object> successContext = new HashMap<>(context);
            successContext.put("tokenLength", response.getToken().length());

            centralLogger.logInfo("АВТОРИЗАЦИЯ_УСПЕХ",
                    "Пользователь успешно авторизован", successContext);

            return response;
        } catch (Exception e) {
            centralLogger.logError("АВТОРИЗАЦИЯ_ОШИБКА",
                    "Ошибка при авторизации пользователя", context, e);
            throw e;
        }
    }
}