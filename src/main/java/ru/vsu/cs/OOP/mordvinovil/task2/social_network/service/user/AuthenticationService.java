package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.user;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.auth.SignInRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.auth.SignUpRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.auth.JwtAuthenticationResponse;

public interface AuthenticationService {
    JwtAuthenticationResponse signUp(SignUpRequest request);
    JwtAuthenticationResponse signIn(SignInRequest request);
}
