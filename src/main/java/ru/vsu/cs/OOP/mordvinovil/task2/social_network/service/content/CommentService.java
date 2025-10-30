package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.content;

import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.CommentRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PageRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.CommentResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.PageResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.Service;

import java.util.concurrent.CompletableFuture;

public interface CommentService extends Service<CommentRequest, User, CommentResponse> {
    CommentResponse create(CommentRequest request, User currentUser);
    CommentResponse editComment(Long id, CommentRequest request, User currentUser);
    CompletableFuture<Boolean> deleteComment(Long commentId, User currentUser);
    CommentResponse getCommentById(Long commentId);

    PageResponse<CommentResponse> getAllCommentsOnPost(Long postId, PageRequest pageRequest);
}
