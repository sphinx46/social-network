package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.providersImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Post;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.PostNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.PostRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.providers.PostEntityProvider;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PostEntityProviderImpl implements PostEntityProvider {

    private final PostRepository postRepository;

    @Override
    public Post getById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(ResponseMessageConstants.NOT_FOUND));
    }

    @Override
    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }
}