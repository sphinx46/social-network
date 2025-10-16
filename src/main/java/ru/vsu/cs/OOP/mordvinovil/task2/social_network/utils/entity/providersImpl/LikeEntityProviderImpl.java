package ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.providersImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.entities.Like;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.exceptions.entity.LikeNotFoundException;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.repositories.LikeRepository;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.constants.ResponseMessageConstants;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.utils.entity.providers.LikeEntityProvider;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LikeEntityProviderImpl implements LikeEntityProvider {

    private final LikeRepository likeRepository;

    @Override
    public Like getById(Long id) {
        return likeRepository.findById(id)
                .orElseThrow(() -> new LikeNotFoundException(ResponseMessageConstants.NOT_FOUND));
    }

    @Override
    public Optional<Like> findById(Long id) {
        return likeRepository.findById(id);
    }
}