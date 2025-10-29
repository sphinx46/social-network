package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.user;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.SignInRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.SignUpRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.JwtAuthenticationResponse;

public interface AuthenticationService {
    JwtAuthenticationResponse signUp(SignUpRequest request);
    JwtAuthenticationResponse signIn(SignInRequest request);
}
