package ru.vsu.cs.OOP.mordvinovil.task2.social_network.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.request.CommentRequest;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.dto.response.CommentResponse;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Comment;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.User;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.CommentRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.PostRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.comment.CommentNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.post.PostNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.security.AccessDeniedException;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public CommentResponse create(CommentRequest request, User currentUser) {
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new PostNotFoundException("Пост не найден."));

        var comment = Comment.builder()
                .post(post)
                .creator(currentUser)
                .content(request.getContent())
                .time(LocalDate.now())
                .imageUrl(request.getImageUrl() != null ? request.getImageUrl() : null)
                .build();

        Comment savedComment = commentRepository.save(comment);
        CommentResponse response = modelMapper.map(savedComment, CommentResponse.class);
        response.setUsername(currentUser.getUsername());
        return response;
    }

    public CommentResponse editComment(Long id, CommentRequest request, User currentUser) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("Комментарий не найден."));

        if (!comment.getCreator().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("У вас нет прав для редактирования этого комментария.");
        }

        if (request.getContent() != null) {
            comment.setContent(request.getContent());
        }

        if (request.getImageUrl() != null) {
            comment.setImageUrl(request.getImageUrl());
        }

        Comment updatedComment = commentRepository.save(comment);
        CommentResponse response = modelMapper.map(updatedComment, CommentResponse.class);
        return response;
    }

    @Transactional
    public CompletableFuture<Boolean> deleteComment(Long commentId, User currentUser) throws Exception {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Комментарий не найден."));

        boolean isCommentCreator = comment.getCreator().getId().equals(currentUser.getId());
        boolean isPostOwner = comment.getPost().getUser().getId().equals(currentUser.getId());

        if (isCommentCreator || isPostOwner) {
            try {
                commentRepository.delete(comment);
                return CompletableFuture.completedFuture(true);
            } catch (Exception e) {
                throw new Exception("Ошибка при удалении комментария");
            }
        } else {
            throw new AccessDeniedException("У вас нет прав для удаления этого комментария.");
        }
    }

    public CommentResponse getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Комментарий не найден."));;

        CommentResponse response = modelMapper.map(comment, CommentResponse.class);
        response.setUsername(comment.getCreator().getUsername());
        return response;
    }
}
