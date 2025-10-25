package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.LikeCommentRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.LikePostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.LikeCommentResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.LikePostResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;

import java.util.List;

public interface LikeService {
    LikeCommentResponse likeComment(User currentUser, LikeCommentRequest request);
    LikePostResponse likePost(User currentUser, LikePostRequest request);
    List<LikePostResponse> getLikesByPost(Long postId);
    List<LikeCommentResponse> getLikesByComment(Long commentId);
    LikeCommentResponse deleteLikeByComment(User currentUser, Long commentId);
    LikePostResponse deleteLikeByPost(User currentUser, Long postId);
}
