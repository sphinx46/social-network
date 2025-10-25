package ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.servicesImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.CommentRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Comment;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.custom.AccessDeniedException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.CommentContentTooLongException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.CommentEmptyContentException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.CommentNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.PostNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.CommentRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.PostRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.CommentValidator;

@Component
@RequiredArgsConstructor
public class CommentValidatorImpl implements CommentValidator {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;


    @Override
    public void validate(CommentRequest request, User currentUser) {
        validateCommentCreation(request, currentUser);
    }

    @Override
    public void validateCommentCreation(CommentRequest request, User currentUser) {
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new PostNotFoundException(ResponseMessageConstants.FAILURE_POST_NOT_FOUND));

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new CommentEmptyContentException(ResponseMessageConstants.FAILURE_COMMENT_EMPTY_CONTENT);
        }

        if (request.getContent().length() > 1000) {
            throw new CommentContentTooLongException(ResponseMessageConstants.FAILURE_COMMENT_CONTENT_TOO_LONG);
        }
    }

    @Override
    public void validateCommentUpdate(CommentRequest request, Long commentId, User currentUser) {
        validateCommentOwnership(commentId, currentUser);

        if (request.getContent() != null && request.getContent().length() > 1000) {
            throw new CommentContentTooLongException(ResponseMessageConstants.FAILURE_COMMENT_CONTENT_TOO_LONG);
        }
    }

    @Override
    public void validateCommentOwnership(Long commentId, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(ResponseMessageConstants.FAILURE_COMMENT_NOT_FOUND));

        boolean isCommentCreator = comment.getCreator().getId().equals(currentUser.getId());
        boolean isPostOwner = comment.getPost().getUser().getId().equals(currentUser.getId());

        if (!isCommentCreator && !isPostOwner) {
            throw new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED);
        }
    }
}