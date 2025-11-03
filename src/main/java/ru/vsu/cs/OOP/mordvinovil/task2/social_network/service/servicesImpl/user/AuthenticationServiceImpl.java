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

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserServiceImpl userServiceImpl;
    private final JwtServiceImpl jwtServiceImpl;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ProfileServiceImpl profileServiceImpl;

    /**
     * Регистрация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    @Transactional
    @Override
    public JwtAuthenticationResponse signUp(SignUpRequest request) {
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
        return new JwtAuthenticationResponse(jwt);
    }

    /**
     * Аутентификация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    @Override
    public JwtAuthenticationResponse signIn(SignInRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
        ));

        var user = userServiceImpl
                .userDetailsService()
                .loadUserByUsername(request.getUsername());

        var jwt = jwtServiceImpl.generateToken(user);
        return new JwtAuthenticationResponse(jwt);
    }
}