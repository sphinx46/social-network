package ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.servicesImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.ProfileRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Profile;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.ProfileAlreadyExistsException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.ProfileNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.ProfileRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.ProfileValidator;

@Component
@RequiredArgsConstructor
public class ProfileValidatorImpl implements ProfileValidator {
    private final ProfileRepository profileRepository;

    @Override
    public void validate(ProfileRequest request, User currentUser) {
        validateProfileCreation(request, currentUser);
    }

    @Override
    public void validateProfileCreation(ProfileRequest request, User currentUser) {
        if (profileRepository.findByUser(currentUser).isPresent()) {
            throw new ProfileAlreadyExistsException(ResponseMessageConstants.FAILURE_CREATE_PROFILE);
        }

        if (request.getBio() != null && request.getBio().length() > 500) {
            throw new IllegalArgumentException("Bio too long");
        }

        if (request.getCity() != null && request.getCity().length() > 100) {
            throw new IllegalArgumentException("City name too long");
        }
    }

    @Override
    public void validateProfileUpdate(ProfileRequest request, User currentUser) {
        getProfileEntity(currentUser);

        if (request.getBio() != null && request.getBio().length() > 500) {
            throw new IllegalArgumentException("Bio too long");
        }

        if (request.getCity() != null && request.getCity().length() > 100) {
            throw new IllegalArgumentException("City name too long");
        }
    }

    @Override
    public void validateAvatarUpload(User currentUser) {
        getProfileEntity(currentUser);
    }

    private Profile getProfileEntity(User user) {
        return profileRepository.findByUser(user)
                .orElseThrow(() -> new ProfileNotFoundException(ResponseMessageConstants.NOT_FOUND));
    }
}