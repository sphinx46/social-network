package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.content;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.LikeCommentRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.LikePostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.LikeCommentResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.LikePostResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;

public interface LikeService {
    LikeCommentResponse likeComment(User currentUser, LikeCommentRequest request);
    LikePostResponse likePost(User currentUser, LikePostRequest request);
    LikeCommentResponse deleteLikeByComment(User currentUser, Long commentId);
    LikePostResponse deleteLikeByPost(User currentUser, Long postId);

    PageResponse<LikePostResponse> getLikesByPost(Long postId, PageRequest pageRequest);
    PageResponse<LikeCommentResponse> getLikesByComment(Long commentId, PageRequest pageRequest);
}
