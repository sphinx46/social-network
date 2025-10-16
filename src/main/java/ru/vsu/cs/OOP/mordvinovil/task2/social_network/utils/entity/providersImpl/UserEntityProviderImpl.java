package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.providersImpl;

import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.UserNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.UserRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.providers.UserEntityProvider;

import java.util.Optional;

@Component
public class UserEntityProviderImpl implements UserEntityProvider {
    private final UserRepository userRepository;

    public UserEntityProviderImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(ResponseMessageConstants.NOT_FOUND));
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}