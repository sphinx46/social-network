package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.providersImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Comment;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.comment.CommentNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.CommentRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.providers.CommentEntityProvider;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CommentEntityProviderImpl implements CommentEntityProvider {

    private final CommentRepository commentRepository;

    @Override
    public Comment getById(Long id) {
        return commentRepository.findByIdWithLikes(id)
                .orElseThrow(() -> new CommentNotFoundException(ResponseMessageConstants.FAILURE_COMMENT_NOT_FOUND));
    }

    @Override
    public Optional<Comment> findById(Long id) {
        return commentRepository.findById(id);
    }
}