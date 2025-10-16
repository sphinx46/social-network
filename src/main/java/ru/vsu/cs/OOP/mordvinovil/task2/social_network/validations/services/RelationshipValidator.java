package ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.RelationshipRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;

public interface RelationshipValidator extends Validator<RelationshipRequest, User> {
    void validateFriendRequest(RelationshipRequest request, User currentUser);
    void validateBlockUser(RelationshipRequest request, User currentUser);
    void validateStatusChange(RelationshipRequest request, User currentUser);
}