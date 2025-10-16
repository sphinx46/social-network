package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.CommentRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.CommentResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Comment;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.CommentRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.EntityMapper;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.EntityUtils;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.factory.ContentFactory;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.CommentValidator;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final EntityMapper entityMapper;
    private final ContentFactory contentFactory;
    private final CommentValidator commentValidator;
    private final EntityUtils entityUtils;

    @Transactional
    public CommentResponse create(CommentRequest request, User currentUser) {
        commentValidator.validate(request, currentUser);

        Post post = entityUtils.getPost(request.getPostId());
        Comment comment = contentFactory.createComment(currentUser, post, request.getContent(), request.getImageUrl());
        Comment savedComment = commentRepository.save(comment);

        return entityMapper.map(savedComment, CommentResponse.class);
    }

    @Transactional
    public CommentResponse editComment(Long id, CommentRequest request, User currentUser) {
        commentValidator.validateCommentUpdate(request, id, currentUser);

        Comment comment = entityUtils.getComment(id);

        if (request.getContent() != null) {
            comment.setContent(request.getContent());
        }

        if (request.getImageUrl() != null) {
            comment.setImageUrl(request.getImageUrl());
        }

        Comment updatedComment = commentRepository.save(comment);
        return entityMapper.map(updatedComment, CommentResponse.class);
    }

    @Transactional
    public CompletableFuture<Boolean> deleteComment(Long commentId, User currentUser) {
        commentValidator.validateCommentOwnership(commentId, currentUser);

        Comment comment = entityUtils.getComment(commentId);
        commentRepository.delete(comment);
        return CompletableFuture.completedFuture(true);
    }

    public CommentResponse getCommentById(Long commentId) {
        Comment comment = entityUtils.getComment(commentId);
        return entityMapper.map(comment, CommentResponse.class);
    }
}