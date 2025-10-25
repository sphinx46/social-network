package ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.servicesImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.PostRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.custom.AccessDeniedException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.PostContentEmptyException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.PostContentTooLongException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.PostNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.PostRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.validations.services.PostValidator;

@Component
@RequiredArgsConstructor
public class PostValidatorImpl implements PostValidator {
    private final PostRepository postRepository;

    @Override
    public void validate(PostRequest request, User currentUser) {
        validatePostCreation(request, currentUser);
    }

    @Override
    public void validatePostCreation(PostRequest request, User currentUser) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new PostContentEmptyException(ResponseMessageConstants.FAILURE_POST_CONTENT_CANNOT_BE_EMPTY);
        }

        if (request.getContent().length() > 2000) {
            throw new PostContentTooLongException(ResponseMessageConstants.FAILURE_POST_CONTENT_TOO_LONG);
        }
    }

    @Override
    public void validatePostUpdate(PostRequest request, Long postId, User currentUser) {
        validatePostOwnership(postId, currentUser);

        if (request.getContent() != null && request.getContent().length() > 2000) {
            throw new IllegalArgumentException(ResponseMessageConstants.FAILURE_POST_CONTENT_TOO_LONG);
        }
    }

    @Override
    public void validatePostOwnership(Long postId, User currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(ResponseMessageConstants.FAILURE_POST_NOT_FOUND));

        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(ResponseMessageConstants.ACCESS_DENIED);
        }
    }
}