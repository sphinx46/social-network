package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.relationship;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.common.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.relationship.RelationshipRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.common.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.relationship.RelationshipResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;

public interface RelationshipService {
    RelationshipResponse sendFriendRequest(RelationshipRequest request, User currentUser);
    PageResponse<RelationshipResponse> getFriendList(User currentUser, PageRequest pageRequest);
    PageResponse<RelationshipResponse> getBlackList(User currentUser, PageRequest pageRequest);
    PageResponse<RelationshipResponse> getDeclinedList(User currentUser, PageRequest pageRequest);
    RelationshipResponse blockUser(RelationshipRequest request, User currentUser);
    RelationshipResponse acceptFriendRequest(RelationshipRequest request, User currentUser);
    RelationshipResponse declineFriendRequest(RelationshipRequest request, User currentUser);
}