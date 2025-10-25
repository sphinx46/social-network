package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.RelationshipRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.RelationshipResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;

import java.util.List;

public interface RelationshipService {
    RelationshipResponse sendFriendRequest(RelationshipRequest request, User currentUser);
    List<RelationshipResponse> getFriendList(User currentUser);
    List<RelationshipResponse> getBlackList(User currentUser);
    List<RelationshipResponse> getDeclinedList(User currentUser);
    RelationshipResponse blockUser(RelationshipRequest request, User currentUser);
    RelationshipResponse acceptFriendRequest(RelationshipRequest request, User currentUser);
    RelationshipResponse declineFriendRequest(RelationshipRequest request, User currentUser);
}
