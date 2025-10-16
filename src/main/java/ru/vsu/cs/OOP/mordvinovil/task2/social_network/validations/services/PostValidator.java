package ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;

public interface PostValidator extends Validator<PostRequest, User> {
    void validatePostCreation(PostRequest request, User currentUser);
    void validatePostUpdate(PostRequest request, Long postId, User currentUser);
    void validatePostOwnership(Long postId, User currentUser);
}