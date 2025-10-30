package ru.vsu.cs.OOP.mordvinovil.task2.social_network.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.SignInRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.SignUpRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.JwtAuthenticationResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.user.AuthenticationService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация")
public class AuthController {
    private final AuthenticationService authenticationService;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Operation(summary = "Регистрация пользователя")
    @PostMapping("/sign-up")
    public JwtAuthenticationResponse signUp(@RequestBody @Valid SignUpRequest request) {
        log.info("Запрос на регистрацию пользователя: {}", request.getEmail());
        JwtAuthenticationResponse response = authenticationService.signUp(request);
        log.info("Пользователь {} успешно зарегистрирован", request.getEmail());
        return response;
    }

    @Operation(summary = "Авторизация пользователя")
    @PostMapping("/sign-in")
    public JwtAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request) {
        log.info("Запрос на авторизацию пользователя: {}", request.getUsername());
        JwtAuthenticationResponse response = authenticationService.signIn(request);
        log.info("Пользователь {} успешно авторизован", request.getUsername());
        return response;
    }
}