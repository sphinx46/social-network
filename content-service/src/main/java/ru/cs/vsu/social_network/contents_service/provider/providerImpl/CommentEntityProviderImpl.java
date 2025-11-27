package ru.cs.vsu.social_network.contents_service.provider.providerImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.entity.Comment;
import ru.cs.vsu.social_network.contents_service.exception.comment.CommentNotFoundException;
import ru.cs.vsu.social_network.contents_service.provider.CommentEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.CommentRepository;
import ru.cs.vsu.social_network.contents_service.utils.MessageConstants;

import java.util.UUID;

/**
 * Реализация провайдера для получения сущности Comment.
 * Обеспечивает доступ к данным постов с обработкой исключительных ситуаций.
 */
@Slf4j
@Component
public class CommentEntityProviderImpl implements CommentEntityProvider {
    private final CommentRepository commentRepository;

    public CommentEntityProviderImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /** {@inheritDoc} */
    @Override
    public Comment getById(UUID id) {
        log.info("КОММЕНТАРИЙ_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_НАЧАЛО: запрос комментария с ID: {}", id);

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("КОММЕНТАРИЙ_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_ОШИБКА: " +
                            "комментарий с ID: {} не найден", id);
                    return new CommentNotFoundException(MessageConstants.COMMENT_NOT_FOUND_FAILURE);
                });

        log.info("КОММЕНТАРИЙ_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_УСПЕХ: комментарий с ID: {} найден", id);
        return comment;
    }
}