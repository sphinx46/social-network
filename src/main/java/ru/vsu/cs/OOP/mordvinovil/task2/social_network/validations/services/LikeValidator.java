package ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.LikeCommentRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.LikePostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;

public interface LikeValidator extends Validator<Object, User> {
    void validateLikeCreation(LikePostRequest request, User currentUser);
    void validateLikeCreation(LikeCommentRequest request, User currentUser);
    void validateLikeDeletion(Long targetId, String targetType, User currentUser);
}