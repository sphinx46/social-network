package ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.CommentRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;

public interface CommentValidator extends Validator<CommentRequest, User> {
    void validateCommentCreation(CommentRequest request, User currentUser);
    void validateCommentUpdate(CommentRequest request, Long commentId, User currentUser);
    void validateCommentOwnership(Long commentId, User currentUser);
}