package ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.profile.ProfileRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;

public interface ProfileValidator extends Validator<ProfileRequest, User> {
    void validateProfileCreation(ProfileRequest request, User currentUser);
    void validateProfileUpdate(ProfileRequest request, User currentUser);
    void validateAvatarUpload(User currentUser);
}