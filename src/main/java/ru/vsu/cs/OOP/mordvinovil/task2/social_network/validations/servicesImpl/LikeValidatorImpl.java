package ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.servicesImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.LikeCommentRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.LikePostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Comment;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Like;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.CommentNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.LikeNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.PostNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.CommentRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.LikeRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.PostRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.LikeValidator;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LikeValidatorImpl implements LikeValidator {
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Override
    public void validate(Object request, User currentUser) {
        if (request instanceof LikePostRequest likePostRequest) {
            validateLikeCreation(likePostRequest, currentUser);
        } else if (request instanceof LikeCommentRequest likeCommentRequest) {
            validateLikeCreation(likeCommentRequest, currentUser);
        }
    }

    @Override
    public void validateLikeCreation(LikePostRequest request, User currentUser) {
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new PostNotFoundException(ResponseMessageConstants.NOT_FOUND));

        Optional<Like> existingLike = likeRepository.findByUserIdAndPostId(currentUser.getId(), request.getPostId());
        if (existingLike.isPresent()) {
            throw new IllegalArgumentException("Like already exists for this post");
        }
    }

    @Override
    public void validateLikeCreation(LikeCommentRequest request, User currentUser) {
        Comment comment = commentRepository.findById(request.getCommentId())
                .orElseThrow(() -> new CommentNotFoundException(ResponseMessageConstants.NOT_FOUND));

        Optional<Like> existingLike = likeRepository.findByUserIdAndCommentId(currentUser.getId(), request.getCommentId());
        if (existingLike.isPresent()) {
            throw new IllegalArgumentException("Like already exists for this comment");
        }
    }

    @Override
    public void validateLikeDeletion(Long targetId, String targetType, User currentUser) {
        Like like;
        if ("post".equals(targetType)) {
            like = likeRepository.findByUserIdAndPostId(currentUser.getId(), targetId)
                    .orElseThrow(() -> new LikeNotFoundException(ResponseMessageConstants.NOT_FOUND));
        } else if ("comment".equals(targetType)) {
            like = likeRepository.findByUserIdAndCommentId(currentUser.getId(), targetId)
                    .orElseThrow(() -> new LikeNotFoundException(ResponseMessageConstants.NOT_FOUND));
        } else {
            throw new IllegalArgumentException("Invalid target type: " + targetType);
        }

        if (!like.getUser().getId().equals(currentUser.getId())) {
            throw new LikeNotFoundException(ResponseMessageConstants.NOT_FOUND);
        }
    }
}