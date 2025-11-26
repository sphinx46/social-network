package ru.cs.vsu.social_network.contents_service.provider.providerImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.cs.vsu.social_network.contents_service.entity.Post;
import ru.cs.vsu.social_network.contents_service.exception.PostNotFoundException;
import ru.cs.vsu.social_network.contents_service.provider.PostEntityProvider;
import ru.cs.vsu.social_network.contents_service.repository.PostRepository;
import ru.cs.vsu.social_network.contents_service.utils.MessageConstants;

import java.util.UUID;

/**
 * Реализация провайдера для получения сущности Post.
 * Обеспечивает доступ к данным постов с обработкой исключительных ситуаций.
 */
@Slf4j
@Component
public class PostEntityProviderImpl implements PostEntityProvider {
    private final PostRepository postRepository;

    public PostEntityProviderImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    /** {@inheritDoc} */
    @Override
    public Post getById(UUID id) {
        log.info("ПОСТ_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_НАЧАЛО: запрос поста с ID: {}", id);

        Post post = postRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("ПОСТ_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_ОШИБКА: пост с ID: {} не найден", id);
                    return new PostNotFoundException(MessageConstants.POST_NOT_FOUND_FAILURE);
                });

        log.info("ПОСТ_ПРОВАЙДЕР_ПОЛУЧЕНИЕ_УСПЕХ: пост с ID: {} найден", id);
        return post;
    }
}